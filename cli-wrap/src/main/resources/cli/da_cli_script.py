#!/usr/bin/env python3
import requests
requests.packages.urllib3.disable_warnings()
import sys
import json
import os
import getpass
import inspect
import time

cmd_folder = os.path.realpath(os.path.abspath(os.path.split(inspect.getfile( inspect.currentframe() ))[0]))

with open(cmd_folder+'/config.json') as config_file:    
    config = json.load(config_file)

da_server_raw = os.getenv('DA_SERVER', config["daServer"])

if (da_server_raw == ""):
    print("Please configure DA server by command $ export DA_SERVER=your.adress")
    print("or by filling address in configuration file (config.json).")
    exit()
    
da_server = da_server_raw + "/da/rest/v-1"
da_server_ws = da_server_raw + "/da/ws"


GAV = "GROUP_ID:ARTIFACT_ID:VERSION"
GA  = "GROUP_ID:ARTIFACT_ID"
PRODUCT_VERSION = "PRODUCT_NAME:VERSION"
STATUS_VALUES   = ['SUPPORTED', 'UNSUPPORTED', 'SUPERSEDED', 'UNKNOWN']

def get_token(login, pswd):
    server = config["keycloakServer"]
    if server == "":
        print("Please configure KeyCloak server by filling address in configuration file (config.json).")
        exit()
    realm = config["keycloakRealm"]
    server += "/realms/" + realm + "/protocol/openid-connect/token"
    client_id = config["keycloakClientId"]
    grant_type="password"
    params = {'grant_type': grant_type,'client_id': client_id,'username': login,'password': pswd}
    r = requests.post(server, params, verify=False)
    if r.status_code == 200:
        reply = json.loads(r.content.decode('utf-8'))
        return( str(reply.get('access_token')))
    else:
        return ""

def auth():
    session = requests.Session() 

    MY_TOKEN = find_local_token()
    if MY_TOKEN:
        print("Using stored auth token.")
    else:
        login = input("Enter login name ["+getpass.getuser()+"]:")
        if login == "":
            login = getpass.getuser()
        print("Enter password for " + login)
        pswd = getpass.getpass()
        MY_TOKEN = get_token(login, pswd)
        store_local_token(MY_TOKEN)

    session.headers.update({'Authorization': " Bearer " + MY_TOKEN})
    return session
    
def find_local_token():
    path = os.path.expanduser("~") + "/.config/dependency-analyzer/auth-token"

    if not os.path.isfile(path):
        return None

    mt = os.path.getmtime(path)
    ct = time.time()

    if ct - mt > 60*115: # token created more then 115 minutes ago
        return None

    f = open(path, 'r')
    token = f.read()
    f.close()
    return token

def store_local_token(token):
    path = os.path.expanduser("~") + "/.config/dependency-analyzer/"
    if not os.path.exists(path):
        os.makedirs(path)

    f = open(path+"auth-token", 'w')
    f.write(token)
    f.close()

def requests_post(link, json_request, login = False):
    if login:
        session = auth()
    else:
        session = requests.Session()
    return requests_wrapper(session.post, link, json_request)

def requests_put(link, json_request):
    session = auth()
    return requests_wrapper(session.put, link, json_request)

def requests_delete(link, json_request):
    session = auth()
    return requests_wrapper(session.delete, link, json_request)

def requests_get(link):
    return requests_wrapper(requests.get, link)

def requests_wrapper(requests_method, link, json_request=None):
    try:
        if json_request:
            reply = requests_method("http://"+link, json=json_request)
        else:
            reply = requests_method("http://"+link)
        # this call throws an exception if the status is 4xx or 5xx
        # we have an exception for 404, since 404 can indicate other
        # normal stuff to the caller of the method
        if reply.status_code != 404:
            reply.raise_for_status()
        return reply
    except requests.exceptions.HTTPError:          
        print(">>> ERROR: Request to: {} failed <<<".format(link))
        print(">>> Status: {} <<<".format(reply.status_code))
        
        if reply.status_code == 403:
            print("Not authorized!")
            exit()
        if json_request:
            print(">>> JSON Data: {} <<<".format(json.dumps(json_request)))
        print("")
        if reply.status_code != 500 and reply.status_code != 503:
            try:
                print(reply.json()["details"])
            except (json.decoder.JSONDecodeError):
                pass
                
    except requests.exceptions.SSLError:
        print("Not authorized!")
                
        sys.exit(1)
       
