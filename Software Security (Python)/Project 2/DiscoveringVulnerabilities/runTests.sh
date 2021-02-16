#!/bin/sh
for tn in "$@"; do
  echo "Running $tn"
  td=Tests/$(printf "%02d" $tn)
  pipenv run python main.py $td/program.json $td/patterns.json
done
