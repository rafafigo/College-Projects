#!/bin/sh
sudo service postgresql start
sudo su -l postgres < psql.in
