package org.ncsu.edu;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Scanner;

public class PeerA {

	private static String hostname = "PA";
	private String receivedHostname;
	private long portNumberOfRFCServerOfPeer;
	private long receivedPortNumberOfRFCServerOfActivePeer;
	private String registrationMessageToRS;
	private String queryMessageToRS;
	private String rfcQueryMessageToOtherPeers;
	private int cookieNumber = 0;
	private int counter = 0;
	private int numberOfActivePeersReceived;
	private LinkedList<ActivePeerDetailsForPA> activePeersDatabase = new LinkedList<ActivePeerDetailsForPA>();
	private LinkedList<RFCIndex> rfcDatabase = new LinkedList<RFCIndex>();
	private String messageType;
	private String completeRFCIndex = "";
	private int receivedRFCNumber;
	private String receivedRFCTitle;
	private String receivedHostnameOfPeerContainingRFC;
	private long rfcServerPortOfPB;
	private String getRFCFileMessageToPeers;
	private static final String RECEIVE_ON_FILE_PATH = System.getProperty("user.dir") + "\\RFCPA";
	private static final String SEND_FROM_FILE_PATH = "C:\\Users\\nikle\\Downloads\\Internet Protocols ECE 573\\RFCPA";
	private int requiredRFCNumber;
	private double startTimeToReceiveRFCFile;
	private double endTimeToReceiveRFCFile;
	private double timeElapsedToDownloadRFCFile;
	private double cumulativeTimeToDownloadAllRFC = 0;
	private String keepAliveMessageToRS;
	private String registration;
	private String pQuery;
	private String rfcIndexQuery;
	private int rfcFileQuery;
	private String leaveMessageToRS;
	private String responseToLeaveFromRS;
	private int task;
	
	/*	Class which stores values for hostname and port number of RFC server of active peers.
	 *	A linked list of this class's data type would be made and details for each active peer
	 *	would be stored in a single node. 
	 */
	private class ActivePeerDetailsForPA {
		
		private String receivedHostname; 
		private long receivedPortNumberOfRFCServerOfActivePeer;
		
		public String getReceivedHostname() {
			return receivedHostname;
		}
		public void setReceivedHostname(String receivedHostname) {
			this.receivedHostname = receivedHostname;
		}
		public long getReceivedPortNumberOfRFCServerOfActivePeer() {
			return receivedPortNumberOfRFCServerOfActivePeer;
		}
		public void setReceivedPortNumberOfRFCServerOfActivePeer(long receivedPortNumberOfRFCServerOfActivePeer) {
			this.receivedPortNumberOfRFCServerOfActivePeer = receivedPortNumberOfRFCServerOfActivePeer;
		}
		
		ActivePeerDetailsForPA(String receivedHostname, long receivedPortNumberOfRFCServerOfActivePeer){
			
			setReceivedHostname(receivedHostname);
			setReceivedPortNumberOfRFCServerOfActivePeer(receivedPortNumberOfRFCServerOfActivePeer);
		}
	}
	
	/*
	 * RFC Index of peer. Will contain details of local as well as RFCs with other peers.
	 * A linked list of this class's data type would be made and details of each RFC would
	 * be stored in a single node.
	 */
	private class RFCIndex {
		private int rfcNumber;
		private String rfcTitle;
		private String hostnameOfPeerContainingRFC;
		private int ttl;
		
		public int getRfcNumber() {
			return rfcNumber;
		}
		public void setRfcNumber(int rfcNumber) {
			this.rfcNumber = rfcNumber;
		}
		public String getRfcTitle() {
			return rfcTitle;
		}
		public void setRfcTitle(String rfcTitle) {
			this.rfcTitle = rfcTitle;
		}
		public String getHostnameOfPeerContainingRFC() {
			return hostnameOfPeerContainingRFC;
		}
		public void setHostnameOfPeerContainingRFC(String hostnameOfPeerContainingRFC) {
			this.hostnameOfPeerContainingRFC = hostnameOfPeerContainingRFC;
		}
		public int getTtl() {
			return ttl;
		}
		public void setTtl(int ttl) {
			this.ttl = ttl;
		}
		
		public RFCIndex(int rfcNumber, String rfcTitle, String hostnameOfPeerContainingRFC, int ttl) {
			
			setRfcNumber(rfcNumber);
			setRfcTitle(rfcTitle);
			setHostnameOfPeerContainingRFC(hostnameOfPeerContainingRFC);
			setTtl(ttl);
		}
	}
	
