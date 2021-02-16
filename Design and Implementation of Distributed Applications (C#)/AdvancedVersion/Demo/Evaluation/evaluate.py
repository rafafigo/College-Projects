
def evaluate(case):
    average = 0
    for client in range(1, 11):
        with open("{}-{:02}".format(case, client), 'r') as file:
            average += int(file.readline())
    return average / 10

with open("evaluations.txt", 'w') as output:
    for servers in (5, 10, 20):
        for crashes in (20, 40, 60):
            for writes in (10, 20, 30, 40):
                case = f"Client-{servers}-{crashes}-{writes}"
                output.write("{}: {}\n".format(case, evaluate(case)))
    for seconds in (10, 20, 30, 40):
        case = f"Client-FreezeMaster-{seconds}"
        output.write("{}: {}\n".format(case, evaluate(case)))
