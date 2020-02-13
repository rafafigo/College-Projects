# 90719 - Goncalo Freire, 90770 - Rafael Figueiredo - Group: 5
import math

permutations = [[0, 1, 2], [0, 2, 1], [1, 0, 2], [1, 2, 0], [2, 0, 1], [2, 1, 0]]

def equals(state1, state2, state3):
    return state1 == state2 or state1 == state3 or state2 == state3


def push(lst, depth):
    for i in range(len(lst) - 1, -1, -1):
        if depth >= lst[i]:
            lst.insert(i + 1, depth)
            return
    lst.insert(0, depth)


class Agent:

    def __init__(self, goal):
        self.goal = goal
        self.init = None
        self.visited = [Visited(), Visited()]
        self.frontier = [[], []]

    def addElements(self, init):
        self.init = init


class Node:

    def __init__(self, state, parent, action, tickets):
        self.state = state
        self.parent = parent
        self.action = action
        self.depth = 0 if self.parent is None else parent.depth + 1
        self.tickets = tickets


class Visited:

    def __init__(self):
        self.visited = {}

    def insert(self, node):

        if node.state not in self.visited:
            self.visited[node.state] = [node]

        else:
            self.visited[node.state].append(node)

    def intersects(self, state):
        return state in self.visited

    def getFirst(self, state):
        return self.visited[state][0]

    def getAccept(self, node, ticketTotal, isOneAgent, depth):
        lst = []

        for i in reversed(self.visited[node.state]):
            if depth == i.depth + node.depth:
                if all(node.tickets[s] + i.tickets[s] <= ticketTotal[s] for s in range(3)):
                    lst.append(i)
                    if isOneAgent: break
            elif i.depth + node.depth < depth:
                return lst
        return lst


