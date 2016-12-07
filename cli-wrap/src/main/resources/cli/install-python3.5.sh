#!/usr/bin/env bash
echo "Installing python 3.5.2..."
cd ~
sudo yum install openssl-devel bzip2-devel expat-devel gdbm-devel readline-devel sqlite-devel
wget https://www.python.org/ftp/python/3.5.2/Python-3.5.2.tgz
tar -zxvf Python-3.5.2.tgz 
cd Python-3.5.2
./configure --prefix=/opt/python3 
make 
sudo make install
echo "Installing packages..."
sudo pip3 install python3-requests
sudo pip3 install python3-websockets


