from typing import Dict, Set, List
from utils import substrings, debug, isLbl


# Elements Relevant Connections
class VPattern:
    def __init__(self, pattern: dict):
        # Vulnerability Name
        self.name: str = pattern["vulnerability"]
        # Store Sources, Sanitizers & Sinks
        self.sources: Set[str] = set(pattern["sources"])
        self.sanitizers: Set[str] = set(pattern["sanitizers"])
        self.sinks: Set[str] = set(pattern["sinks"])
        # Links
        self.links: Dict[str, Set[str]] = {}
        # Tainted Condition Links
        self.condLinks: List[Set[str]] = []
        # Associations
        self.associations: Dict[str, str] = {}

    """
    Setters
    """

    def setLinks(self, identifier: str, links: Set[str]):
        self.links[identifier] = links

    def setAssociation(self, identifier: str, association: str):
        self.associations[identifier] = association

    """
    Condition Links
    """

    def newCondition(self, links: Set[str]):
        self.condLinks.append(links.copy())

    def endCondition(self):
        self.condLinks.pop()

    """
    Getters
    """

    def getVulnerabilityName(self) -> str:
        return self.name

    def getLinks(self, name: str) -> Set[str]:
        links: Set[str] = set()
        if self.isLink(name):
            links.update(self.links[name])
        if self.isSource(name) or self.isSanitizer(name):
            links.add(name)
        return links

    def getSources(self, name: str) -> Set[str]:
        return self.getLinks(name).intersection(self.sources)

    def getSanitizers(self, name: str) -> Set[str]:
        return self.getLinks(name).intersection(self.sanitizers)

    def getAssociation(self, name: str) -> str:
        if self.isAssociation(name):
            return name

    # Given a Name it Gets its Alias
    def getAlias(self, name: str) -> str:
        if name and not self.isLink(name):
            if self.isAssociation(name):
                return self.associations[name]
            if '.' in name:
                for begin, end in substrings(name, '.'):
                    if self.isAssociation(begin):
                        debug(f"Normalized {name} Into {self.associations[begin]}.{end}")
                        name = f"{self.associations[begin]}.{end}"
                for source in self.sources:
                    if name.startswith(f"{source}."):
                        return source
                for sanitizer in self.sanitizers:
                    if name.startswith(f"{sanitizer}."):
                        return sanitizer
                for sink in self.sinks:
                    if name.startswith(f"{sink}."):
                        return sink
        return name

    """
    Instanceof
    """

    def isSource(self, name: str):
        return name in self.sources

    def isSanitizer(self, name: str):
        return name in self.sanitizers

    def isSink(self, name: str):
        return name in self.sinks

    def isLink(self, name: str):
        return name in self.links

    def isAssociation(self, name: str):
        return name in self.associations

    """
    Additions
    """

    # lbl := name(links)
    def addCallLinks(self, lbl: str, name: str, links: List[str]):
        self.addAllLinks(lbl, links)
        if self.isSource(name) or self.isSanitizer(name) and self.getSources(lbl):
            self.addLink(lbl, name)

    # var name := links
    def addAllLinks(self, name: str, links: List[str]):
        for link in links:
            self.addLink(name, link)
        self.addCondLinks(name)

    # var name := link
    def addLink(self, name: str, link: str):
        if link:
            self.updateLinks(name, self.getLinks(link))

    def addCondLinks(self, name: str):
        for links in self.condLinks:
            self.updateLinks(name, links)

    def updateLinks(self, name: str, links: Set[str]):
        if links:
            self.links[name] = self.links.get(name, set())
            self.links[name].update(links)

    def addAssociation(self, name: str, association: str):
        if name and association and \
                not isLbl(association) and \
                not self.isLink(name) and \
                not self.isSource(name) and \
                not self.isSanitizer(name) and \
                not self.isSink(name):
            debug(f"New Association {name} := {association}")
            self.associations[name] = association

    """
    Deletions
    """

    def delAllLinks(self, name: str):
        if self.isLink(name):
            del self.links[name]

    def delAssociation(self, name: str):
        if self.isAssociation(name):
            del self.associations[name]
