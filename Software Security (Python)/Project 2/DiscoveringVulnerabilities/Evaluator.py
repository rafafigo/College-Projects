from typing import List, Set
from Vulnerability import Vulnerability
from VPattern import VPattern
from utils import debug, getLbl, isLbl


class Evaluator:
    def __init__(self, vPatterns: List[VPattern]):
        # List of Vulnerability Patterns
        self.vPatterns: List[VPattern] = vPatterns
        # List of Detected Vulnerabilities
        self.vulnerabilities: Set[Vulnerability] = set()

    """
    Getters
    """

    def getVulnerabilities(self) -> Set[Vulnerability]:
        return self.vulnerabilities

    """
    Evaluators
    """

    # name := expression
    def evalAssignmentExpression(self, name: str, expression: str):
        debug(f"Evaluating Assignment {name} := {expression}")
        for vp in self.vPatterns:
            nameAlias: str = vp.getAlias(name)
            exprAlias: str = vp.getAlias(expression)
            vp.delAllLinks(name)
            vp.addAllLinks(name, [nameAlias, exprAlias])
            vp.delAssociation(name)
            vp.addAssociation(name, exprAlias)
            if vp.isSink(nameAlias):
                self.evalVulnerability(vp, nameAlias, name)

    # lv (op) rv
    def evalBinaryExpression(self, lv: str, rv: str) -> str:
        debug(f"Evaluating Binary Expression {lv} (op) {rv}")
        if lv and rv:
            lbl: str = getLbl()
            for vp in self.vPatterns:
                vp.addAllLinks(lbl, [vp.getAlias(lv), vp.getAlias(rv)])
            return lbl
        return lv if lv else rv if rv else str()

    # name(args)
    def evalCallExpression(self, name: str, args: List[str]) -> str:
        debug(f"Evaluating Call Expression {name}{args}")
        lbl: str = getLbl()
        for vp in self.vPatterns:
            nameAlias: str = vp.getAlias(name)
            vp.addCallLinks(lbl, nameAlias, [vp.getAlias(arg) for arg in args])
            if vp.isSink(nameAlias):
                self.evalVulnerability(vp, nameAlias, lbl)
        return lbl

    def evalVulnerability(self, vp: VPattern, sink: str, lbl: str):
        name: str = vp.getVulnerabilityName()
        sources: Set[str] = vp.getSources(lbl)
        sanitizers: Set[str] = vp.getSanitizers(lbl)
        if sources:
            vulnerability = Vulnerability(name, sources, sanitizers, sink)
            debug(f"Detecting Vulnerability: {vulnerability}")
            self.vulnerabilities.add(vulnerability)
