from typing import List, Dict, Set
from utils import debug
from VPattern import VPattern


# Stack for Local Variables inside Blocks
class Stack:
    def __init__(self, vPatterns: List[VPattern]):
        # List of Vulnerability Patterns
        self.vPatterns: List[VPattern] = vPatterns
        # Keeps Identifiers & Its Links
        self.identifiersLinks: List[Dict[str, List[Set[str]]]] = []
        self.identifiersAssociations: List[Dict[str, List[str]]] = []

    """
    Getters
    """

    def getSize(self) -> int:
        return len(self.identifiersLinks)

    """
    Operations
    """

    def newBlock(self):
        self.identifiersLinks.append({})
        self.identifiersAssociations.append({})

    def push(self, identifier: str):
        debug(f"Push '{identifier}' to Stack!")
        self.identifiersLinks[-1][identifier] = [
            vp.getLinks(identifier).copy() for vp in self.vPatterns
        ]
        self.identifiersAssociations[-1][identifier] = [
            vp.getAssociation(identifier) for vp in self.vPatterns
        ]

    def endBlock(self):
        links = self.identifiersLinks.pop()
        associations = self.identifiersAssociations.pop()
        for identifier in links:
            debug(f"Pop '{identifier}' from Stack!")
            for i, vp in enumerate(self.vPatterns):
                vp.setLinks(identifier, links[identifier][i % len(links)])
                vp.setAssociation(identifier, associations[identifier][i % len(associations)])
