# Medical Test Records

## 1. System Setup

The system can be used in a single computer or VM using different terminals or in 4 VMs that need to be setup as described in Section [1.2](#12-setup-of-4-virtual-machines), if you desire to use virtual machine/s you can use the OVA that is available on this [(Link)](https://web.tecnico.ulisboa.pt/~ist190774/SIRS/) (Username: `mtr`, Password: `1337`), that contains all the requirements to run this system.

### 1.1. Requirements
- JDK 11
- Maven
- PostgresSQL
- Ubuntu
In case you are using a VM:
  - VBoxGuest Additions

### 1.2. Setup Of 4 Virtual Machines
To run this project in 4 VMs, it is needed:
- Install Oracle VM VirtualBox
- Four VMs running with Ubuntu:
  - HospitalServer
  - PolicyAuthoring
  - PartnerLab
  - Employee
- Create 3 NAT Networks (File -> Preferences -> Network):
  - 10.0.2.0/28 with the name HSEmployee
  - 10.0.3.0/28 with the name HSPartnerLab
  - 10.0.4.0/28 with the name HSPolicyAuthoring
- Add the following adapters (Settings -> Network) to the VM in order to establish NAT Networks:
  - HospitalServer: Adapter 1: HSEmployee, Adapter 2: HSPartnerLab, Adapter 3: HSPolicyAuthoring
  - PolicyAuthoring: Adapter 1: HSPolicyAuthoring
  - PartnerLab: Adapter 1: HSPartnerLab
  - Employee: Adapter 1: HSEmployee
- Start the VMs
- Setup a shared folder in each VM in order to transmit the Certificates, and the Certificate Signing Requests (CSR) between the CA, and the VMs
- Do the same steps that are described below but in the corresponding VM
- When running each program change the default arguments of the following modules:
  - Hospital Server: Change the default IP of the Policy Authoring, to the actual IP on that NAT Network.
  - Partner Lab: Change the default IP of the Hospital Server, to the actual IP on that NAT Network
  - Employee: Change the default IP of the Hospital Server, to the actual IP on that NAT Network.

## 2. Generate CA Certificate

Generate a CA Certificate. This is required in order to Sign the System Nodes CSRs by a Trusted Entity.      

Commands:
```bash
$ cd CA
$ ./newCA.sh
```

## 3. Run Policy Authoring

Generate the Policy Authoring Certificate Signed by a Trusted Entity. This is required for the TLS Connections to be established.

Commands:
```bash
$ cd PolicyAuthoring/src/main/resources
$ ./newCSR.sh
```

Provide the CSR Generated, and the CSR Config File to the CA and Sign it. Following that, Copy it back along with the CA Certificate to `PolicyAuthoring/src/main/resources`.

Commands:
```bash
$ cd CA
$ ./signCSR.sh PA
```

At this moment Policy Authoring is ready to Start Running.

Commands:
```bash
$ cd PolicyAuthoring
$ mvn exec:java
```

**Obs**: To `mvn exec:java` can be additionally provided the following Pairs:
- `-DmyPort=<PORT>` To Configure the Port where the Policy Authoring is going to be deployed.

## 3. Run Hospital Server

Already with the Policy Authoring deployed.
Generate the Hospital Server Certificate Signed by a Trusted Entity. This is required for the TLS Connections to be established, and to Authenticate the Custom Security Procol Handshake.

Commands:
```bash
$ cd HospitalServer/src/main/resources
$ ./newCSR.sh
```

Provide the CSR Generated, and the CSR Config File to the CA and Sign it. Following that, Copy it back along with the CA Certificate to `HospitalServer/src/main/resources`.

Commands:
```bash
$ cd CA
$ ./signCSR.sh HS
```

Start the Hospital Server Database where it will be Stored all Persistent Data that the Hospital Server has.

Commands:
```bash
$ cd HospitalServer/src/main/resources
$ ./newDB.sh
```

At this moment Hospital Server is ready to Start Running.

Commands:
```bash
$ cd HospitalServer
$ mvn exec:java
```

**Obs**: To `mvn exec:java` can be additionally provided the following Pairs:
- `-DmyServerEmployeePort=<PORT>` To Configure the Port where the Server, which implements the Employee Services, is going to be deployed.
- `-DmyServerPartnerLabPort=<PORT>` To Configure the Port where the Server, which implements the Partner Lab Services, is going to be deployed.
- `-DmyPolicyAuthoringHost=<HOST>` To Configure the Host where the Policy Authoring is deployed.
- `-DmyPolicyAuthoringPort=<HOST>` To Configure the Port where the Policy Authoring is deployed.
- `-DmyDBInit=1` To Create and Populate the Tables of the Hospital Server Database. **Must** be provided only the 1st time after the Database is Created (Running `./newDB.sh`)

## 4. Run a User

### 4.1. Partner Lab

Already with the Hospital Server deployed.
Generate the Partner Lab Certificate Signed by a Trusted Entity. This is required to Sign its Test Results, and to Authenticate the Custom Security Protocol Handshake.

Commands:
```bash
$ cd PartnerLab/src/main/resources
$ ./newCSR.sh
```

Provide the CSR Generated, and the CSR Config File to the CA and Sign it. Following that, Copy it back along with the CA Certificate to `PartnerLab/src/main/resources`.

Commands:
```bash
$ cd CA
$ ./signCSR.sh PL
```

At this moment Partner Lab is ready to Start Running.

Commands:
```bash
$ cd PartnerLab
$ mvn exec:java
```

**Obs**: To `mvn exec:java` can be additionally provided the following Pairs:
- `-DmyServerHost=<HOST>` To Configure the Host where the Hospital Server is deployed.
- `-DmyServerPort=<PORT>` To Configure the Port where the Partner Lab Services on Hospital Server are deployed.

### 4.2. Employee

Already with the Hospital Server deployed.
Copy the CA Certificate to `Employee/src/main/resources`. It is required so that TLS is able to verify the Hospital Server Certificate.

At this moment Employee is ready to Start Running.

Commands:
```bash
$ cd Employee
$ mvn exec:java
```

**Obs**: To `mvn exec:java` can be additionally provided the following Pairs:
- `-DmyServerHost=<HOST>` To Configure the Host where the Hospital Server is deployed.
- `-DmyServerPort=<PORT>` To Configure the Port where the Employee Services on Hospital Server are deployed.

---

## Relevat Links:
- Project Overview Available [(Here)](https://github.com/tecnico-sec/Project-Overview-2021_1)
- Project Topic (3<sup>rd</sup>) Available [(Here)](https://github.com/tecnico-sec/Project-Topics-2021_1#3-medical-test-records)

---

| Name | University | Email |
| ---- | ---- | ---- |
| Ricardo Grade | Instituto Superior Técnico | ricardo.grade@tecnico.ulisboa.pt |
| Sara Machado | Instituto Superior Técnico | sara.f.machado@tecnico.ulisboa.pt |
| Rafael Figueiredo | Instituto Superior Técnico | rafael.alexandre.roberto.figueiredo@tecnico.ulisboa.pt |
