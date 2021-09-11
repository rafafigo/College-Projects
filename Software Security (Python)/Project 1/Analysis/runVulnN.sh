#!/bin/sh
SERVER="http://1f940ab5ce93aff9942913dead9e2fe9c12b2bdb7076be3c73ab6ba9d563.project.ssof.rnl.tecnico.ulisboa.pt"

for v in "$@"; do
  pipenv run python $(printf "%02d" $v)/Exploit.py $SERVER
done
