Demo Guide
==========

## Usage Example

- Prepare the Work Environment (In the root directory of the project `mvn clean install -DskipTests`).
- Run Zookeeper (In the directory where Zookeeper is): `./zkServer.sh  start`:
- Run Server (In module [silo-server](silo-server/): `mvn compile exec:java`).
- Run Client *eye* (In module [eye](eye/): `./target/appassembler/bin/eye localhost 2181 Tagus 30 30`).
  - This registers the camera named 'Tagus' in the Location (30.0, 30.0) on the Server.
- Start recording writting a comment on the Client *eye* terminal:
  - `# Here We Go!`
- Record an image of a person with the identifier '90770' by typing on the Client *eye* terminal:
  - `person, 90770`
- Record an image of a car with the identifier '86AA23' by typing on the Client *eye* terminal:
  - `car, 86AA23`
- Report the recorded images to the Server by typing an empty line on the Client *eye* terminal.
- Pause Captures recording for 10 Seconds by typing on the Client *eye* terminal:
  - `zzz 10000`
- Run Client *spotter* (In module [spotter](spotter/): `./target/appassembler/bin/spotter localhost 2181`).
- Search a person with identifier '90770' by typing on the Client *spotter* terminal:
  - `spot person 90770`
  - This will present the most recent Observation of this person. In this case the presented Observation will be `PERSON,90770,<TimeStamp>,Tagus,30.0,30.0`.
- Record an image of a person with the identifier '90774' by typing on the the Client *eye* terminal:
  - `person, 90774`
- Report the recorded image to the Server by typing an empty line on the Client *eye* terminal.
- Search for persons with an identifier that starts with '9077' by typing on Client *spotter* terminal:
  - `spot person 9077*`
  - This will present the most recent Observation of each of the persons with an identifier that starts with '9077'. In this case the presented Observations will be `PERSON,90770,<TimeStamp>,Tagus,30.0,30.0` and `PERSON,90774,<TimeStamp>,Tagus,30.0,30.0` by ascending order of identifier.
- Record another image of a car with the identifier '86AA23' by typing on the Client *eye* terminal:
  - `car, 86AA23`
- Report the recorded image to the Server by typing an empty line on the Client *eye* terminal.
- Search the trace of the car with the identifier '86AA23' by typing on the Client *spotter* terminal:
  - `trail car 86AA23`
  - This will present all Observations of the Car with this identifier. In this case the presented Observations will be `CAR,86AA23,<TimeStamp>,Tagus,30.0,30.0` and `CAR,86AA23,<TimeStamp>,Tagus,30.0,30.0` by most recent order.

### Notes

- `<TimeStamp>` is in format [ISO 8601](https://www.iso.org/iso-8601-date-and-time-format.html).
- Note that you can use whatever partial identifier on command `spot` in the Client *spotter* using '*'.
- In the directory [tests](/tests) there are available demonstrations of possible inputs for the Clients *eye* and *spotter*.

## Fault Tolerance & Replication Example

- Prepare the Work Environment (In the root directory of the project `mvn clean install -DskipTests`).
- Run Zookeeper (In the directory where Zookeeper is): `./zkServer.sh  start`:
- Run replica with `instance = 1` (In module [silo-server](silo-server/): `mvn compile exec:java -Dsync=10`).
- Run Client *spotter* (In module [spotter](spotter/): `./target/appassembler/bin/spotter localhost 2181`).
- Run Client *eye* (In module [eye](eye/): `./target/appassembler/bin/eye localhost 2181 Tagus 30 30`).
  - This registers the camera named 'Tagus' in the Location (30.0, 30.0) on replica with `instance = 1`.
- Record an image of a person with the identifier '1337' by typing on the Client *eye* terminal:
  - `person, 1337`
  - The Observation was recorded on replica with `instance = 1`.
- Type `spot person *` Enter on *spotter* terminal connected to replica with `instance = 1`.
  - The presented Observation will be `PERSON,1337,<TimeStamp>,Tagus,30.0,30.0`.
- Run replica with `instance = 2` (In module [silo-server](silo-server/): `mvn compile exec:java -Dsync=10 -Dinstance=2`).
- Run Client *spotter* (In module [spotter](spotter/): `./target/appassembler/bin/spotter localhost 2181 2`).
  - This connects *spotter* with replica with `instance = 2`.
- Type `spot person *` Enter on *spotter* terminal connected to replica with `instance = 2`.
  - The presented Observation will be `PERSON,1337,<TimeStamp>,Tagus,30.0,30.0`, that shows that the Observation is also recorded on replica with `instance = 2`.
- Turn off the *spotter* connected to replica with `instance = 2` by typing `^C` on its terminal.
- Crash the replica with `instance = 1` by typing `^C` on its terminal.
- Type `spot person *` Enter on *spotter* terminal connected previously with `instance = 1`.
  - As you can see, *spotter* is still able to answer to commands and the Observation `PERSON,1337,<TimeStamp>,Tagus,30.0,30.0` is presented. This *spotter* is now connected to the replica with `instance = 2`.
- Crash the replica with `instance = 2` by typing `^C` on its terminal.
- Run replica with `instance = 1` (In module [silo-server](silo-server/): `mvn compile exec:java -Dsync=10`).
  - Note that this replica does not have the camera 'Tagus' registered. 
- Record an image of a person with the identifier '90774' by typing on the Client *eye* terminal:
  - `person, 90774`
  - This *eye* is now connected to the replica with `instance = 1`. Note that the Observation is recorded with success because the camera 'Tagus' is registered again.
- Type `spot person *` Enter on *spotter* terminal.
  - As expected, the presented Observation will be `PERSON,90774,<TimeStamp>,Tagus,30.0,30.0`.
- Run replica with `instance = 2` (In module [silo-server](silo-server/): `mvn compile exec:java -Dsync=10 -Dinstance=2`).
- Crash the replica with `instance = 1` by typing `^C` on its terminal.
- Type `spot person *` Enter on *spotter* terminal.
  - The presented Observation will be `PERSON,90774,<TimeStamp>,Tagus,30.0,30.0`.

## Authors:

Group T18:
- 86923, [Sara Machado](https://github.com/SaraMachado)
- 90770, [Rafael Figueiredo](https://github.com/RafaelAlexandreIST)
- 90774, [Ricardo Grade](https://github.com/Opty1337)
