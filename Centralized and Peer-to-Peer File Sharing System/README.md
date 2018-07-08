# IP Project 1: Peer-to-Peer system with Distributed index
The project strives towards developing our understanding of client-server and peer-to-peer architecture. This is done by implementing a peer-to-peer system with distributed index. The system consists of a registration server and six peers- P0, P1, P2, P3, P4 and P5.
## Getting started
There are three scenarios that can be tested with our codes.

* Centralized file distribution
* Peer-to-Peer file distribution
* Simple case demonstrating flow

We have shared a **jar**, 60 **RFCs** and all java **source** files. 
## Running the tests
The commands are provided to run the code on a Linux machine. A new command prompt will be required to execute each of the below mentioned commands.

* For centralized and P2P file distribution scenario

1. Execute **java -cp Project.jar org.ncsu.edu.RegistrationServer**.
2. Execute **java -cp Project.jar org.ncsu.edu.P0** (Please register immediately after execution).
3. Execute **java -cp Project.jar org.ncsu.edu.P1** (Please register immediately after execution).
4. Execute **java -cp Project.jar org.ncsu.edu.P2** (Please register immediately after execution).
5. Execute **java -cp Project.jar org.ncsu.edu.P3** (Please register immediately after execution).
6. Execute **java -cp Project.jar org.ncsu.edu.P4** (Please register immediately after execution).
7. Execute **java -cp Project.jar org.ncsu.edu.P5** (Please register immediately after execution).

After registering, select which task needs to be executed (Press 1 or 2 depending on the scenario to be tested). Please do this step for each peer before moving ahead. Post this, the instructions can be followed from the prompts coming up on the screen. Keepalives are sent every 30 seconds by each peer after it starts downloading. A new folder would be created for each peer (RFCP0, RFCP1, RFCP2, RFCP3, PFCP4 and RFCP5) and these would contain the downloaded RFCs for each peer.

* For simple case between peers A and B to demonstrate flow

Initially, terminate the **Registration server**. Peer B would send a **leave** message to the registration server after 40 seconds and Peer A would make a request for the list of active peers again to the registration server after 50 seconds. Since Peer B is no longer marked as active by the registration server, Peer A would receive a response as 'Sorry. No active peers found'.


1. Execute **java -cp Project.jar org.ncsu.edu.RegistrationServer**.
2. Execute **java -cp Project.jar org.ncsu.edu.PeerA** (Please register immediately after execution).
3. Execute **java -cp Project.jar org.ncsu.edu.PeerB** (Please register immediately after execution).

Post this, the instructions can be followed from the prompts coming up on the screen. A new folder would be created (RFCPA) and this would contain the downloaded RFC.
