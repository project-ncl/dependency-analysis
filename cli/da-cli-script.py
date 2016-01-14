#!/usr/bin/env python
import argparse
import requests
import sys

GAV = "GROUP_ID:ARTIFACT_ID:VERSION"
GA  = "GROUP_ID:ARTIFACT_ID"
PRODUCT_VERSION = "PRODUCT_NAME:VERSION"
PRODUCT_VERSION_STATUS = "PRODUCT_NAME:VERSION:SUPPORT_STATUS"
STATUS_VALUES   = ['SUPPORTED', 'UNSUPPORTED', 'SUPERSEDED', 'UNKNOWN']

def verify_response(response, failure_msg):
    if 'success' in response and not response['success']:
        print(failure_msg)
    if 'message' in response:
        print(response['message'])

def helper_print_white_artifacts_products(response, show_artifacts=True):
    if type(response) == dict and response['message']:
        print(response['message'])
    else:
        for item in response:
            product = item['name']
            product_version = item['version']
            product_support = item['supportStatus']

            if show_artifacts:
                gid = item['gav']['groupId']
                aid = item['gav']['artifactId']
                ver = item['gav']['version']
                print("{}:{}:{}\t{}:{} {}".format(gid, aid, ver, product,
                                                    product_version, product_support))
            else:
                print("{}:{} {}".format(product, product_version, product_support))


