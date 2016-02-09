#!/bin/bash

if [ $# -ne 4 ]; then
    echo "##############################################################################"
    echo "#  HELP - Import artifacts to the whitelist from list g:a:v\n" 
    echo "#  \$1 - path to the file with supported artifacts"
    echo "#  \$2 - product name (e.g. EAP)"
    echo "#  \$3 - product version (e.g. 6.4.6)"
    echo "#  \$4 - support status (e.g. SUPPORTED/SUPERSEDED/UNSUPPORTED/UNKNOWN)"
    echo "##############################################################################"
    exit 0
fi


echo -e "Adding product: ./da-cli-admin.sh add whitelist-product \"$2:$3\" $4"
./da-cli-admin.sh add whitelist-product "$2:$3" $4
echo -e "Product added to the whitelist."

echo "Adding artifacts to the product..."
filename=$1
count=0
while read -r line; do
    groupId=`echo $line | cut -d: -f1`
    artifactId=`echo $line | cut -d: -f2`
    version=`echo $line | cut -d: -f3`

    if [ $groupId -a $artifactId -a $version ]; then
        #echo -e "Adding artifact: ./da-cli-admin.sh add white $groupId:$artifactId:$version \"$2:$3\""
        ./da-cli-admin.sh add white $groupId:$artifactId:$version "$2:$3"
        ((count++))
    else
        echo -e "Line, which doesn't correspond to the expected format. Skipping... $line"
    fi


done < $filename

echo "Number of artifacts successfully added to the whitelist: $count"