def verify_response(response, failure_msg, fill=False):
    if 'success' in response and not response['success']:
        print(failure_msg)
    if 'message' in response:
        print(response['message'])
    if fill:
        pass#print("There were problems receiving response from the server (504 Gateway timeout), however your request might be performed successfully. Please wait a few minutes and check if the requested changes are done. If not and this problem persists please report the issue.")


def helper_print_white_artifacts_products(response, show_artifacts=True):
    if type(response) == dict and response['errorMessage']:
        print(response['errorMessage'])
    else:
        for item in response:
            product = item['name']
            product_version = item['version']
            product_support = item['supportStatus']
            product_id = ""
            if('id' in item):
                product_id = " ("+str(item['id'])+")"

            if show_artifacts:
                gid = item['gav']['groupId']
                aid = item['gav']['artifactId']
                ver = item['gav']['version']
                print("{}:{}:{}\t{}:{} {}".format(gid, aid, ver, product,
                                                    product_version, product_support))
            else:
                print("{}:{} {}{}".format(product, product_version, product_support, product_id))



def add_artifacts(gav, color, product=None, status=None):
    if color == "white":
        validate_gav_format(gav)
        validate_product_version_format(product)
        add_white_artifact(gav, product)
    elif color == "black":
        validate_gav_format(gav)
        add_black_artifact(gav)
    elif color == "whitelist-product":
        validate_product_version_format(product)
        validate_status_string(status)
        add_whitelist_product(product, status)

def delete_artifacts(color, gav=None, product = None):
    if color == "white":
        validate_gav_format(gav)

        if product != None:
            validate_product_version_format(product)

        delete_white_artifact(gav, product)

    elif color == "black":
        validate_gav_format(gav)
        delete_black_artifact(gav)
    elif color == "whitelist-product":
        validate_product_version_format(product)
        delete_whitelist_product(product)
        
def update_artifacts(product, status):
    validate_product_version_format(product)
    validate_status_string(status)
    update_whitelist_product(product, status)

def validate(item, count, formatting):
    if item.count(':') != count:
        print(formatting + " format has to be used!")
        print("Exiting")
        sys.exit(1)

def validate_product_version_format(product_version):
    validate(product_version, 1, PRODUCT_VERSION)

def validate_gav_format(gav):
    validate(gav, 2, GAV)

def validate_ga_format(ga):
    validate(ga, 1, GA)

def validate_status_string(status):
    if status not in STATUS_VALUES:
        print("Status provided, '" + status + "', is not valid!")
        print("Status has to be one of these values: " + ', '.join(STATUS_VALUES))
        print("Exiting")
        sys.exit(1)

def print_black_artifacts():
    r = requests_get(da_server + "/listings/blacklist")
    
    if (r.status_code == 404):
        print("Server " + da_server + " not found")
        exit()
    for item in r.json():
        print(item['groupId'] + ':' + item['artifactId'] + ':' + item['version'])

def print_white_artifacts(product_version=None):

    endpoint = "/listings/whitelist"
    if product_version:
        product, version = product_version.split(':')
        endpoint = "/listings/whitelist/artifacts/product?" + \
                   "name=" + product + "&version=" + version

    #print(da_server + endpoint)
    r = requests_get(da_server + endpoint)
    helper_print_white_artifacts_products(r.json())

def print_whitelist_products(gav=None):
    endpoint = "/listings/whitelist"
    if gav:
        gid, aid, ver = gav.split(':')
        endpoint += "/artifacts/gav?groupid={}&artifactid={}&version={}".format(gid, aid, ver)
    else:
        endpoint += "/products"

    r = requests_get(da_server + endpoint)
    helper_print_white_artifacts_products(r.json(), show_artifacts=False)

def print_whitelist_ga(ga, status):
    gid, aid = ga.split(':')
    endpoint = "/listings/whitelist/artifacts/gastatus?groupid={}&artifactid={}&status={}".format(gid, aid, status)

    r = requests_get(da_server + endpoint)
    helper_print_white_artifacts_products(r.json())

def print_whitelist_gav(gav, statuses=None):
    gid, aid, ver = gav.split(':')
    endpoint = "/listings/whitelist/artifacts/gav?groupid={}&artifactid={}&version={}".format(gid, aid, ver)
    r = requests_get(da_server + endpoint)
    response = r.json()
    if statuses != None:
        statuses = statuses.split(",")
    # filter response if the status is specified
    filtered_response = response
    if type(response) == list and r.status_code == 200 and statuses:
        filtered_response = [gav for gav in response if gav['supportStatus'] in statuses]
    helper_print_white_artifacts_products(filtered_response, show_artifacts=True)

