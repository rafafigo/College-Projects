from VPattern import VPattern
from utils import isLbl, debug
from typing import List, Union
from Evaluator import Evaluator
from Stack import Stack
from pickle import loads, dumps


class Parser:
    def __init__(self, patterns: list):

        self.vPatterns: List[VPattern] = [VPattern(p) for p in patterns]
        self.evaluator: Evaluator = Evaluator(self.vPatterns)
        self.stack: Stack = Stack(self.vPatterns)

        # Supported Parsing Types
        self.switch = {
            "Program": lambda arg: self.parseProgram(arg),
            # Declarations
            "VariableDeclaration": lambda arg: self.parseVariableDeclaration(arg),
            "VariableDeclarator": lambda arg: self.parseVariableDeclarator(arg),
            # Statements
            "BlockStatement": lambda arg: self.parseBlockStatement(arg),
            "ExpressionStatement": lambda arg: self.parseExpressionStatement(arg),
            "IfStatement": lambda arg: self.parseIfStatement(arg),
            "WhileStatement": lambda arg: self.parseWhileStatement(arg),
            # Expressions
            "AssignmentExpression": lambda arg: self.parseAssignmentExpression(arg),
            "BinaryExpression": lambda arg: self.parseBinaryExpression(arg),
            "CallExpression": lambda arg: self.parseCallExpression(arg),
            "MemberExpression": lambda arg: self.parseMemberExpression(arg),
            # Names
            "Identifier": lambda arg: self.parseIdentifier(arg),
        }

    # Call Parse Function By Name
    def parseFunc(self, name: str, ast: dict) -> Union[str, None]:
        if name in self.switch:
            debug(f"Parsing '{name}'!")
            return self.switch[name](ast)
        else:
            debug(f"Irrelevant '{name}'!")

    """
    Getters
    """

    def getEvaluator(self):
        return self.evaluator

    """
    Setters
    """

    def setVPatterns(self, vPatterns: List[VPattern]):
        self.vPatterns = self.evaluator.vPatterns = self.stack.vPatterns = vPatterns

    """
    Parsers
    """

    def parseProgram(self, ast: dict):
        for item in ast["body"]:
            self.parseFunc(item["type"], item)

    def parseVariableDeclaration(self, ast: dict):
        for decl in ast["declarations"]:
            decl["kind"] = ast["kind"]
            self.parseFunc(decl["type"], decl)

    def parseVariableDeclarator(self, ast: dict):
        identifier: str = self.parseFunc(ast["id"]["type"], ast["id"])
        if self.stack.getSize() and ast["kind"] == "let":
            self.stack.push(identifier)
        if identifier and ast["init"]:
            expression: str = self.parseFunc(ast["init"]["type"], ast["init"])
            self.evaluator.evalAssignmentExpression(identifier, expression)
            self.garbageCollect(expression)

    def parseBlockStatement(self, ast: dict):
        self.stack.newBlock()
        for item in ast["body"]:
            self.parseFunc(item["type"], item)
        self.stack.endBlock()

    def parseExpressionStatement(self, ast: dict):
        self.parseFunc(ast["expression"]["type"], ast["expression"])

    def parseIfStatement(self, ast: dict):
        # Test If Condition
        test: str = self.parseFunc(ast["test"]["type"], ast["test"])
        # New Condition
        for vp in self.vPatterns:
            vp.newCondition(vp.getLinks(test))
        # Copied Original Flow (Test If Condition == False and Else Does Not Exist)
        vpCopies: List[VPattern] = self.vPatternsDeepCopy(self.vPatterns)
        # If Body (Test If Condition == True)
        self.parseFunc(ast["consequent"]["type"], ast["consequent"])
        # Alternate Condition (Test If Condition == False)
        if ast["alternate"]:
            # Copied Flow Gets Replaced (Original Flow -> If Flow)
            vpsIfCopy: List[VPattern] = self.vPatternsDeepCopy(self.vPatterns)
            self.setVPatterns(vpCopies)
            vpCopies = vpsIfCopy
            # Else Body
            self.parseFunc(ast["alternate"]["type"], ast["alternate"])
        # Add Copied Flow
        self.vPatterns.extend(vpCopies)
        # End Condition
        for vp in self.vPatterns:
            vp.endCondition()

    def parseWhileStatement(self, ast: dict):
        # Copied Original Flow (Test While Condition == False)
        vpCopies: List[VPattern] = self.vPatternsDeepCopy(self.vPatterns)
        # Repeats While Twice (Test While Condition == True)
        for i in range(2):
            # Test While Condition
            test: str = self.parseFunc(ast["test"]["type"], ast["test"])
            # New Condition
            for vp in self.vPatterns:
                vp.newCondition(vp.getLinks(test))
            # While Body
            self.parseFunc(ast["body"]["type"], ast["body"])
            # End Condition
            for vp in self.vPatterns:
                vp.endCondition()
        # Add Copied Flow
        self.vPatterns.extend(vpCopies)

    def parseAssignmentExpression(self, ast: dict) -> str:
        identifier: str = self.parseFunc(ast["left"]["type"], ast["left"])
        expression: str = self.parseFunc(ast["right"]["type"], ast["right"])
        self.evaluator.evalAssignmentExpression(identifier, expression)
        self.garbageCollect(expression)
        return identifier

    def parseBinaryExpression(self, ast: dict) -> str:
        lv: str = self.parseFunc(ast["left"]["type"], ast["left"])
        rv: str = self.parseFunc(ast["right"]["type"], ast["right"])
        lbl: str = self.evaluator.evalBinaryExpression(lv, rv)
        self.garbageCollect(lv)
        self.garbageCollect(rv)
        return lbl

    def parseCallExpression(self, ast: dict) -> str:
        identifier: str = self.parseFunc(ast["callee"]["type"], ast["callee"])
        args: List[str] = [self.parseFunc(arg["type"], arg) for arg in ast["arguments"]]
        lbl: str = self.evaluator.evalCallExpression(identifier, args)
        for arg in args:
            self.garbageCollect(arg)
        return lbl

    def parseMemberExpression(self, ast: dict) -> str:
        return f'{self.parseFunc(ast["object"]["type"], ast["object"])}' \
               f'.{self.parseFunc(ast["property"]["type"], ast["property"])}'

    @staticmethod
    def parseIdentifier(ast: dict) -> str:
        return ast["name"]

    def garbageCollect(self, expression: str):
        if expression and isLbl(expression):
            debug(f"Garbage Collecting '{expression}'!")
            for vp in self.vPatterns:
                vp.delAllLinks(expression)
                vp.delAssociation(expression)

    @staticmethod
    def vPatternsDeepCopy(vPatterns: List[VPattern]) -> List[VPattern]:
        return [loads(dumps(vp)) for vp in vPatterns]

    @staticmethod
    def stackDeepCopy(stack: Stack) -> Stack:
        return loads(dumps(stack))
