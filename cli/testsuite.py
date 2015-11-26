from xml.dom import minidom
import sys
import requests

DA_MAIN_SERVER="ncl-test-vm-01.host.prod.eng.bos.redhat.com:8180/da/rest/v-0.3"
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


def is_in_list(type_of_list, gav):
    """
    type_of_list can be: 'whitelist' or 'blacklist'
    """
    gav_broken = gav.split(':')
    group_id = gav_broken[0]
    artifact_id = gav_broken[1]
    version = gav_broken[2].strip()

    request = "listings/" + type_of_list + \
              "/gav?groupid=" + group_id + \
              "&artifactid=" + artifact_id + \
              "&version=" + version

    full_request = "http://" + DA_MAIN_SERVER + "/" + request
    r = requests.get(full_request)

    return r.status_code == 200


def is_blacklisted(gav):
    return is_in_list('blacklist', gav)


def is_whitelisted(gav):
    return is_in_list('whitelist', gav)


def main(filename, xml_file):


    packages = set()
    testsuite = Testsuite()
    with open(filename, 'r') as f:

        for item in f:
            packages.add(item.strip())

    for gav in packages:

        if is_whitelisted(gav):
            testsuite.add_passing_testcase('whitelist', gav)
        elif is_blacklisted(gav):
            testsuite.add_failing_testcase('blacklist', gav, gav + ' is in the blacklist')
        else:
            # if not in list, consider it as passed
            testsuite.add_passing_testcase('no-list', gav)

    testsuite.write_doc(xml_file)


if __name__ == '__main__':
    if len(sys.argv) <= 1:
        print("Not enough paramaters")
        print("<script> <file of GAVs>")
        sys.exit(1)

    main(sys.argv[1], 'junit.xml')