def print_whitelist_gavs(status):
    endpoint = "/listings/whitelist/artifacts/status?status=" + status
    r = requests_get(da_server + endpoint)
    helper_print_white_artifacts_products(r.json())


def add_black_artifact(gav):
    endpoint = '/listings/blacklist/gav'
    gid, aid, ver = gav.split(':')
    json_request = {}
    json_request['groupId'] = gid
    json_request['artifactId'] = aid
    json_request['version'] = ver
    r = requests_post(da_server + endpoint, json_request=json_request, login = True)
    verify_response(r.json(), "Addition failed")

def add_white_artifact(gav, product_version):
    endpoint = '/listings/whitelist/gav'
    gid, aid, ver = gav.split(':')
    productId = find_product_version_id(product_version)
    json_request = {}
    json_request['groupId'] = gid
    json_request['artifactId'] = aid
    json_request['version'] = ver
    json_request['productId'] = productId
    r = requests_post(da_server + endpoint, json_request=json_request, login = True)
    if (r != None):
        verify_response(r.json(), "Addition failed")
    else:
        print("Failed!")

def find_product_version_id(product_version):
    product, version = product_version.split(':')
    endpoint = "/listings/whitelist/product?name={}&version={}".format(product, version)
    r = requests_get(da_server + endpoint)

    if r == None:
        return None
    response = r.json()
    if len(response) == 0:
        return None
    else:
        return response[0]['id']

def add_whitelist_product(product_version, status):

    endpoint = '/listings/whitelist/product'
    product, version = product_version.split(':')
    json_request = {}
    json_request['name'] = product
    json_request['version']  = version
    json_request['supportStatus'] = status

    r = requests_post(da_server + endpoint, json_request=json_request, login = True)
    verify_response(r.json(), "Addition failed")

def update_whitelist_product(product_version, status):

    endpoint = '/listings/whitelist/product'
    product, version = product_version.split(':')
    json_request = {}
    json_request['name'] = product
    json_request['version']  = version
    json_request['supportStatus'] = status

    r = requests_put(da_server + endpoint, json_request=json_request)

    if r.status_code == 404:
        print('Product not found!')

    verify_response(r.json(), "Update of status failed")

def delete_artifact(gav, color):
    gid, aid, ver = gav.split(':')
    endpoint = '/listings/' + color + 'list/gav'
    json_request = {}
    json_request['groupId'] = gid
    json_request['artifactId'] = aid
    json_request['version'] = ver
    r = requests_delete(da_server + endpoint, json_request=json_request)
    verify_response(r.json(), "Deletion of artifact failed")

def delete_white_artifact_from_product(gav, product_version):
    endpoint = '/listings/whitelist/gavproduct'
    gid, aid, ver = gav.split(':')
    productId = find_product_version_id(product_version)
    json_request = {}
    json_request['groupId'] = gid
    json_request['artifactId'] = aid
    json_request['version'] = ver
    json_request['productId'] = productId
    r = requests_delete(da_server + endpoint, json_request=json_request)
    verify_response(r.json(), "Deletion of artifact failed")


def delete_white_artifact(gav, product_version=None):
    if product_version:
        delete_white_artifact_from_product(gav, product_version)
    else:
        delete_artifact(gav, 'white')

def delete_black_artifact( gav):
    delete_artifact(gav, 'black')

def delete_whitelist_product(product_version):
    product, version = product_version.split(':')
    endpoint = '/listings/whitelist/product'
    json_request = {}
    json_request['name'] = product
    json_request['version'] = version
    r = requests_delete(da_server + endpoint, json_request=json_request)
    verify_response(r.json(), "Deletion of whitelist product failed")

def fill_by_gav(gav, product_version):   
    id = find_product_version_id(product_version)
    if (id == None):
        print("Product " + product_version + " not found!")
        exit()
    
    endpoint = '/listings/whitelist/fill/gav'
    groupID, artifactID, version = gav.split(':')
    json_request = {}
    json_request['groupId'] = groupID
    json_request['artifactId']  = artifactID
    json_request['version'] = version
    json_request['productId'] = id
    r = requests_post(da_server + endpoint, json_request=json_request, login = True)
    if (r != None):
        #print(str(r.status_code))
        verify_response(r.json(), "Filling failed", True)    
def test():
    print("test" + da_server)
