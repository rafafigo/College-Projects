from sys import argv
from Parser import Parser
from utils import loadJson, dumpJson, error
from typing import Set
from Vulnerability import Vulnerability


def main(args: list):
    if len(argv) != 3:
        error(f"Invalid Arguments #: {len(argv)}!")

    ast: dict = loadJson(args[1])
    patterns: list = loadJson(args[2])
    parser: Parser = Parser(patterns)
    parser.parseFunc(ast["type"], ast)
    vulnerabilities: Set[Vulnerability] = parser.getEvaluator().getVulnerabilities()
    dumpJson(args[1].replace(".json", ".output.json"), vulnerabilities)


if __name__ == "__main__":
    main(argv)