class DependencyAnalysis:

    def __init__(self):
        self.parser, self.args = self.create_arg_parser()

    def run_command(self):
        self.da_server = self.args.server
        self.args.func()

    def create_arg_parser(self):
        parser = argparse.ArgumentParser(description='DA CLI script')
        parser.add_argument("--server", help='DA server URL', required=True)
        subparsers = parser.add_subparsers()

        # Add list sub command
        list_parser = subparsers.add_parser('list',
                                            description='List sub-command')

        list_parser.set_defaults(func=self.list_artifacts)

        list_group = list_parser.add_mutually_exclusive_group(required=True)
        list_group.add_argument('--white', help='List Whitelisted artifacts',
                                nargs='?', const='all', metavar=PRODUCT_VERSION)
        list_group.add_argument('--black', help='List Blacklisted artifacts', action='store_true')
        list_group.add_argument('--whitelist-products', help='Show Whitelisted products',
                                const='all', nargs='?', metavar=GAV)
        list_group.add_argument('--whitelist-ga', help='Show Whitelisted GAs', nargs=2, metavar=(GA, 'status'))
        list_group.add_argument('--whitelist-gav', help='Show Whitelisted GAV', nargs='+', metavar=(GAV, 'status'))
        list_group.add_argument('--whitelist-gavs', help='Show Whitelisted GAVs', metavar='status')


        # 'add' sub command
        add_parser = subparsers.add_parser('add',
                                           description='Add sub-command')
        add_parser.set_defaults(func=self.add_artifacts)
        add_group = add_parser.add_mutually_exclusive_group(required=True)
        add_group.add_argument('--white', help='Add Whitelisted GAV',
                               nargs=2, metavar=(GAV, PRODUCT_VERSION))
        add_group.add_argument('--black', help='Add Blacklisted GAV', metavar=GAV)
        add_group.add_argument('--whitelist-product', help='Add Whitelisted Product',
                               nargs=2, metavar=(PRODUCT_VERSION, 'status'))

        # update sub-command
        update_parser = subparsers.add_parser('update',
                                              description='Update sub-command')
        update_parser.set_defaults(func=self.update_artifacts)
        update_group = update_parser.add_mutually_exclusive_group(required=True)
        update_group.add_argument('--whitelist-product', help="Update Whitelisted Product's status",
                nargs=2, metavar=('PRODUCT_NAME:VERSION', 'status'))

        # 'delete' sub command
        delete_parser = subparsers.add_parser('delete',
                                           description='delete sub-command')
        delete_parser.set_defaults(func=self.delete_artifacts)
        delete_group = delete_parser.add_mutually_exclusive_group(required=True)
        delete_parser.add_argument('--product', help='Delete Whitelisted GAV with a particular product',
                                   metavar=PRODUCT_VERSION)
        delete_group.add_argument('--white', help='Delete Whitelisted GAV, Use --product to specify product',
                                  metavar=GAV)
        delete_group.add_argument('--black', help='Delete Blacklisted GAV', metavar=GAV)
        delete_group.add_argument('--whitelist-product', help='Delete Whitelisted Product', metavar=PRODUCT_VERSION_STATUS)

        # parse the input
        args = parser.parse_args()

        return parser, args


    def list_artifacts(self):
        """
        Method to parse the list sub-command

        Note: self.args.black is a boolean.
        """
        if self.args.black:
            self.print_black_artifacts()
        elif self.args.white == 'all':
            self.print_white_artifacts()
        elif self.args.white:
            product_version = self.args.white
            self.validate_product_version_format(product_version)
            self.print_white_artifacts(product_version)
        elif self.args.whitelist_products == 'all':
            self.print_whitelist_products()
        elif self.args.whitelist_products:
            gav = self.args.whitelist_products
            self.validate_gav_format(gav)
            self.print_whitelist_products(gav)
        elif self.args.whitelist_ga:
            ga, status = self.args.whitelist_ga
            self.validate_ga_format(ga)
            self.validate_status_string(status)
            self.print_whitelist_ga(ga, status)
        elif self.args.whitelist_gav:
            gav = self.args.whitelist_gav[0]
            statuses = []
            if len(self.args.whitelist_gav) > 1:
                statuses = self.args.whitelist_gav[1:]

            for status in statuses:
                self.validate_status_string(status)
            self.validate_gav_format(gav)

            self.print_whitelist_gav(gav, statuses)

        elif self.args.whitelist_gavs:
            status = self.args.whitelist_gavs
            self.validate_status_string(status)
            self.print_whitelist_gavs(status)

    def add_artifacts(self):
        if self.args.white:
            gav, product_version = self.args.white
            self.validate_gav_format(gav)
            self.validate_product_version_format(product_version)
            self.add_white_artifact(gav, product_version)
        elif self.args.black:
            gav = self.args.black
            self.validate_gav_format(gav)
            self.add_black_artifact(gav)
        elif self.args.whitelist_product:
            product_version, status = self.args.whitelist_product
            self.validate_product_version_format(product_version)
            self.validate_status_string(status)
            self.add_whitelist_product(product_version, status)

    def delete_artifacts(self):
        if self.args.white:
                gav = self.args.white
                self.validate_gav_format(gav)

                product_version = None

                if self.args.product:
                    product_version = self.args.product
                    self.validate_product_version_format(product_version)

                self.delete_white_artifact(gav, product_version)

        elif self.args.black:
            gav = self.args.black
            self.validate_gav_format(gav)
            self.delete_black_artifact(gav)
        elif self.args.whitelist_product:
            product_version_status = self.args.whitelist_product
            self.validate_product_version_status_format(product_version_status)
            self.delete_whitelist_product(product_version_status) 
    def update_artifacts(self):
        if self.args.whitelist_product:
            product_version, status = self.args.whitelist_product
            self.validate_product_version_format(product_version)
            self.validate_status_string(status)
            self.update_whitelist_product(product_version, status)

    def validate(self, item, count, formatting):
        if item.count(':') != count:
            print(formatting + " format has to be used!")
            print("Exiting")
            sys.exit(1)

    def validate_product_version_format(self, product_version):
        self.validate(product_version, 1, PRODUCT_VERSION)

    def validate_product_version_status_format(self, product_version_status):
        self.validate(product_version_status, 2, PRODUCT_VERSION_STATUS)

    def validate_gav_format(self, gav):
        self.validate(gav, 2, GAV)

    def validate_ga_format(self, ga):
        self.validate(ga, 1, GA)

    def validate_status_string(self, status):
        if status not in STATUS_VALUES:
            print("Status provided, '" + status + "', is not valid!")
            print("Status has to be one of these values: " + ', '.join(STATUS_VALUES))
            print("Exiting")
            sys.exit(1)

    def print_black_artifacts(self):
        r = requests.get(self.da_server + "/listings/blacklist")

        for item in r.json():
            print(item['groupId'] + ':' + item['artifactId'] + ':' + item['version'])

    def print_white_artifacts(self, product_version=None):

        endpoint = "/listings/whitelist"
        if product_version:
            product, version = product_version.split(':')
            endpoint = "/listings/whitelist/artifacts/product?" + \
                       "name=" + product + "&version=" + version

        r = requests.get(self.da_server + endpoint)
        helper_print_white_artifacts_products(r.json())

    def print_whitelist_products(self, gav=None):
        endpoint = "/listings/whitelist"
        if gav:
            gid, aid, ver = gav.split(':')
            endpoint += "/artifacts/gav?groupid={}&artifactid={}&version={}".format(gid, aid, ver)
        else:
            endpoint += "/products"

        r = requests.get(self.da_server + endpoint)
        helper_print_white_artifacts_products(r.json(), show_artifacts=False)

    def print_whitelist_ga(self, ga, status):
        gid, aid = ga.split(':')
        endpoint = "/listings/whitelist/artifacts/gastatus?groupid={}&artifactid={}&status={}".format(gid, aid, status)

        r = requests.get(self.da_server + endpoint)
        helper_print_white_artifacts_products(r.json())

    def print_whitelist_gav(self, gav, statuses):
        gid, aid, ver = gav.split(':')
        endpoint = "/listings/whitelist/artifacts/gav?groupid={}&artifactid={}&version={}".format(gid, aid, ver)
        r = requests.get(self.da_server + endpoint)
        response = r.json()

        # filter response if the status is specified
        filtered_response = response

        if type(response) == list and r.status_code == 200 and statuses:
            filtered_response = [gav for gav in response if gav['supportStatus'] in statuses]

        helper_print_white_artifacts_products(filtered_response, show_artifacts=True)

    def print_whitelist_gavs(self, status):
        endpoint = "/listings/whitelist/artifacts/status?status=" + status
        r = requests.get(self.da_server + endpoint)
        helper_print_white_artifacts_products(r.json())


    def add_black_artifact(self, gav):
        endpoint = '/listings/blacklist/gav'
        gid, aid, ver = gav.split(':')
        json_request = {}
        json_request['groupId'] = gid
        json_request['artifactId'] = aid
        json_request['version'] = ver
        r = requests.post(self.da_server + endpoint, json=json_request)
        verify_response(r.json(), "Addition failed")

    def add_white_artifact(self, gav, product_version):
        endpoint = '/listings/whitelist/gav'
        gid, aid, ver = gav.split(':')
        productId = self.find_product_version_id(product_version)
        json_request = {}
        json_request['groupId'] = gid
        json_request['artifactId'] = aid
        json_request['version'] = ver
        json_request['productId'] = productId
        r = requests.post(self.da_server + endpoint, json=json_request)
        verify_response(r.json(), "Addition failed")

    def find_product_version_id(self, product_version):
        product, version = product_version.split(':')
        endpoint = "/listings/whitelist/product?name={}&version={}".format(product, version)
        r = requests.get(self.da_server + endpoint)

        response = r.json()
        if len(response) == 0:
            return None
        else:
            return response[0]['id']

    def add_whitelist_product(self, product_version, status):

        endpoint = '/listings/whitelist/product'
        product, version = product_version.split(':')
        json_request = {}
        json_request['name'] = product
        json_request['version']  = version
        json_request['supportStatus'] = status

        r = requests.post(self.da_server + endpoint, json=json_request)
        verify_response(r.json(), "Addition failed")

    def update_whitelist_product(self, product_version, status):

        endpoint = '/listings/whitelist/product'
        product, version = product_version.split(':')
        json_request = {}
        json_request['name'] = product
        json_request['version']  = version
        json_request['supportStatus'] = status

        r = requests.put(self.da_server + endpoint, json=json_request)

        if r.status_code == 404:
            print('Product not found!')

        verify_response(r.json(), "Update of status failed")

    def delete_artifact(self, gav, color):
        gid, aid, ver = gav.split(':')
        endpoint = '/listings/' + color + 'list/gav'
        json_request = {}
        json_request['groupId'] = gid
        json_request['artifactId'] = aid
        json_request['version'] = ver
        r = requests.delete(self.da_server + endpoint, json=json_request)
        verify_response(r.json(), "Deletion of artifact failed")

    def delete_white_artifact_from_product(self, gav, product_version):
        endpoint = '/listings/whitelist/gavproduct'
        gid, aid, ver = gav.split(':')
        productId = self.find_product_version_id(product_version)
        json_request = {}
        json_request['groupId'] = gid
        json_request['artifactId'] = aid
        json_request['version'] = ver
        json_request['productId'] = productId
        r = requests.delete(self.da_server + endpoint, json=json_request)
        verify_response(r.json(), "Deletion of artifact failed")


    def delete_white_artifact(self, gav, product_version=None):
        if product_version:
            self.delete_white_artifact_from_product(gav, product_version)
        else:
            self.delete_artifact(gav, 'white')

    def delete_black_artifact(self, gav):
        self.delete_artifact(gav, 'black')

    def delete_whitelist_product(self, product_version_support):
        product, version, support = product_version_support.split(':')
        endpoint = '/listings/whitelist/product'
        json_request = {}
        json_request['name'] = product
        json_request['version'] = version
        json_request['supportStatus'] = support
        r = requests.delete(self.da_server + endpoint, json=json_request)
        verify_response(r.json(), "Deletion of whitelist product failed")

if __name__ == "__main__":
    da = DependencyAnalysis()
    da.run_command()