	/*
	 * Registration cycle. Opens socket with server, sends hostname and RFC server port
	 * and receives a cookie number in response
	 */
	public void registerCycle() throws IOException {
		Socket clientSocket = null;
		DataOutputStream outputStream = null;
		InputStream inputStream = null;		
		try {
					clientSocket = new Socket("127.0.0.1", 65423);
					outputStream = new DataOutputStream(clientSocket.getOutputStream());
					portNumberOfRFCServerOfPeer = 65406;
					registrationMessageToRS = "Register" + " " + hostname + " " + portNumberOfRFCServerOfPeer + " ";
					System.out.println("Register " + hostname + "P2P-DI/1.0");
					System.out.println("Host: Registration Server");
					System.out.println("RFC server port of Peer A is " + portNumberOfRFCServerOfPeer + "\n");
					outputStream.writeBytes(registrationMessageToRS);
					
					/*
					 * Receiving cookie number from RS
					 */
					inputStream = clientSocket.getInputStream();
					Scanner scanner = new Scanner(inputStream).useDelimiter(" ");
					if(scanner.hasNext()) {
						cookieNumber = Integer.parseInt(scanner.next());
						System.out.println("P2P-DI/1.0 200 Registration successful");
						System.out.println("Connection: close");
						System.out.println("Cookie number: " + cookieNumber + "\n");
					}
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					if(inputStream != null) {
						inputStream.close();
					}
					if(outputStream != null) {
						outputStream.close();
					}
					if(clientSocket != null) {
						clientSocket.close();
					}
				}
	}
	
