# Highly Dependable Location Tracker

### Requirements

- Maven
- Java 15
- PostgreSQL 12

## Base Setup

```shell
$ mvn clean install -DskipTests
$ cd Server/src/main/resources/
$ sh newDB.sh
$ cd -
```

---

## To Run Server

```shell
$ cd Server
$ mvn exec:java
```

### Additional Arguments

- `-DmyNByzantineUsers=<f>` (Default: `1`)
- `-DmySessionTime=<Session Duration in Seconds>` (Default: `60`)

## To Run Users

- To Run *N* Users, *N* <= 10:

```shell
$ cd User
$ bash runUsers.bash <N> <f> <Max Distance> <Session Duration in Seconds> <Epoch Duration in Millis>
```

### Suggested Parameters

- `<N>`: 10
- `<f>`: 1
- `<Max Distance>`: 5
- `<Session Duration in Seconds>`: 60
- `<Epoch Duration in Millis>`: 1000
  (`-1` To Automatically Open the User Shell Without Submitting its Locations)

## To Run HA

```shell
$ cd HA
$ mvn exec:java
```

### Additional Arguments

- `-DmySessionTime=<Session Duration in Seconds>` (Default: `60`)

---

## To Run Tests

### To Run User Tests

```shell
$ sh runUsersTests.sh
```

#### Instructions

1) Please, Insert your Password in the Terminal that is Requesting it. (The Script to Create a New Database
   Requires `sudo`). This Terminal Will Run the Server!
2) Please, Press *Enter* in the Terminal Displaying *Press Enter to Run Users Tests!* after the Server has Initialized!
   This Terminal Will Run the Users Tests!

### To Run HA Tests

```shell
$ sh runHATests.sh
```

#### Instructions

1) Please, Insert your Password in the Terminal that is Requesting it. (The Script to Create a New Database
   Requires `sudo`). This Terminal Will Run the Server!
2) Please, Press *Enter* in the Terminal Displaying *Press Enter to Launch Users!* after the Server has Initialized! It
   Will Open 10 User Processes Submitting its Location in Multiple Epochs.
3) Please, Press *Enter* in the Terminal Displaying *Press Enter to Run HA Tests!* after all Users Submit its Location
   Proofs. (It Takes Around 10 Seconds). This Terminal Will Run the HA Tests.

### To Run Byzantine Tests

```shell
$ sh runByzantineTests.sh
```

#### Instructions

1) Please, Insert your Password in the Terminal that is Requesting it. (The Script to Create a New Database
   Requires `sudo`). This Terminal Will Run the Server!
2) Please, Press *Enter* in the Terminal Displaying *Press Enter to Run Byzantine Tests!* after the Server has
   Initialized! This Terminal Will Run the Byzantine Tests.

### To Run Data Persistence Test

```shell
$ sh runPersistenceTest.sh
```

#### Test Description

The Script:

1) Runs the Server.
2) Lets a User Submit its Location Proofs.
3) Kills the Server.
4) Runs the Server Again.
5) Lets the Same User to Submit Again its Location Proofs.
   (The Server Already Have Them All!)

### To Run Data Corruption Test

```shell
$ sh runCorruptionTest.sh
```

#### Test Description

The Script:

1) Runs the Server.
2) Lets a User Submit a few Location Proofs.
3) Kills the Server.
   (While the User is Submitting its Location Proofs!)
4) The User Keeps Retrying the Submission.
5) Runs the Server Again.
6) Lets the User Submit its Remaining Location Proofs.
7) Kills the Server.
8) Runs the Server Again.
9) Lets the Same User to Submit Again its Location Proofs.
   (The Server Already Have Them All!)

---

## Tests Categories

### Freshness Tests

- [`submitULReportFreshness`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/ByzantineUserToServerAuthFreshTests.java#L47)
- [`obtainULNoFreshness`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/ByzantineUserToServerAuthFreshTests.java#L60)
- [`dHNoFreshness`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/ByzantineUserToDHServerAuthFreshTests.java#L35)

### Authenticity Tests

- [`dHNoValidSignature`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/ByzantineUserToDHServerAuthFreshTests.java#L40)
- [`getAuthProofsInvalidSignature`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/ByzantineUserToUserAuthFreshTests.java#L60)
- [`submitULReportNoValidHMAC`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/ByzantineUserToServerAuthFreshTests.java#L53)
- [`obtainULNoValidHMAC`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/ByzantineUserToServerAuthFreshTests.java#L66)

### Invalid Session Tests

- [`submitULReportWithInvalidSession`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/ByzantineUserToServerTests.java#L202)
- [`obtainULWithInvalidSession`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/ByzantineUserToServerTests.java#L230)

### Dropping Packets Tests

- [`submitULReportDoesNotAnswer`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/MITMByzantineUserToServerTests.java#L88)
- [`obtainULDoesNotAnswer`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/MITMByzantineUserToServerTests.java#L123)
- [`requestULProofDoesNotAnswer`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/MITMByzantineUserToUserTests.java#L103)

### Rejecting Packets Tests

- [`submitULReportWhereServerReject`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/MITMByzantineUserToServerTests.java#L70)
- [`obtainULWhereServerReject`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/MITMByzantineUserToServerTests.java#L106)
- [`requestULMalformedExceptionProof`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/MITMByzantineUserToUserTests.java#L83)

### Forging Proofs Tests

- [`submitULWithInsufficientProofs`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/ByzantineUserToServerTests.java#L182)
- [`submitULWithoutProofs`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/ByzantineUserToServerTests.java#L167)
- [`submitULReplicatedProofs`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/ByzantineUserToServerTests.java#L148)
- [`submitULSelfGeneratedProofs`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/ByzantineUserToServerTests.java#L131)
- [`requestULProofToFarAwayUser`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/ByzantineUserToUserTests.java#L79)

### Impersonating Tests

- [`submitULImpersonator`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/ByzantineUserToServerTests.java#L111)
- [`obtainULImpersonator`](./Byzantine/src/test/java/pt/tecnico/ulisboa/hds/hdlt/user/byzantine/ByzantineUserToServerTests.java#L101)

### Data Persistence Test

- [`runPersistenceTest`](./runPersistenceTest.sh)

### Data Corruption Test

- [`runCorruptionTest`](./runCorruptionTest.sh)

---

## Useful Scripts

- To Get the List of Users in the Proximity Considering a Given Max Distance:

```shell
$ cd User
$ python3 getCloseUsers.py src/main/resources/Grid.csv <Max Distance>
```

- To Reset Database and Run Server:

```shell
$ cd Server
$ sh runServer.sh <f>
```
