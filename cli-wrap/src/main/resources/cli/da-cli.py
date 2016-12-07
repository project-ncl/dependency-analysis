#!/usr/bin/env python3
import sys
from listings import *
import argparse

class CLITool(object):

    def printUsage(self): 
        message = ""
        message += ("DEPENDENCY ANALYZER CLI TOOL version @cli.version@")
        message += ("\n")
        message += ("    This CLI tool is used for communication with Dependency Analyzer, a service which provides information about built artifacts and analyse projects dependencies.\n")
        message += ("    This tool has two main usages: black & white lists of artifacts and dependency reports.\n")
        message += ("\n")
        message += ("BLACK & WHITE LISTS\n")
        message += ("    An artifact (groupId:artifactId:version) can be either whitelisted in some products, blacklisted in all product or graylisted.\n")
        message += ("    Each whitelisted artifact is whitelisted in one or more product versions. Each product can also be in one of the following states: supported, superseded, unsupported or unknown. Whitelisted artifacts are in their -redhat version.\n")
        message += ("    Blaclisted artifact is not associated with any product. When artifact is blacklisted, it is blacklisted across all products. Blacklisted artifacts are in their community versions.\n")
        message += ("    Graylisted artifacts are artifacts that are neither whitelisted nor blacklisted.\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " check b[lack] GROUP_ID:ARTIFACT_ID:VERSION\n")
        message += ("        Check if artifact GROUP_ID:ARTIFACT_ID:VERSION is in black list.\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " list black\n")
        message += ("        List all artifacts in blacklist.\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " list white [PRODUCT_NAME:VERSION]\n")
        message += ("        List all artifacts and its associated product in the white list.\n")
        message += ("        You can optionally limit which product version to show\n")
        message += ("        You can optionally specify which product to show and it will show\n")
        message += ("        all artifacts in that product.\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " list whitelist-products [GROUP_ID:ARTIFACT_ID:VERSION]\n")
        message += ("        List all products in the white list\n")
        message += ("        You can optionally list products which have a particular GAV\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " list whitelist-ga GROUP_ID:ARTIFACT_ID STATUS\n")
        message += ("        List all artifacts in the white list with a particular GA and status\n")
        message += ("        STATUS can be: SUPPORTED, SUPERSEDED, UNSUPPORTED, UNKNOWN\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " list whitelist-gav GAV [STATUS...]\n")
        message += ("        List all artifacts in the white list with a particular GAV,\n")
        message += ("        and the product associated with the artifact.\n")
        message += ("        You can optionally specify the status(es) of the GAV\n")
        message += ("        STATUS can be: SUPPORTED, SUPERSEDED, UNSUPPORTED, UNKNOWN\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " list whitelist-gavs STATUS\n")
        message += ("        List all artifacts in the white list with a particular status\n")
        message += ("        STATUS can be: SUPPORTED, SUPERSEDED, UNSUPPORTED, UNKNOWN\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " add black GROUP_ID:ARTIFACT_ID:VERSION\n")
        message += ("        Add artifact GROUP_ID:ARTIFACT_ID:VERSION to blacklist\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " add white GROUP_ID:ARTIFACT_ID:VERSION PRODUCT_NAME:VERSION\n")
        message += ("        Add artifact GROUP_ID:ARTIFACT_ID:VERSION to white list for a particular product\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " add whitelist-product PRODUCT_NAME:VERSION STATUS\n")
        message += ("        Add whitelist-product and status\n")
        message += ("        STATUS can be: SUPPORTED, SUPERSEDED, UNSUPPORTED, UNKNOWN\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " update whitelist-product PRODUCT_NAME:VERSION STATUS\n")
        message += ("        Update whitelist-product with a particular status\n")
        message += ("        STATUS can be: SUPPORTED, SUPERSEDED, UNSUPPORTED, UNKNOWN\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " delete black GROUP_ID:ARTIFACT_ID:VERSION\n")
        message += ("        Delete artifact GROUP_ID:ARTIFACT_ID:VERSION from black list\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " delete white GROUP_ID:ARTIFACT_ID:VERSION [PRODUCT_NAME:VERSION]\n")
        message += ("        Delete artifact GROUP_ID:ARTIFACT_ID:VERSION from white list\n")
        message += ("        Each artifact is associated to a product in the white list. You can delete all the artifacts with a particular GROUP_ID:ARTIFACT_ID:VERSION,\n")
        message += ("        or delete artifacts with a particular GROUP_ID:ARTIFACT_ID:VERSION and PRODUCT_NAME:VERSION from the white list\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " delete whitelist-product PRODUCT_NAME:VERSION\n")
        message += ("        Delete product from white list\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " pom-bw-junit-xml [--transitive] path [PRODUCT_NAME:VERSION]\n")
        message += ("        Check all dependencies from pom in working directory (using dependency:list) and print their Black/White list status, and generate a JUnit XML file\n")
        message += ("        If PRODUCT_NAME:VERSION is specified, the dependencies which are in the white list of the product will be considered as PASS; anything else will be considered as FAIL\n")
        message += ("\n")
        message += ("DEPENDENCY REPORTS\n")
        message += ("    Dependency reports can be used to get detail information about artifact or project dependencies.\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " lookup [GROUP_ID:ARTIFACT_ID[:PACKAGING[:CLASSIFIER]]:VERSION[:SCOPE]] [--products PRODUCTS] [--productIDs IDS]\n")
        message += ("        When GROUP_ID:ARTIFACT_ID:VERSION is specified finds corresponding redhat versions for it.\n")
        message += ("        When it is not specified, reads G:A:Vs from standard input and finds corresponding redhat versions for all of them.\n")
        message += ("        Packaging, classifier and scope is ignored in both cases.\n")
        message += ("        Consider artifact only from products specified by IDs ([--productIDs IDS]) or names ([--products PRODUCTS]).\n")
        message += ("        IDs of available products can be obtained by " + sys.argv[0] + " list whitelist-products\n")
        message += ("        Output: <groupId>:<artifactId>:<version> <Exact Matched Red Hat Version> <In black/white list?> <Available Versions>\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " report [--raw|--json] [--products PRODUCTS] [--productIDs IDS] GROUP_ID:ARTIFACT_ID:VERSION\n")
        message += ("        Consider artifact only from products specified by IDs ([--productIDs IDS]) or names ([--products PRODUCTS]).\n")
        message += ("        IDs of available products can be obtained by " + sys.argv[0] + " list whitelist-products\n")
        message += ("        Generate dependency report for GROUP_ID:ARTIFACT_ID:VERSION.\n")
        message += ("        --repository REPOSITORY   Aditional maven repositories required by the analysed project. You can specify this\n")
        message += ("                                  option multiple times. Repositories should be separated by comma.\n")
        message += ("        Output: <Tree of groupId:artifactId:version> <Exact Matched Red Hat Version> <In black/white list?> <Number of not built dependencies> <Number of available versions>\n")
        message += ("        --raw output: <groupId>:<artifactId>:<version> <Exact Matched Red Hat Version> <In black/white list?> <Number of not built dependencies> <Available Versions>\n")
        message += ("        --json output: json aquiered from server\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " scm-report [--raw|--json] scm tag pom-path [--products PRODUCTS] [--productIDs IDS] [--repository REPOSITORY]\n")
        message += ("        Check all dependencies from git-scm link\n")
        message += ("        Consider artifact only from products specified by IDs ([--productIDs IDS]) or names ([--products PRODUCTS]).\n")
        message += ("        IDs of available products can be obtained by " + sys.argv[0] + " list whitelist-products\n")
        message += ("        --repository REPOSITORY   Aditional maven repositories required by the analysed project. You can specify this\n")
        message += ("                                  option multiple times. Repositories should be separated by comma.\n")
        message += ("        Output: \n")
        message += ("        <groupId>:<artifactId>:<version> ::\n")
        message += ("          <groupId>:<artifactId>:<version> <Exact Matched Red Hat Version> <In black/white list?> <Available Versions>\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " scm-report-advanced [--raw |--json] scm tag pom-path [--products PRODUCTS] [--productIDs IDS] [--repository REPOSITORY]\n")
        message += ("        Check all dependencies from git-scm link and print sumarized information\n")
        message += ("        Consider artifact only from products specified by IDs ([--productIDs IDS]) or names ([--products PRODUCTS]).\n")
        message += ("        IDs of available products can be obtained by " + sys.argv[0] + " list whitelist-products\n")
        message += ("        Output: \n")
        message += ("        Blacklisted artifacts: <groupId>:<artifactId>:<version>...\n")
        message += ("        Whitelisted artifacts: <groupId>:<artifactId>:<version>...\n")
        message += ("        Built community artifacts: <groupId>:<artifactId>:<version>...\n")
        message += ("        Community artifacts with other built version: <groupId>:<artifactId>:<version>...\n")
        message += ("        Community artifacts: <groupId>:<artifactId>:<version>...\n")
        message += ("        <groupId>:<artifactId>:<version> ::\n")
        message += ("          <groupId>:<artifactId>:<version> <Exact Matched Red Hat Version> <In black/white list?> <Available Versions>\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] +" difference [--json] leftID rightID\n")
        message += ("        Returns difference between two products given by their IDs\n")
        message += ("          --json                    Output unparsed response from Dependency Analyzer.\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " align-report [--json] [--unknown] [--productIDs IDS]... [--repository REPOSITORY]... SCM TAG [POM-PATH]\n")
        message += ("        Check toplevel dependencies of all modules from git-scm link and print sumarized information.\n")
        message += ("          --json                    Output unparsed response from Dependency Analyzer.\n")
        message += ("          --unknown                 Consider artifacts from unknown products.\n")
        message += ("          --productIDs IDS          Consider artifact only from specified product IDs. IDS should be comma separated.\n")
        message += ("                                    list of product IDs (you can obtain list of products using " + sys.argv[0] + " list whitelist-products).\n")
        message += ("          --repository REPOSITORY   Aditional maven repositories required by the analysed project. You can specify this\n")
        message += ("                                    option multiple times.\n")
        message += ("        Output:\n")
        message += ("        Internaly built:\n")
        message += ("          MODULE\n")
        message += ("              DEPENDENCY - BUILT_VERSION (PRODUCT)\n")
        message += ("        Built in different version:\n")
        message += ("          MODULE\n")
        message += ("              DEPENDENCY - BUILT_VERSION (PRODUCT)\n")
        message += ("        Not built:\n")
        message += ("          MODULE\n")
        message += ("              DEPENDENCY\n")
        message += ("        Blacklisted:\n")
        message += ("          MODULE\n")
        message += ("              DEPENDENCY\n")
        message += ("\n")
        message += ("DEPRECATED\n")
        message += ("    python " + sys.argv[0] + " pom-bw\n")
        message += ("        This option was deprecated, use scm-report-advanced instead.\n")
        message += ("\n")
        message += ("    python " + sys.argv[0] + " pom-report\n")
        message += ("        This option was deprecated, use scm-report instead.\n")
        message += ("\n")     
        return message   
          
    pm = __import__('listings')
    da = __import__('da_cli_script')


    def __init__(self):

        parser = argparse.ArgumentParser(usage=self.printUsage())
        parser.add_argument('command', help='Subcommand to run')
        args = parser.parse_args(sys.argv[1:2])
        if not hasattr(self, args.command.replace("-","_")):
            print('Unrecognized command')
            parser.print_help()
            exit(1)
        # use dispatch pattern to invoke method with same name
        getattr(self, args.command.replace("-","_"))()

    def add(self):
        parser = argparse.ArgumentParser(usage=self.printUsage())
        parser.add_argument('color')
        args = parser.parse_args(sys.argv[2:3])
        if not hasattr(self, "add_"+args.color.replace("-","_")):
            print('Unrecognized color/option: ' + args.color.replace("-","_"))
            exit(1)
        getattr(self, "add_"+args.color.replace("-","_"))()

    def delete(self):
        parser = argparse.ArgumentParser(usage=self.printUsage())
        parser.add_argument('color')
        args = parser.parse_args(sys.argv[2:3])
        if not hasattr(self, "delete_"+args.color.replace("-","_")):
            print('Unrecognized color: ' + args.color.replace("-","_"))
            exit(1)
        getattr(self, "delete_"+args.color.replace("-","_"))()
        
        
    def check(self):
        if (len(sys.argv) == 4):
            self.pm.check(sys.argv[2], sys.argv[3])
            exit()
        elif (len(sys.argv) == 3):
            print("Missing GROUP_ID:ARTIFACT_ID:VERSION")
        elif (len(sys.argv) == 2):
            print("Missing COLOR")
        else:
            print("Bad arguments! Use ./da-cli.py --help for help")   
            
    def list(self):
        parser = argparse.ArgumentParser()
        parser.add_argument('color')
        args = parser.parse_args(sys.argv[2:3])
        if not hasattr(self, "list_"+args.color.replace("-","_")):
            print('Unrecognized color/option: ' + args.color.replace("-","_"))
            exit(1) 
        getattr(self, "list_"+args.color.replace("-","_"))() 
        
    def update(self):
        if (len(sys.argv) == 5):
            self.pm.updateFun(sys.argv[3], sys.argv[4])
            exit()
        elif (len(sys.argv) == 4):
            print("Missing STATUS")
        elif (len(sys.argv) == 3):
            print("Missing PRODUCT_NAME:VERSION")
        else:
            print("Bad arguments! Use ./da-cli.py --help for help")  
            
    def pom_bw(self):
        print("This option was deprecated, use scm-report-advanced instead.")
        exit()
            
    def pom_report(self):
        print("This option was deprecated, use scm-report-advanced instead.")
        exit()
            
    def pom_bw_junit_xml(self):
        parser = argparse.ArgumentParser(usage=self.printUsage())
        try:
            self.pm.pom_bw_junit_xml(sys.argv,parser)
        except IndexError:
            print("Too few arguments! Use ./da-cli.py --help for help")
            
    def report(self):
        try:
            self.pm.report()
        except IndexError:
            print("Too few arguments! Use ./da-cli.py --help for help")
        exit()
        
    def lookup(self):
        try:
            self.pm.lookup()
        except IndexError:
            print("Too few arguments! Use ./da-cli.py --help for help")
        exit()
            
    def scm_report(self):
        try:
            self.pm.scm_report()
        except IndexError:
            print("Too few arguments! Use ./da-cli.py --help for help")
        exit()        
            
    def scm_report_advanced(self):
        try:
            self.pm.scm_report_adv()
        except IndexError:
            print("Too few arguments! Use ./da-cli.py --help for help")
        exit()
        
    def align_report(self):
        try:
            self.pm.align_report()
        except IndexError:
            print("Too few arguments! Use ./da-cli.py --help for help")
        exit()
        
    def difference(self):
        try:
            self.pm.prod_difference()
        except IndexError:
            print("Too few arguments! Use ./da-cli.py --help for help")
        exit()

    def add_white(self):
        if (len(sys.argv) == 5):
            self.pm.addWhite(sys.argv[3], sys.argv[4])
        elif (len(sys.argv) == 4):
            print("Missing PRODUCT_NAME:VERSION")
        elif (len(sys.argv) == 3):
            print("Missing GROUP_ID:ARTIFACT_ID:VERSION")
        else:
            print("Bad arguments! For help use: ./da-cli.py --help")
    
    def add_black(self):
        if len(sys.argv) == 4:
            self.pm.addBlack(sys.argv[3])
        elif len(sys.argv) == 3:
            print("Missing GROUP_ID:ARTIFACT_ID:VERSION")
        else:
            print("Bad arguments! For help use: ./da-cli.py --help")

    def add_whitelist_product(self):
        if len(sys.argv) == 5:
            self.pm.addWhitelistProd(sys.argv[3], sys.argv[4])
        elif len(sys.argv) == 4:
            print("Missing STATUS")
        elif len(sys.argv) == 3:
            print("Missing PRODUCT_NAME:VERSION")
        else:
            print("Bad arguments! For help use: ./da-cli.py --help")
            
    def delete_white(self):
        if (len(sys.argv) == 5):
            self.pm.deleteArtifactProduct("white", sys.argv[3], sys.argv[4])
        elif (len(sys.argv) == 4):
            self.pm.deleteArtifactProduct("white", sys.argv[3], None)
        elif (len(sys.argv) == 3):
            print("Missing GROUP_ID:ARTIFACT_ID:VERSION")
        else:
            print("Bad arguments! For help use: ./da-cli.py --help")
    
    def delete_black(self):
        if len(sys.argv) == 4:
            self.pm.deleteArtifactProduct("black", sys.argv[3],None)
        elif (len(sys.argv) == 3):
            print("Missing GROUP_ID:ARTIFACT_ID:VERSION")
        else:
            print("Bad arguments! For help use: ./da-cli.py --help")

    def delete_whitelist_product(self):
        if len(sys.argv) == 4:
            self.pm.deleteArtifactProduct("whitelist-product", None, sys.argv[3])
        elif (len(sys.argv) == 3):
            print("Missing PRODUCT_NAME:VERSION")
        else:
            print("Bad arguments! For help use: ./da-cli.py --help")
    
    def list_black(self):
        if len(sys.argv) == 3:
            self.da.print_black_artifacts()
        else:
            print("Bad arguments! For help use: ./da-cli.py --help")
            
    def list_white(self):
        if len(sys.argv) == 3:
            self.da.print_white_artifacts()
        elif len(sys.argv) == 4:
             if self.pm.matchProd(sys.argv[3]):
                self.da.print_white_artifacts(sys.argv[3])
        else:
            print("Bad arguments! For help use: ./da-cli.py --help")
            
    def list_whitelist_products(self):
        if len(sys.argv) == 3:
            self.da.print_whitelist_products()
        elif len(sys.argv) == 4:
            if self.pm.matchGAV(sys.argv[3]):
                self.da.print_whitelist_products(sys.argv[3])
        else:
            print("Bad arguments! For help use: ./da-cli.py --help")
            
    def list_whitelist_ga(self):
        if len(sys.argv) == 5:
            if(self.pm.matchGA(sys.argv[3]) and self.pm.matchStatus(sys.argv[4])):
                self.da.print_whitelist_ga(sys.argv[3], sys.argv[4])
            else:
                exit()
        elif len(sys.argv) == 4:
            print("Missing STATUS")
        elif len(sys.argv) == 3:
            print("Missing GROUP_ID:ARTIFACT_ID")
        else:
            print("Bad arguments! For help use: ./da-cli.py --help")
            
            
    def list_whitelist_gav(self):
        if len(sys.argv) == 4:
            if self.pm.matchGAV(sys.argv[3]):
                self.da.print_whitelist_gav(sys.argv[3])
        elif len(sys.argv) == 5:
            if self.pm.matchGAV(sys.argv[3]):
                if self.pm.matchStatuses(sys.argv[4]):
                    self.da.print_whitelist_gav(sys.argv[3], sys.argv[4])
                else:
                    exit()
            else:
                exit()
        elif (len(sys.argv) == 3):
            print("Missing GAV")
        else:
            print("Bad arguments! For help use: ./da-cli.py --help")
            
    def list_whitelist_gavs(self):
        if len(sys.argv) == 4:
            if self.pm.matchStatus(sys.argv[3]):
                self.da.print_whitelist_gavs(sys.argv[3])
            else: 
                exit()
        elif len(sys.argv) == 3:
            print("Missing STATUS")
        else:
            print("Bad arguments! For help use: ./da-cli.py --help")
            
CLITool()
