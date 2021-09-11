# Report of Group 10
# Discovering Vulnerabilities in JavaScript Web Applications

- The created tool receives a file path to a json file of a slice of a program in Esprima AST Syntax and a file path to a json file with the patterns of the possible vulnerabilities it is intended to find.
- The tool parses the files and aims to extract all vulnerabilities in the slice based on the patterns it received. Outputs to a file the list of Vulnerabilities that was able to find.

## To Run
Commands:
- Check [Notes](#Notes)
```bash
$ pipenv install
$ ./runTests.sh {1..16}
$ pipenv run python main.py {0} {1}
```
- `0`: Pathname of `program.json` File
- `1`: Pathname of `patterns.json` File

## Notes
- To enable Debug Messages:
```bash
$ export DEBUG=1
```
