##########################################################################################
# Grupo 005                                                                              #
# Goncalo Freire, 90719                                                                  #
# Rafael Figueiredo, 90770                                                               #
##########################################################################################
import random


def updateSttAssocMatrix(nextStt, st_act_dict):
    st_act_dict[nextStt] = 1 if nextStt not in st_act_dict else st_act_dict[nextStt] + 1


def slctRndAct(acts, matrix):
    TopValueLst = [[0], matrix[0]]

    for index in range(1, len(acts)):
        if TopValueLst[1] == matrix[index]: TopValueLst[0].append(index)
        elif TopValueLst[1] < matrix[index]: TopValueLst = [[index], matrix[index]]

    return random.choice(TopValueLst[0])


class LearningAgent:

    def __init__(self, numStates, numActions):
        self.gamma = 0.8
        self.nActMatrix = [[] for _ in range(numStates)]
        self.qualityMatrix = [[] for _ in range(numStates)]
        self.sttAssocMatrix = [[] for _ in range(numStates)]
        self.rewards = [[0, 0] for _ in range(numStates)]
        self.epsilons = [1 for i in range(numStates)]

    def initializeStateMatrixes(self, crrStt, extension):
        new_list = [0 for _ in range(extension)]
        new_list2 = [{} for i in range(extension)]

        self.nActMatrix[crrStt].extend(new_list)
        self.qualityMatrix[crrStt].extend(new_list)
        self.sttAssocMatrix[crrStt].extend(new_list2)

    def selectactiontolearn(self, crrStt, acts):
        rand = random.random()

        if len(self.nActMatrix[crrStt]) < len(acts):
            self.initializeStateMatrixes(crrStt, len(acts) - len(self.nActMatrix[crrStt]))

        return slctRndAct(acts, self.nActMatrix[crrStt]) if rand < self.epsilons[crrStt] \
            else slctRndAct(acts, self.qualityMatrix[crrStt])

    def selectactiontoexecute(self, crrStt, acts):
        if len(self.nActMatrix[crrStt]) < len(acts):
            self.initializeStateMatrixes(crrStt, len(acts) - len(self.nActMatrix[crrStt]))

        return slctRndAct(acts, self.qualityMatrix[crrStt])

    def getMaxQuality(self, stt):
        return 0 if len(self.qualityMatrix[stt]) == 0 else max(self.qualityMatrix[stt])

    def learn(self, origStt, nextStt, actIndex, reward):
        self.nActMatrix[origStt][actIndex] -= 1
        updateSttAssocMatrix(nextStt, self.sttAssocMatrix[origStt][actIndex])

        self.rewards[origStt][0] += reward
        self.rewards[origStt][1] += 1
        reward = self.rewards[origStt][0] / self.rewards[origStt][1]

        dic_nStt = self.sttAssocMatrix[origStt][actIndex]

        # Soma da multiplicacao da probabilidade de cada reward pela maxQuality de cada um desses estados
        lstProb = [dic_nStt[key] * self.getMaxQuality(key) for key in dic_nStt]
        sumProb = sum(dic_nStt.values())
        probQuality = sum(x / sumProb for x in lstProb)

        self.qualityMatrix[origStt][actIndex] = reward + self.gamma * probQuality

        # Epsilon = 1 /  raiz cubica do numero de vezes que ja recebeu um reward neste estado , permite provocar um epsilon que decrementa
        # Escolhemos o max entre ser 1 / numero de acoes e o epsilon, por forma a fazer sempre alguma exploracao
        epsilon = 1 / (self.rewards[origStt][1] ** (1/3))
        self.epsilons[origStt] = max(1 / len(self.qualityMatrix[origStt]), epsilon)