	/*
	 * Query cycle. 'PQuery' sent along with hostname and cookie number.
	 * Response includes all the active peers together.
	 * (hostname) and (receivedPortNumberOfRFCServerOfActivePeer) of each peer separated
	 * and stored in a single node (activePeersDatabase) which is a linked list of  
	 * (ActivePeerDetailsForPA) class
	 * Query message to get active peers list
	 */
	public void getActivePeersFromRS() throws IOException {
		Socket clientSocket = null;
		DataOutputStream outputStream = null;
		InputStream inputStream = null;
		try {
					clientSocket = new Socket("127.0.0.1", 65423);
					outputStream = new DataOutputStream(clientSocket.getOutputStream());
					
					//Requesting for active peer list
					if(cookieNumber != 0) {
					queryMessageToRS = "PQuery" + " " + hostname + " " + String.valueOf(cookieNumber) + " ";
					outputStream.writeBytes(queryMessageToRS);
					System.out.println("GET Active peers P2P-DI/1.0");
					System.out.println("Host: Registration server");
					System.out.println("OS: " + System.getProperty("os.name") + "\n");
					}
					
					
					/*
					 * Should put a call to registerCycle() mostly. The client may leave and then cookieNumber would be made 0.
					 * So now before getting active peers list, it should ideally register or write a new method for re-registration.
					 * This would be followed by retrieval of active peers list. Maybe an else block which includes all this will work.
					 */
					
					
					//Receiving active peer list or appropriate message if no active peers
					inputStream = clientSocket.getInputStream();
					Scanner scanner = new Scanner(inputStream).useDelimiter(" ");
					if(scanner.hasNext()) {
						numberOfActivePeersReceived = Integer.parseInt(scanner.next());
					}
					while(scanner.hasNext()) {
						if(counter == 0 && numberOfActivePeersReceived > 0) {
							receivedHostname = scanner.next();
							counter++;
						}
						else if(counter == 1 && numberOfActivePeersReceived > 0) {
							receivedPortNumberOfRFCServerOfActivePeer = Integer.parseInt(scanner.next());
							activePeersDatabase.add(new ActivePeerDetailsForPA(receivedHostname, receivedPortNumberOfRFCServerOfActivePeer));
							counter = 0;
						}
					}
					if(numberOfActivePeersReceived > 0)
						System.out.println("P2P-DI/1.0 200 Active peers list received successfully \n");
					if(numberOfActivePeersReceived == 0) {
						System.out.println("Sorry. No active peers found.");
					}
					
					for(ActivePeerDetailsForPA activePeerDetailsForPA : activePeersDatabase) {
						System.out.println(activePeerDetailsForPA.getReceivedHostname());
						System.out.println(activePeerDetailsForPA.getReceivedPortNumberOfRFCServerOfActivePeer());
					}
					
				} 
				catch (Exception e) {
					e.printStackTrace();
				} 
				finally {
					if(inputStream != null) {
						inputStream.close();
					}
					if(outputStream != null) {
						outputStream.close();
					}
					if(clientSocket != null) {
						clientSocket.close();
					}
				}

			}
	
	
	/*
	 * Method to fetch RFC indexes present with other peers.
	 * Contacts each active peer by traversing the (activePeersDatabase) linked list.
	 * Receives their RFC index in response and merges with own RFC index.
	 */
	public void getRFCIndexFromOtherPeers() throws IOException {
		Socket clientSocket = null;
		DataOutputStream outputStream = null;
		InputStream inputStream = null;
		if(activePeersDatabase.size() > 0) {
			for(ActivePeerDetailsForPA activePeerDetailsForPA : activePeersDatabase) {
				try {
					/*
					 * Opens a client socket and establishes connection 
					 * with one of the active peers present in the peer index.
					 */
					clientSocket = new Socket("127.0.0.1", (int) activePeerDetailsForPA.getReceivedPortNumberOfRFCServerOfActivePeer());
					outputStream = new DataOutputStream(clientSocket.getOutputStream());
					System.out.println("GET RFC-Index P2P-DI/1.0");
					System.out.println("Host: " + activePeerDetailsForPA.getReceivedHostname());
					System.out.println("OS: " + System.getProperty("os.name") + "\n");
					rfcQueryMessageToOtherPeers = "RFCQuery" + " ";
					outputStream.writeBytes(rfcQueryMessageToOtherPeers);
					
					inputStream = clientSocket.getInputStream();
					Scanner scanner = new Scanner(inputStream).useDelimiter(" ");
					while(scanner.hasNext()) {
						if(counter == 0) {
							receivedRFCNumber = Integer.parseInt(scanner.next());
							counter++;
						}
						else if(counter == 1) {
							receivedRFCTitle = scanner.next();
							counter++;
						}
						else if(counter == 2) {
							receivedHostnameOfPeerContainingRFC = scanner.next();
							System.out.println("RFC Number: " + receivedRFCNumber);
							System.out.println("RFC Title: " + receivedRFCTitle);
							System.out.println("Host: " + receivedHostnameOfPeerContainingRFC + "\n");
							rfcDatabase.add(new RFCIndex(receivedRFCNumber, receivedRFCTitle, receivedHostnameOfPeerContainingRFC, 7200));
							counter = 0;
						}
					}
					System.out.println("P2P-DI/1.0 200 RFC-Index received successfully from " + receivedHostnameOfPeerContainingRFC + "\n");
					
				} 
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					if(inputStream != null) {
						inputStream.close();
					}
					if(outputStream != null) {
						outputStream.close();
					}
					if(clientSocket != null) {
						clientSocket.close();
					}
				}
			}
		}
	}
	
	public void retrieveRFCServerPortsOfActivePeers() {
		for (ActivePeerDetailsForPA activePeerDetailsForPA : activePeersDatabase) {
			if(activePeerDetailsForPA.getReceivedHostname().equals("PB")) {
				rfcServerPortOfPB = activePeerDetailsForPA.getReceivedPortNumberOfRFCServerOfActivePeer();
			}
		}
	}
	
	public void requestForRFCToPeer(int rfcNumber, String hostnameOfPeerContainingRFC, DataOutputStream outputStream) throws UnknownHostException, IOException {
			System.out.format("GET RFC %s P2P-DI/1.0 \n", rfcNumber);
			System.out.println("Host: " + hostnameOfPeerContainingRFC);
			System.out.println("OS: " + System.getProperty("os.name") + "\n");
			getRFCFileMessageToPeers = "GetRFC" + " " + rfcNumber + " ";
			outputStream.writeBytes(getRFCFileMessageToPeers);
		
	}
	
	public void storeRFCinFileOnReception(InputStream inputStream, int rfcNumber) throws IOException {
		FileOutputStream fileOutputStream = null;
		BufferedOutputStream bufferedOutputStream = null;
		int bytesRead;
		int currentBytesRead = 0;
		byte [] fileAsArray = new byte[60000000];
		new File(RECEIVE_ON_FILE_PATH).mkdirs();
		fileOutputStream = new FileOutputStream(RECEIVE_ON_FILE_PATH + "\\" + rfcNumber + ".pdf");
		bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
		do {
			bytesRead = inputStream.read(fileAsArray, currentBytesRead, (fileAsArray.length - currentBytesRead));
			if(bytesRead >=0) {
				currentBytesRead += bytesRead;
			}
		}while(bytesRead > -1);
		
		bufferedOutputStream.write(fileAsArray, 0, currentBytesRead);
		bufferedOutputStream.flush();
		System.out.println("P2P-DI/1.0 200 RFC " + rfcNumber + " downloaded successfully \n");
		bufferedOutputStream.close();
	}
	
	public void getRFCFileFromOtherPeers(int rfcNumber) throws UnknownHostException, IOException {
		
		/*
		 * First we need to find out the RFC server ports of each peer
		 * so that the client can open a socket connection with
		 * that RFC server and then send a request for the RFCs with that peer.
		 * This would involve minimal changes while optimizing i.e
		 * if the peer wants a particular RFC only then we'll just
		 * have to incorporate a condition for that particular RFC number 
		 * and everything else would remain the same. On the other hand, 
		 * if a connection is opened with each of the peers to receive all the 
		 * RFCs with that peer then it would involve more changes to incorporate 
		 * more changes i.e we'll have to first traverse the RFC index, find the appropriate 
		 * peer containing the RFC and then open connection with that peer. Also, unnecessary
		 * calls might be made to all the peers if we use a 'for' loop traversing the (activePeerDatabase)
		 */
		Socket clientSocket = null;
		DataOutputStream outputStream = null;
		InputStream inputStream = null;
		/*
		 * FileWriter and BufferedWriter used to store the values
		 * of cumulative times after each RFC file is received.
		 * bufferedWriter.write(value) is used to store the value.
		 */
		FileWriter fileWriter = new FileWriter("CumulativeTimeForPA");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		retrieveRFCServerPortsOfActivePeers();
		
		try {
			if(rfcDatabase.size() > 0) {
				for(RFCIndex rfcIndex : rfcDatabase) {
					if(rfcIndex.getHostnameOfPeerContainingRFC().equals("PB") && rfcNumber == rfcIndex.getRfcNumber()) {
						startTimeToReceiveRFCFile = System.nanoTime();
						clientSocket = new Socket("127.0.0.1", (int) rfcServerPortOfPB);
						outputStream = new DataOutputStream(clientSocket.getOutputStream());
						
						requestForRFCToPeer(rfcIndex.getRfcNumber(), rfcIndex.getHostnameOfPeerContainingRFC(), outputStream );
						
						inputStream = clientSocket.getInputStream();
						storeRFCinFileOnReception(inputStream, rfcIndex.getRfcNumber());
						endTimeToReceiveRFCFile = System.nanoTime();
						timeElapsedToDownloadRFCFile = (endTimeToReceiveRFCFile - startTimeToReceiveRFCFile)/ 1000000000.0;
						cumulativeTimeToDownloadAllRFC += timeElapsedToDownloadRFCFile;
						bufferedWriter.write(String.valueOf(cumulativeTimeToDownloadAllRFC) + "\n");
					}
				}
				bufferedWriter.close();
				cumulativeTimeToDownloadAllRFC = 0;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(outputStream != null) {
				outputStream.close();
			}
			if(inputStream != null) {
				inputStream.close();
			}
			if(clientSocket != null) {
				clientSocket.close();
			}
		}
		
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
		PeerA peerA = new PeerA();
		Scanner reader = new Scanner(System.in);
		
		System.out.println("Please press 'y' to register with the registration server");
		peerA.registration = reader.next();
		if(peerA.registration.equalsIgnoreCase("y")) {
			peerA.registerCycle();
		}
		
		System.out.println("Please press 'y' to get list of active peers from Registration Server");
		peerA.pQuery = reader.next();
		if(peerA.pQuery.equalsIgnoreCase("y")) {
			peerA.getActivePeersFromRS();
		}
		
		System.out.println("Please press 'y' to get RFC index from all other active peers");
		peerA.rfcIndexQuery = reader.next();
		if(peerA.rfcIndexQuery.equalsIgnoreCase("y")) {
			peerA.getRFCIndexFromOtherPeers();
		}
		
		System.out.println("Peer A has two RFCs in its RFC index (8277 and 8269). Please enter RFC number of RFC to be downloaded.");
		peerA.rfcFileQuery = reader.nextInt();
		peerA.getRFCFileFromOtherPeers(peerA.rfcFileQuery);
		
		Thread.sleep(50000);
		peerA.activePeersDatabase.remove();
		peerA.getActivePeersFromRS();
	}

}
