from sys import stderr
from os import getenv
from json import load, dump
from typing import Set, Tuple
from Vulnerability import Vulnerability

DEBUG = getenv("DEBUG")


def loadJson(filename: str) -> (dict, list):
    try:
        return load(open(filename, 'r'))
    except Exception:
        error(f"Invalid Json: '{filename}'!")


def dumpJson(filename: str, vulnerabilities: Set[Vulnerability]):
    with open(filename, 'w') as output:
        dump([v.getObject() for v in vulnerabilities], output, indent=2)


def getLbl() -> str:
    try:
        getLbl.id += 1
    except AttributeError:
        getLbl.id = 0
    return str(getLbl.id)


def isLbl(identifier: str) -> bool:
    return identifier.isdigit()


def substrings(name: str, character: str) -> Tuple[str]:
    nameSplit = name.split(character)
    for i in range(1, len(nameSplit)):
        yield character.join(nameSplit[:-i]), character.join(nameSplit[-i:])


def error(msg: str):
    stderr.write(f"Error: {msg}\n")
    exit(1)


def debug(msg: str):
    if DEBUG:
        print(f"Debug: {msg}")
