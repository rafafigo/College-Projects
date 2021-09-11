# Project Organization
- BIT
  - highBit
  - lowBit
  - SolverInstrumentation.java
- datasets
- org
- pt/ulisboa/tecnico/cnv
  - scaling
    - autoscaler
      - AutoScaler.java	
    - loadbalancer
      - LBWebServer.java
      - LoadBalancer.java
      - MSS
      - UserRequest
      - UserRequestsCosts
    - ScalingApp.java
    - ScalingInstance.java
  - server
    - HealthHandler.java
    - MSS.java
    - ScanHandler.java
    - ServerArgumentParser.java
    - Webserver.java
  - solver
  - util

- `SolverInstrumentation.java`: It is the class that contains the instrumentation code that is applied to the Solver;
- `pt`: It is the package where the code of the AutoScaler, LoadBalancer, WebServer and the Solver are placed;
- `org`: It contains dependencies of the system.

# Link for JavaDoc
- All the code done by the group is well documented on http://web.tecnico.ulisboa.pt/ist190774/CCV/JavaDocs/
