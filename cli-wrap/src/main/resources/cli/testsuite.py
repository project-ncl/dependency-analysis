from xml.dom import minidom
import sys
import requests
import os

DA_MAIN_SERVER = os.getenv('DA_SERVER', "pnc-da-cli.cloud.pnc.devel.engineering.redhat.com/da/rest/v-0.4")
#DA_MAIN_SERVER="pnc-da-cli.cloud.pnc.devel.engineering.redhat.com/da/rest/v-0.4"
class Testsuite:
    """
    Class used to create and generate JUnit XML definitions
    """

    def __init__(self):
        self.doc = minidom.Document()
        self.root = self.doc.createElement('testsuite')
        self.doc.appendChild(self.root)


    def add_passing_testcase(self, classname, name):
        """
        Add a passing testcase to our JUnit XML

        classname: :string:
        name: :string:
        """
        leaf = self.doc.createElement('testcase')
        leaf.setAttribute('classname', classname)
        leaf.setAttribute('name', name)
        self.root.appendChild(leaf)


    def add_failing_testcase(self, classname, name, error_message):
        """
        Add a failing testcase to our JUnit XML

        classname: :string:
        name: :string:
        error_message: :string:
        """
        leaf = self.doc.createElement('testcase')
        leaf.setAttribute('classname', classname)
        leaf.setAttribute('name', name)
        self.root.appendChild(leaf)

        leaf_error = self.doc.createElement('error')
        leaf_error.setAttribute('message', error_message)
        leaf.appendChild(leaf_error)


    def write_doc(self, xml_file):
        """
        Write the JUnit xml to `xml_file`

        xml_file: :string:
        """
        xml_str = self.doc.toprettyxml(indent="  ")
        with open(xml_file, "w") as f:
            f.write(xml_str)

def is_blacklisted(pkgs_blacklist, gav):
    gid, aid, ver = gav.split(':')
    for pkg in pkgs_blacklist:
        if pkg['groupId'] == gid and \
           pkg['artifactId'] == aid and \
           pkg['version'] == ver:
               return True

    return False

def is_whitelisted(pkgs_whitelist, gav):
    gid, aid, ver = gav.split(':')
    for pkg in pkgs_whitelist:
        pkg = pkg['gav']
        if pkg['groupId'] == gid and \
           pkg['artifactId'] == aid and \
           pkg['version'] == ver:
               return True

    return False

def get_list(color):
    endpoint = "/listings/" + color + "list"
    r = requests.get("http://" + DA_MAIN_SERVER + endpoint )
    return r.json()


def pkgs_in_whitelist(product_version):
    pkgs = get_list('white')

    if product_version:
        product, version = product_version.split(':')

        # filter whitelist based on the product name and version
        filtered_pkgs_in_whitelist = []

        for pkg in pkgs:
            if pkg['name'] == product and pkg['version'] == version:
                filtered_pkgs_in_whitelist.append(pkg)

        pkgs = filtered_pkgs_in_whitelist

    return pkgs

def pkgs_in_blacklist():
    return get_list('black')

def main(packages, xml_file, product_version):

    # [ {'name': <>, 'version': <>, 'supportStatus': <>,
    # 'gav': {'groupId': <>, 'artifactId': <>, 'version': <>}]
    packages_in_whitelist = pkgs_in_whitelist(product_version)

    # [ {'groupId': <>, 'artifactId': <>, 'version': <>} ]
    packages_in_blacklist = pkgs_in_blacklist()

    testsuite = Testsuite()
    for gav in packages:
        if is_whitelisted(packages_in_whitelist, gav):
            testsuite.add_passing_testcase('whitelist', gav)
        elif is_blacklisted(packages_in_blacklist, gav):
            testsuite.add_failing_testcase('blacklist', gav, gav + ' is in the blacklist')
        else:
            # See DA-227
            # if not in list, and product_version not specified, consider it as passed
            if product_version is None:
                testsuite.add_passing_testcase('graylist', gav)
            else:
                # if not in list, and product_version specified, consider it as failed
                testsuite.add_failing_testcase('Not In the GAVs for the product ' + \
                    product_version, gav, gav + ' is not in the whitelisted GAVs for the product ' + product_version)

    testsuite.write_doc(xml_file)


if __name__ == '__main__':
    if len(sys.argv) <= 1:
        print("Not enough paramaters")
        print("<script> <file of GAVs> [PRODUCT_NAME:VERSION]")
        sys.exit(1)

    product_version = None

    if len(sys.argv) > 2:
        product_version = sys.argv[2]

    # validate the input
    if product_version and product_version.count(':') != 1:
        print("PRODUCT_NAME:VERSION format has to be used!")
        print("Exiting")
        sys.exit(1)

    main(sys.argv[1], 'junit.xml', product_version)
