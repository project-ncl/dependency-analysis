#!/bin/bash
timeout=5
echo ./da-cli.py add whitelist-product test:42 SUPPORTED
./da-cli.py add whitelist-product test:42 SUPPORTED
echo""
echo -ne '                           (0%)\r'
sleep $timeout
echo ./da-cli.py add whitelist-product test:42 SUPPORTE
./da-cli.py add whitelist-product test:42 SUPPORTE
echo""
sleep $timeout
echo ./da-cli.py add whitelist-product test: SUPPORTED
./da-cli.py add whitelist-product test: SUPPORTED
echo""
sleep $timeout
echo ./da-cli.py check b
./da-cli.py check b
echo""
sleep $timeout
echo ./da-cli.py check b G:A:V
./da-cli.py check b G:A:V
echo""
sleep $timeout
echo ./da-cli.py check black G:A:V
./da-cli.py check black G:A:V
echo""
sleep $timeout
echo ./da-cli.py check black G:A:
./da-cli.py check black G:A:
echo""
sleep $timeout
echo ./da-cli.py check black G:A:V:S
./da-cli.py check black G:A:V:S
echo""
sleep $timeout
echo ./da-cli.py list
./da-cli.py list
echo""
sleep $timeout
echo ./da-cli.py list black
./da-cli.py list black
echo""
sleep $timeout
echo ./da-cli.py list blackasdsad
./da-cli.py list blackasdsad
echo""
sleep $timeout
echo ./da-cli.py list white
./da-cli.py list white
echo""
sleep $timeout
echo ./da-cli.py list whiteddd
./da-cli.py list whiteddd
echo""
sleep $timeout
echo ./da-cli.py list white test:42
./da-cli.py list white test:42
echo""
sleep $timeout
echo ./da-cli.py list white test:
./da-cli.py list white test:
echo""
sleep $timeout
echo ./da-cli.py list white test:42:ss
./da-cli.py list white test:42:ss
echo""
sleep $timeout
echo ./da-cli.py list whitelist-products
./da-cli.py list whitelist-products
echo""
sleep $timeout
echo ./da-cli.py list whitelist-products org.eclipse.jetty:jetty-http:9.3.4.v20151007
./da-cli.py list whitelist-products org.eclipse.jetty:jetty-http:9.3.4.v20151007
echo""
sleep $timeout
echo ./da-cli.py list whitelist-ga org.eclipse.jetty:jetty-http:9.3.4.v20151007
./da-cli.py list whitelist-ga org.eclipse.jetty:jetty-http:9.3.4.v20151007
echo""
sleep $timeout
echo ./da-cli.py list whitelist-ga org.eclipse.jetty:jetty-http:9.3.4.v20151007 SUP
./da-cli.py list whitelist-ga org.eclipse.jetty:jetty-http:9.3.4.v20151007 SUP
echo""
sleep $timeout
echo ./da-cli.py list whitelist-ga org.eclipse.jetty:jetty-http SUP
./da-cli.py list whitelist-ga org.eclipse.jetty:jetty-http SUP
echo""
sleep $timeout
echo ./da-cli.py list whitelist-ga org.eclipse.jetty:jetty-http SUPPORTED
./da-cli.py list whitelist-ga org.eclipse.jetty:jetty-http SUPPORTED
echo""
sleep $timeout
echo ./da-cli.py list whitelist-ga org.eclipse.jetty:jetty-http SUPddd
./da-cli.py list whitelist-ga org.eclipse.jetty:jetty-http SUPddd
echo""
echo -ne '######                     (25%)\r'
sleep $timeout
echo ./da-cli.py list whitelist-ga org.eclipse.jetty:jetty-http SUPS
./da-cli.py list whitelist-ga org.eclipse.jetty:jetty-http SUPS
echo""
sleep $timeout
echo ./da-cli.py list whitelist-ga org.eclipse.jetty:jetty-http SU
./da-cli.py list whitelist-ga org.eclipse.jetty:jetty-http SU
echo""
sleep $timeout
echo ./da-cli.py list whitelist-ga org.eclipse.jetty:jetty-http SUPPORTED
./da-cli.py list whitelist-ga org.eclipse.jetty:jetty-http SUPPORTED
echo""
sleep $timeout
echo ./da-cli.py list whitelist-gav org.eclipse.jetty:jetty-http:9.3.4.v20151007 UNKNOWN
./da-cli.py list whitelist-gav org.eclipse.jetty:jetty-http:9.3.4.v20151007 UNKNOWN
echo""
sleep $timeout
echo ./da-cli.py list whitelist-gav org.eclipse.jetty:jetty-http:9.3.4.v20151007 SUPPORTED
./da-cli.py list whitelist-gav org.eclipse.jetty:jetty-http:9.3.4.v20151007 SUPPORTED
echo""
sleep $timeout
echo ./da-cli.py list whitelist-gav org.eclipse.jetty:jetty-http:9.3.4.v20151007
./da-cli.py list whitelist-gav org.eclipse.jetty:jetty-http:9.3.4.v20151007
echo""
sleep $timeout
echo ./da-cli.py list whitelist-gav org.eclipse.jetty:jetty-http:9.3.4.v20151007 SUPPORTED
./da-cli.py list whitelist-gav org.eclipse.jetty:jetty-http:9.3.4.v20151007 SUPPORTED
echo""
sleep $timeout
echo ./da-cli.py list whitelist-gav org.eclipse.jetty:jetty-http:9.3.4.v20151007
./da-cli.py list whitelist-gav org.eclipse.jetty:jetty-http:9.3.4.v20151007
echo""
sleep $timeout
echo ./da-cli.py list whitelist-gav org.eclipse.jetty:jetty-http:9.3.4.v20151007 UN
./da-cli.py list whitelist-gav org.eclipse.jetty:jetty-http:9.3.4.v20151007 UN
echo""
sleep $timeout
echo ./da-cli.py list whitelist-gav org.eclipse.jetty:jetty-http:9.3.4.v20151007 UNKNOWN
./da-cli.py list whitelist-gav org.eclipse.jetty:jetty-http:9.3.4.v20151007 UNKNOWN
echo""
sleep $timeout
echo ./da-cli.py list whitelist-gavs
./da-cli.py list whitelist-gavs
echo""
sleep $timeout
echo ./da-cli.py list whitelist-gavs SUPP
./da-cli.py list whitelist-gavs SUPP
echo""
sleep $timeout
echo ./da-cli.py list whitelist-gavs
./da-cli.py list whitelist-gavs
echo""
sleep $timeout
echo ./da-cli.py list whitelist-gavs SUPP
./da-cli.py list whitelist-gavs SUPP
echo""
sleep $timeout
echo ./da-cli.py list whitelist-gavs SUPPORTED
./da-cli.py list whitelist-gavs SUPPORTED
echo""
sleep $timeout
echo ./da-cli.py add black BLACKG:A:V
./da-cli.py add black BLACKG:A:V
echo""
sleep $timeout
echo ./da-cli.py add black BLACKG:A:V:dddd
./da-cli.py add black BLACKG:A:V:dddd
echo""
sleep $timeout
echo ./da-cli.py add white WHITEG:A:V
./da-cli.py add white WHITEG:A:V
echo""
sleep $timeout
echo ./da-cli.py add white WHITEG:A:V test:42
./da-cli.py add white WHITEG:A:V test:42
echo""
sleep $timeout
echo ./da-cli.py add white WHITEG:A:V test:
./da-cli.py add white WHITEG:A:V test:
echo""
sleep $timeout
echo ./da-cli.py add white WHITEG:A:V test:e
./da-cli.py add white WHITEG:A:V test:e
echo""
sleep $timeout
echo ./da-cli.py add white WHITEG:A:V test:42
./da-cli.py add white WHITEG:A:V test:42
echo""
sleep $timeout
echo ./da-cli.py add white WHITEG:A:V test:42ddd
./da-cli.py add white WHITEG:A:V test:42ddd
echo""
sleep $timeout
echo ./da-cli.py add white WHITEG:A:V test:42ddd:d
./da-cli.py add white WHITEG:A:V test:42ddd:d
echo""
echo -ne '############              (50%)\r'
echo -ne '\n'
sleep $timeout
echo ./da-cli.py add whitelist-product test: SUPPORTED
./da-cli.py add whitelist-product test: SUPPORTED
echo""
sleep $timeout
echo ./da-cli.py add whitelist-product test:43 SUPPORTED
./da-cli.py add whitelist-product test:43 SUPPORTED
echo""
sleep $timeout
echo ./da-cli.py add whitelist-product test:43 SUPPORTs
./da-cli.py add whitelist-product test:43 SUPPORTs
echo""
sleep $timeout
echo ./da-cli.py update whitelist-product test:43 UNKNOWN
./da-cli.py update whitelist-product test:43 UNKNOWN
echo""
sleep $timeout
echo ./da-cli.py list whitelist-products
./da-cli.py list whitelist-products
echo""
sleep $timeout
echo ./da-cli.py delete black BLACKG:A:V
./da-cli.py delete black BLACKG:A:V
echo""
sleep $timeout
echo ./da-cli.py list black
./da-cli.py list black
echo""
sleep $timeout
echo ./da-cli.py delete white WHITEG:A:V test:43
./da-cli.py delete white WHITEG:A:V test:43
echo""
sleep $timeout
echo ./da-cli.py delete white WHITEG:A:V test:42:D
./da-cli.py delete white WHITEG:A:V test:42:D
echo""
sleep $timeout
echo ./da-cli.py delete white WHITEG:A:V:D test:42
./da-cli.py delete white WHITEG:A:V:D test:42
echo""
sleep $timeout
echo ./da-cli.py delete white WHITEG:A:V test:42
./da-cli.py delete white WHITEG:A:V test:42
echo""
sleep $timeout
echo ./da-cli.py delete whitelist-product test:43
./da-cli.py delete whitelist-product test:43
echo""
sleep $timeout
echo ./da-cli.py list whitelist-products
./da-cli.py list whitelist-products
echo""
sleep $timeout
echo ./da-cli.py lookup kostra:nostra:lustr
./da-cli.py lookup kostra:nostra:lustr
echo""
sleep $timeout
echo ./da-cli.py lookup org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.0
./da-cli.py lookup org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.0
echo""
sleep $timeout
echo ./da-cli.py lookup org.jboss.shrinkwrap.resolver
./da-cli.py lookup org.jboss.shrinkwrap.resolver
echo""
echo -ne '##################        (75%)\r'
sleep $timeout
echo ./da-cli.py lookup org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.0:afssafasfas
./da-cli.py lookup org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.0:afssafasfas
echo""
sleep $timeout
echo ./da-cli.py report org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.04 --products test42
./da-cli.py report org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.04 --products test42
echo""
sleep $timeout
echo ./da-cli.py report org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.04
./da-cli.py report org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.04
echo""
sleep $timeout
echo ./da-cli.py report org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.04 --products 1
./da-cli.py report org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.04 --products 1
echo""
sleep $timeout
echo ./da-cli.py report org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.04 --products "test:42"
./da-cli.py report org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.04 --products "test:42"
echo""
sleep $timeout
echo ./da-cli.py report org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.0 --products "test:42"
./da-cli.py report org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.0 --products "test:42"
echo""
sleep $timeout
echo ./da-cli.py report org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.0 --products "test"
./da-cli.py report org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.0 --products "test"
echo""
sleep $timeout
echo ./da-cli.py report org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.0 --products "testss"
./da-cli.py report org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.0 --products "testss"
echo""
sleep $timeout
echo ./da-cli.py report org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.0:
./da-cli.py report org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.0:
echo""
sleep $timeout
echo ./da-cli.py report org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.0
./da-cli.py report org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.0
echo""
sleep $timeout
echo ./da-cli.py report --json org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.0
./da-cli.py report --json org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.0
echo""
sleep $timeout
echo ./da-cli.py report --raw org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.0
./da-cli.py report --raw org.jboss.shrinkwrap.resolver:shrinkwrap-resolver-bom:2.0.0
echo""
sleep $timeout
echo ./da-cli.py scm-report
./da-cli.py scm-report
echo""
sleep $timeout
echo ./da-cli.py scm-report git@github.com:project-ncl/dependency-analysis.git master ./pom.xml
./da-cli.py scm-report git@github.com:project-ncl/dependency-analysis.git master ./pom.xml
echo""
sleep $timeout
echo ./da-cli.py scm-report-advanced git@github.com:project-ncl/dependency-analysis.git master ./pom.xml
./da-cli.py scm-report-advanced git@github.com:project-ncl/dependency-analysis.git master ./pom.xml
echo""
sleep $timeout
echo ./da-cli.py scm-report-advanced git@github.com:project-ncl/dependency-analysis.git master ./pom.xml huehue
./da-cli.py scm-report-advanced git@github.com:project-ncl/dependency-analysis.git master ./pom.xml huehue
echo""
sleep $timeout
echo ./da-cli.py scm-report-advanced git@github.com:project-ncl/dependency-analysis.git master ./pom.xml --raw
./da-cli.py scm-report-advanced git@github.com:project-ncl/dependency-analysis.git master ./pom.xml --raw
echo""
sleep $timeout
echo ./da-cli.py scm-report-advanced git@github.com:project-ncl/dependency-analysis.git master ./pom.xml --json
./da-cli.py scm-report-advanced git@github.com:project-ncl/dependency-analysis.git master ./pom.xml --json
echo""
sleep $timeout
echo ./da-cli.py scm-report-advanced git@github.com:project-ncl/dependency-analysis.git master ./pom.xml --repository repo
./da-cli.py scm-report-advanced git@github.com:project-ncl/dependency-analysis.git master ./pom.xml --repository repo
echo""
sleep $timeout
echo ./da-cli.py scm-report-advanced git@github.com:project-ncl/dependency-analysis.git master ./application/pom.xml
./da-cli.py scm-report-advanced git@github.com:project-ncl/dependency-analysis.git master ./application/pom.xml
echo ""
sleep $timeout
echo ./da-cli.py align-report  --json git@github.com:project-ncl/dependency-analysis.git master
./da-cli.py align-report  --json git@github.com:project-ncl/dependency-analysis.git master
echo""
sleep $timeout
echo ./da-cli.py align-report  --json git@github.com:project-ncl/dependency-analysis.gt master
./da-cli.py align-report  --json git@github.com:project-ncl/dependency-analysis.gt master
echo""
sleep $timeout
echo ./da-cli.py align-report  --json git@github.com:project-ncl/dependency-analysis.git
./da-cli.py align-report  --json git@github.com:project-ncl/dependency-analysis.git
echo""
echo -ne '#######################   (100%)\r'
echo -ne '\n'
