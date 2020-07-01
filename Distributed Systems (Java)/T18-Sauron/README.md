# Sauron

Distributed Systems 2019-2020, 2nd semester project

## Authors
**Group T18**

### Team members
| Number | Name | User | Email |
|--------|-------------------|-----------------------------------------|-----------------------------------------------------------------|
| 86923  | Sara Machado      | <https://github.com/SaraMachado>        | <mailto:sara.f.machado@tecnico.ulisboa.pt>                      |
| 90770  | Rafael Figueiredo | <https://github.com/RafaelAlexandreIST> | <mailto:rafael.alexandre.roberto.figueiredo@tecnico.ulisboa.pt> |
| 90774  | Ricardo Grade     | <https://github.com/Opty1337>           | <mailto:ricardo.grade@tecnico.ulisboa.pt>                       |

### Task leaders
| Task set | To-Do | Leader |
| ---------|-------------------------------| --------------------|
| core     | protocol buffers, silo-client | _(whole team)_      |
| T1       | cam_join, cam_info, eye       | _Ricardo Grade_     |
| T2       | report, spotter               | _Rafael Figueiredo_ |
| T3       | track, trackMatch, trace      | _Sara Machado_      |
| T4       | test T1                       | _Rafael Figueiredo_ |
| T5       | test T2                       | _Sara Machado_      |
| T6       | test T3                       | _Ricardo Grade_     |
| P2       | 2nd Project                   | _(whole team)_      |

## Getting Started

The overall system is composed of multiple modules.
The main server is the _silo_.
The clients are the _eye_ and _spotter_.

See the [project statement](https://github.com/tecnico-distsys/Sauron/blob/master/README.md) for a full description of the domain and the system.

### Prerequisites

Java Developer Kit 11 is required running on Linux, Windows or Mac.
Maven 3 is also required.

To confirm that you have them installed, open a terminal and type:

```
javac -version

mvn -version
```

### Preparation of Work Environment
In root directory of the project:
```
mvn clean install -DskipTests
```

In the directory where Zookeeper is:
```
./zkServer.sh  start
```

### To Execute the Client *eye*
In module [silo-server](silo-server/):
```
mvn compile exec:java -Dinstance=X -Dsync=Y -Dtimeout=T -Dretries=R

X = Replica ID
Y = Number of seconds between replicas messages exchanges
T = Timeout of requests
R = Number of retries to establish connection between replicas

Default: X = 1 (argument is optional)
Default: Y = 30 (argument is optional)
Default: T = 10 (argument is optional)
Default: R = 3 (argument is optional)
```
In module [eye](eye/):
```
./target/appassembler/bin/eye <Host> <Port> <Camera Name> <Camera Latitude> <Camera Longitude> <Instance>

Default: Instance = 1 (argument is optional)
```

### To Execute the Client *spotter*
In module [silo-server](silo-server/):
```
mvn compile exec:java
```
In module [spotter](spotter/):
```
./target/appassembler/bin/spotter <Host> <Port> <Instance>

Default: Instance = 1 (argument is optional)
```

### To Execute Integration Tests
In module [silo-server](silo-server/):
```
mvn compile exec:java
```
In root directory of the project:
```
mvn verify
```

### To Execute the tests of *eye*
In module [silo-server](silo-server/):
```
mvn compile exec:java
```
In module [eye](eye/):
```
./target/appassembler/bin/eye localhost 8080 Tagus 30 30 < ../demo/tests/eye.txt > /tmp/eye.out
diff ../demo/tests/eye.out /tmp/eye.out
```

### To Execute the tests of *spotter*
In module [silo-server](silo-server/):
```
mvn compile exec:java
```
In module [spotter](spotter/):
```
./target/appassembler/bin/spotter localhost 8080  < ../demo/tests/spotter.txt > /tmp/spotter.out
diff ../demo/tests/spotter.out /tmp/spotter.out
```

## Built With
* [Maven](https://maven.apache.org/) - Build Tool and Dependency Management
* [gRPC](https://grpc.io/) - RPC framework

## Versioning
We use [SemVer](http://semver.org/) for versioning. 