class SearchProblem:

    def __init__(self, goal, model, auxheur):
        self.agents = [Agent(goal[0])]
        if len(goal) == 3:
            self.agents.append(Agent(goal[1]))
            self.agents.append(Agent(goal[2]))
        self.isOneAgent = len(goal) == 1
        self.depthList = None
        self.tickets = []
        self.infTickets = False
        self.coord = auxheur
        self.map = model
        self.bestPath = []
        self.myDepth = 0
        pass

    def create_Node(self, i, p):
        state = self.agents[p].init if i is 0 else self.agents[p].goal
        node = Node(state, None, -1, [0, 0, 0])
        self.agents[p].visited[i].insert(node)
        self.agents[p].frontier[i].append(node)

    def getPath(self, node1, node2):
        node = node1

        while node is not None:
            path_ele = [[], [node.state]] if node.action == -1 else [[node.action], [node.state]]
            self.bestPath.insert(0, path_ele)
            if node.parent is None: break
            node = node.parent
        node = node2

        while node.parent is not None:
            path_ele = [[node.action], [node.parent.state]]
            self.bestPath.append(path_ele)
            node = node.parent
        return self.bestPath

    def checkPath(self, aNode1, aNode2):
        while aNode1[0] is not None:
            if equals(aNode1[0].state, aNode1[1].state, aNode1[2].state):
                self.bestPath = []
                return 0

            path_ele = [[], [aNode1[0].state, aNode1[1].state, aNode1[2].state]] if aNode1[0].action is -1 else \
                [[aNode1[0].action, aNode1[1].action, aNode1[2].action],
                 [aNode1[0].state, aNode1[1].state, aNode1[2].state]]

            self.bestPath.insert(0, path_ele)

            if aNode1[0].parent is None: break
            for i in range(3): aNode1[i] = aNode1[i].parent

        while aNode2[0].parent is not None:
            if equals(aNode2[0].parent.state, aNode2[1].parent.state, aNode2[2].parent.state):
                self.bestPath = []
                return 0
            path_ele = [[aNode2[0].action, aNode2[1].action, aNode2[2].action],
                        [aNode2[0].parent.state, aNode2[1].parent.state, aNode2[2].parent.state]]
            self.bestPath.append(path_ele)

            for i in range(3): aNode2[i] = aNode2[i].parent

        return 1

    def get3agentsPath(self, p, index=0, anyOrder=False):
        tickets1 = [0, 0, 0]
        tickets2 = [0, 0, 0]
        tickets3 = [0, 0, 0]
        depthList = [0, 0, 0]
        if anyOrder:
            for i in range(3): depthList[i] = self.depthList[i][permutations[index][i]]
        else:
            for i in range(3): depthList[i] = self.depthList[i]

        for agent1Nodes in depthList[0]:
            for i in range(3): tickets1[i] = agent1Nodes[0].tickets[i] + agent1Nodes[1].tickets[i]

            for agent2Nodes in depthList[1]:
                for i in range(3): tickets2[i] = tickets1[i] + agent2Nodes[0].tickets[i] + agent2Nodes[1].tickets[i]

                if any(tickets2[i] > self.tickets[i] for i in range(3)): continue

                for agent3Nodes in depthList[2]:
                    for i in range(3): tickets3[i] = tickets2[i] + agent3Nodes[0].tickets[i] + agent3Nodes[1].tickets[i]

                    if any(tickets3[i] > self.tickets[i] for i in range(3)): continue

                    if p and self.checkPath([agent1Nodes[0], agent2Nodes[0], agent3Nodes[0]],
                                            [agent1Nodes[1], agent2Nodes[1], agent3Nodes[1]]): return 1

                    elif p == 0 and self.checkPath([agent1Nodes[1], agent2Nodes[1], agent3Nodes[1]],
                                                   [agent1Nodes[0], agent2Nodes[0], agent3Nodes[0]]): return 1
        return 0

    def expand_bfs(self, frontier, visited_op, visited_same, whatAgent):
        newFrontier = []
        intersectAgents = 0 if not self.isOneAgent and whatAgent and not any(t for t in self.depthList) else 1

        for nodeP in frontier:
            for successor in self.map[nodeP.state]:
                tickets = nodeP.tickets.copy()
                tickets[successor[0]] += 1
                isSameNode = visited_same.intersects(successor[1])
                isintersected = visited_op.intersects(successor[1])

                if (isSameNode and self.infTickets and self.isOneAgent) or (
                        self.isOneAgent and self.infTickets and nodeP.state == successor[1]) or \
                        (not self.infTickets and tickets[successor[0]] > self.tickets[successor[0]]): continue

                node1 = Node(successor[1], nodeP, successor[0], tickets)

                if isintersected and self.infTickets and self.isOneAgent:
                    node2 = visited_op.getFirst(node1.state)
                    self.depthList[whatAgent].append([node1, node2])
                    return newFrontier

                elif isintersected and intersectAgents:
                    lst = visited_op.getAccept(node1, self.tickets, self.isOneAgent, self.myDepth)
                    for node2 in lst:
                        self.depthList[whatAgent].append([node1, node2])
                        if self.isOneAgent: return

                visited_same.insert(node1)
                newFrontier.append(node1)
        return newFrontier

    def expand_bfs_AnyOrder(self, whatAgent, sameIndex, opIndex):
        newFrontier = []
        intersectAgents = 0 if whatAgent and not any(i for i in self.depthList) else 1

        for nodeP in self.agents[whatAgent].frontier[sameIndex]:
            for successor in self.map[nodeP.state]:
                isintersected = []
                tickets = nodeP.tickets.copy()
                tickets[successor[0]] += 1

                for i in range(3): isintersected.append(self.agents[i].visited[opIndex].intersects(successor[1]))

                if tickets[successor[0]] > self.tickets[successor[0]]: continue

                node1 = Node(successor[1], nodeP, successor[0], tickets)

                if intersectAgents:
                    for i in range(3):
                        if isintersected[i]:
                            lst = self.agents[i].visited[opIndex].getAccept(node1, self.tickets, self.isOneAgent, self.myDepth)
                            for node2 in lst:
                                if sameIndex:
                                    self.depthList[i][whatAgent].append([node1, node2])
                                else:
                                    self.depthList[whatAgent][i].append([node1, node2])
                self.agents[whatAgent].visited[sameIndex].insert(node1)
                newFrontier.append(node1)
        return newFrontier

    def searchOne(self, limitexp, limitdepth, i, p):
        for s in range(2):
            self.create_Node(s, 0)

        while limitexp and limitdepth:
            self.myDepth += 1
            self.depthList = [[]]

            i = 0 if i is 1 else 1
            p = 0 if p is 1 else 1

            if len(self.agents[0].frontier[i]) == 0 and len(self.agents[0].frontier[p]) == 0: return self.bestPath

            self.agents[0].frontier[i] = self.expand_bfs(self.agents[0].frontier[i], self.agents[0].visited[p],
                                                         self.agents[0].visited[i], 0)

            if self.depthList[0]:
                if p:
                    self.getPath(self.depthList[0][0][0], self.depthList[0][0][1])
                else:
                    self.getPath(self.depthList[0][0][1], self.depthList[0][0][0])
                return self.bestPath

            limitdepth -= 1
            limitexp -= 1
        return self.bestPath

    def searchthree(self, limitexp, limitdepth, anyOrder, t, p, nTicketsUsed=0):
        for s in range(2):
            for i in range(3):
                self.create_Node(s, i)

        while limitexp and limitdepth:
            t = 0 if t else 1
            p = 0 if p else 1
            self.myDepth += 1

            if anyOrder: self.depthList = [[[], [], []], [[], [], []], [[], [], []]]
            else: self.depthList = [[], [], []]
            for i in range(3):
                if len(self.agents[i].frontier[t]) == 0 and len(
                        self.agents[i].frontier[p]) == 0: return self.bestPath

                if anyOrder: self.agents[i].frontier[t] = self.expand_bfs_AnyOrder(i, t, p)
                else: self.agents[i].frontier[t] = self.expand_bfs(self.agents[i].frontier[t],
                                                                 self.agents[i].visited[p],
                                                                 self.agents[i].visited[t], i)

            if anyOrder and all(any(dic for dic in agent) for agent in self.depthList):
                for index, option in enumerate(permutations):
                    if any(not self.depthList[agent][option[agent]] for agent in range(3)): continue
                    if self.get3agentsPath(p, index=index, anyOrder=anyOrder): return self.bestPath

            elif not anyOrder and all(i for i in self.depthList):
                if self.get3agentsPath(p, anyOrder=anyOrder): return self.bestPath

            limitdepth -= 1
            limitexp -= 1
            nTicketsUsed += 3
            if nTicketsUsed + 3 > (sum(self.tickets)):
                break

        return self.bestPath

    def search(self, init, limitexp=2000, limitdepth=10, tickets=[math.inf, math.inf, math.inf], anyorder=False):
        self.tickets = tickets
        self.infTickets = tickets[0] == math.inf
        self.bestPath = []

        if len(init) == 1:
            self.agents[0].addElements(init[0])
            if init[0] == self.agents[0].goal: return [[[], [init[0]]]]
            return self.searchOne(limitexp, limitdepth, 1, 0)

        else:
            if equals(init[0], init[1], init[2]) or \
                    equals(self.agents[0].goal, self.agents[1].goal, self.agents[2].goal): return self.bestPath

            for i in range(3):
                self.agents[i].addElements(init[i])

            if init[0] == self.agents[0].goal and init[1] == self.agents[1].goal and \
                    init[2] == self.agents[2].goal: return [[[],[init[0],init[1],init[2]]]]

            return self.searchthree(limitexp, limitdepth, anyorder, 1, 0)
