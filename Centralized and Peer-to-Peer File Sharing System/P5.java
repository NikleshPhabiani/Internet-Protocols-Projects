package org.ncsu.edu;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class P5 {
	private static String hostname = "P5";
	private String receivedHostname;
	private long portNumberOfRFCServerOfPeer;
	private long receivedPortNumberOfRFCServerOfActivePeer;
	private String registrationMessageToRS;
	private String queryMessageToRS;
	private String rfcQueryMessageToOtherPeers;
	private int cookieNumber = 0;
	private int counter = 0;
	private int numberOfActivePeersReceived;
	private LinkedList<ActivePeerDetailsForP5> activePeersDatabase = new LinkedList<ActivePeerDetailsForP5>();
	private LinkedList<RFCIndex> rfcDatabase = new LinkedList<RFCIndex>();
	private String messageType;
	private String completeRFCIndex = "";
	private int receivedRFCNumber;
	private String receivedRFCTitle;
	private String receivedHostnameOfPeerContainingRFC;
	private long rfcServerPortOfP0;
	private long rfcServerPortOfP1;
	private long rfcServerPortOfP2;
	private long rfcServerPortOfP3;
	private long rfcServerPortOfP4;
	private String getRFCFileMessageToPeers;
	private static final String RECEIVE_ON_FILE_PATH = System.getProperty("user.dir") + "\\RFCP5";
	private static final String SEND_FROM_FILE_PATH = System.getProperty("user.dir");
	private int requiredRFCNumber;
	private double startTimeToReceiveRFCFile;
	private double endTimeToReceiveRFCFile;
	private double timeElapsedToDownloadRFCFile;
	private double cumulativeTimeToDownloadAllRFC = 0;
	private String keepAliveMessageToRS;
	private String registration;
	private String pQuery;
	private String rfcIndexQuery;
	private String rfcFileQuery;
	private String leaveMessageToRS;
	private String responseToLeaveFromRS;
	private int task;
	
private class ActivePeerDetailsForP5 {
		
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
		
		ActivePeerDetailsForP5(String receivedHostname, long receivedPortNumberOfRFCServerOfActivePeer){
			
			setReceivedHostname(receivedHostname);
			setReceivedPortNumberOfRFCServerOfActivePeer(receivedPortNumberOfRFCServerOfActivePeer);
		}
	}

/*
 * RFC Index of peer. Will contain details of local as well as RFCs with other peers.
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
 * An incoming request for either the RFC index
 * or RFC file is received and appropriate response
 * sent to the requesting peer. A new class is made that 
 * implements runnable since we want to pass the connectionSocket
 * as the parameter to the thread. By default, Runnable has only run()
 * method inside. So a new class with a parameterized constructor is
 * made to accept the connectionSocket as a parameter and then when
 * a call need to be made to this thread, an object of Runnable class
 * is created which possesses an instance of the newly created class,
 * which is (ThreadToSendRFCIndexOrRFCToPeer) in our case.
 */

private class ThreadToSendRFCIndexOrRFCToPeer implements Runnable {
	public ThreadToSendRFCIndexOrRFCToPeer(Socket connectionSocket) throws IOException {
		
		InputStream inputStream = null;
		DataOutputStream outputStream = null;
		FileInputStream fileInputStream = null;
		BufferedInputStream bufferedInputStream = null;
		try {
			if(connectionSocket != null) {
				inputStream = connectionSocket.getInputStream();
				Scanner scanner = new Scanner(inputStream).useDelimiter(" ");
				if(scanner.hasNext()) {
					messageType = scanner.next();
				}
				if(messageType.equals("RFCQuery")) {
					completeRFCIndex = "";
					for(RFCIndex rfcIndex : rfcDatabase) {
						if(rfcIndex.getHostnameOfPeerContainingRFC().equals(hostname)) {
							completeRFCIndex += String.valueOf(rfcIndex.getRfcNumber()) + " " + rfcIndex.getRfcTitle() + " " 
												+ rfcIndex.getHostnameOfPeerContainingRFC() + " ";
						}	
						
					}
					outputStream = new DataOutputStream(connectionSocket.getOutputStream());
					if(completeRFCIndex != null && !completeRFCIndex.isEmpty()) {
						outputStream.writeBytes(completeRFCIndex);
						completeRFCIndex = "";
					}
				}
				else if(messageType.equals("GetRFC")) {
					if(scanner.hasNext()) {
						requiredRFCNumber = Integer.parseInt(scanner.next());
					}
						for(RFCIndex rfcIndex : rfcDatabase) {
							if(requiredRFCNumber == rfcIndex.getRfcNumber()) {
								File file = new File(SEND_FROM_FILE_PATH + "\\" + requiredRFCNumber + ".pdf");
								byte [] byteArray = new byte[(int) file.length()];
								fileInputStream = new FileInputStream(file);
								bufferedInputStream = new BufferedInputStream(fileInputStream);
								bufferedInputStream.read(byteArray, 0, byteArray.length);
								outputStream = new DataOutputStream(connectionSocket.getOutputStream());
								outputStream.write(byteArray, 0, byteArray.length);
								outputStream.flush();
							}
						}
					
					
				}
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
			if(fileInputStream != null) {
				fileInputStream.close();
			}
			if(bufferedInputStream != null) {
				bufferedInputStream.close();
			}
			if(connectionSocket != null) {
				connectionSocket.close();
			}
		}
	}
	
	public void run() {
		
	}
}
	
/*
 * Scheduler class which extends Timer class created to perform periodic tasks
 * like keepalives and asking the user whether he/she wants to continue or leave. 
 */
private class PeriodicTasks extends TimerTask {
	
	public void run() {
			
		try {
			/*
			 * Sends keepalive messages to update its TTL with the RS. 
			 */
			keepAliveCycle();
			
			/*
			 * Updates TTL values in RFC database for RFC indexes received
			 * from other peers.
			 */
			int index = 0;
			for(RFCIndex rfcIndex : rfcDatabase) {
				if(!rfcIndex.getHostnameOfPeerContainingRFC().equals("P5")) {
					if(rfcIndex.getTtl() == 0) {
						rfcDatabase.remove(index);
						index++;
						continue;
					}
					else {
						rfcIndex.setTtl((rfcIndex.getTtl() - 30));
						index++;
						continue;
					}
				}
				index++;
			}
			index = 0;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
	public void registerCycle() throws IOException {
		
		/*
		 * Registration cycle. Opens socket with server, sends hostname and RFC server port 
		 * and receives a cookie number in response
		 */
		Socket clientSocket = null;
		DataOutputStream outputStream = null;
		InputStream inputStream = null;
				try {
					clientSocket = new Socket("127.0.0.1", 65423);
					outputStream = new DataOutputStream(clientSocket.getOutputStream());
					portNumberOfRFCServerOfPeer = 65405;
					registrationMessageToRS = "Register" + " " + hostname + " " + portNumberOfRFCServerOfPeer + " ";
					System.out.println("Register " + hostname + "P2P-DI/1.0");
					System.out.println("Host: Registration Server");
					System.out.println("RFC server port of P5 is " + portNumberOfRFCServerOfPeer + "\n");
					outputStream.writeBytes(registrationMessageToRS);
					
					//Receiving cookie number from RS
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
	
	public void getActivePeersFromRS() throws IOException {
		
		/*
		 * Query message to get active peers list
		 */
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
					 * Should put a call to registerCycle(). The client may leave and then cookieNumber would be made 0.
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
							activePeersDatabase.add(new ActivePeerDetailsForP5(receivedHostname, receivedPortNumberOfRFCServerOfActivePeer));
							counter = 0;
						}
					}
					if(numberOfActivePeersReceived > 0)
						System.out.println("P2P-DI/1.0 200 Active peers list received successfully \n");
					if(numberOfActivePeersReceived == 0) {
						System.out.println("Sorry. No active peers found.");
					}
					
					for(ActivePeerDetailsForP5 activePeerDetailsForP5 : activePeersDatabase) {
						System.out.println(activePeerDetailsForP5.getReceivedHostname());
						System.out.println(activePeerDetailsForP5.getReceivedPortNumberOfRFCServerOfActivePeer());
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
		if(activePeersDatabase.size() != 0) {
			for(ActivePeerDetailsForP5 activePeerDetailsForP5 : activePeersDatabase) {
				try {
					/*
					 * Opens a client socket and establishes connection 
					 * with one of the active peers present in the peer index.
					 */
					clientSocket = new Socket("127.0.0.1", (int) activePeerDetailsForP5.getReceivedPortNumberOfRFCServerOfActivePeer());
					outputStream = new DataOutputStream(clientSocket.getOutputStream());
					System.out.println("GET RFC-Index P2P-DI/1.0");
					System.out.println("Host: " + activePeerDetailsForP5.getReceivedHostname());
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
		for (ActivePeerDetailsForP5 activePeerDetailsForP5 : activePeersDatabase) {
			if(activePeerDetailsForP5.getReceivedHostname().equals("P0")) {
				rfcServerPortOfP0 = activePeerDetailsForP5.getReceivedPortNumberOfRFCServerOfActivePeer();
			}
			if(activePeerDetailsForP5.getReceivedHostname().equals("P1")) {
				rfcServerPortOfP1 = activePeerDetailsForP5.getReceivedPortNumberOfRFCServerOfActivePeer();
			}
			if(activePeerDetailsForP5.getReceivedHostname().equals("P2")) {
				rfcServerPortOfP2 = activePeerDetailsForP5.getReceivedPortNumberOfRFCServerOfActivePeer();
			}
			if(activePeerDetailsForP5.getReceivedHostname().equals("P3")) {
				rfcServerPortOfP3 = activePeerDetailsForP5.getReceivedPortNumberOfRFCServerOfActivePeer();
			}
			if(activePeerDetailsForP5.getReceivedHostname().equals("P4")) {
				rfcServerPortOfP4 = activePeerDetailsForP5.getReceivedPortNumberOfRFCServerOfActivePeer();
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

public void getRFCFileFromOtherPeers() throws UnknownHostException, IOException {
	
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
	FileWriter fileWriter = new FileWriter("CumulativeTimeForP5");
	BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
	retrieveRFCServerPortsOfActivePeers();
	
	try {
		if(rfcDatabase.size() > 0) {
			for(RFCIndex rfcIndex : rfcDatabase) {
				if(rfcIndex.getHostnameOfPeerContainingRFC().equals("P0")) {
					startTimeToReceiveRFCFile = System.nanoTime();
					clientSocket = new Socket("127.0.0.1", (int) rfcServerPortOfP0);
					outputStream = new DataOutputStream(clientSocket.getOutputStream());
					
					requestForRFCToPeer(rfcIndex.getRfcNumber(), rfcIndex.getHostnameOfPeerContainingRFC(), outputStream );
					
					inputStream = clientSocket.getInputStream();
					storeRFCinFileOnReception(inputStream, rfcIndex.getRfcNumber());
					endTimeToReceiveRFCFile = System.nanoTime();
					timeElapsedToDownloadRFCFile = (endTimeToReceiveRFCFile - startTimeToReceiveRFCFile)/ 1000000000.0;
					cumulativeTimeToDownloadAllRFC += timeElapsedToDownloadRFCFile;
					bufferedWriter.write(String.valueOf(cumulativeTimeToDownloadAllRFC) + "\n");
				}
				else if(rfcIndex.getHostnameOfPeerContainingRFC().equals("P1")) {
					startTimeToReceiveRFCFile = System.nanoTime();
					clientSocket = new Socket("127.0.0.1", (int) rfcServerPortOfP1);
					outputStream = new DataOutputStream(clientSocket.getOutputStream());
					
					requestForRFCToPeer(rfcIndex.getRfcNumber(), rfcIndex.getHostnameOfPeerContainingRFC(), outputStream );
					
					inputStream = clientSocket.getInputStream();
					storeRFCinFileOnReception(inputStream, rfcIndex.getRfcNumber());
					endTimeToReceiveRFCFile = System.nanoTime();
					timeElapsedToDownloadRFCFile = (endTimeToReceiveRFCFile - startTimeToReceiveRFCFile)/ 1000000000.0;
					cumulativeTimeToDownloadAllRFC += timeElapsedToDownloadRFCFile;
					bufferedWriter.write(String.valueOf(cumulativeTimeToDownloadAllRFC) + "\n");
				}
				else if(rfcIndex.getHostnameOfPeerContainingRFC().equals("P2")) {
					startTimeToReceiveRFCFile = System.nanoTime();
					clientSocket = new Socket("127.0.0.1", (int) rfcServerPortOfP2);
					outputStream = new DataOutputStream(clientSocket.getOutputStream());
					requestForRFCToPeer(rfcIndex.getRfcNumber(), rfcIndex.getHostnameOfPeerContainingRFC(), outputStream );
					
					inputStream = clientSocket.getInputStream();
					storeRFCinFileOnReception(inputStream, rfcIndex.getRfcNumber());
					endTimeToReceiveRFCFile = System.nanoTime();
					timeElapsedToDownloadRFCFile = (endTimeToReceiveRFCFile - startTimeToReceiveRFCFile)/ 1000000000.0;
					cumulativeTimeToDownloadAllRFC += timeElapsedToDownloadRFCFile;
					bufferedWriter.write(String.valueOf(cumulativeTimeToDownloadAllRFC) + "\n");
				}
				else if(rfcIndex.getHostnameOfPeerContainingRFC().equals("P3")) {
					startTimeToReceiveRFCFile = System.nanoTime();
					clientSocket = new Socket("127.0.0.1", (int) rfcServerPortOfP3);
					outputStream = new DataOutputStream(clientSocket.getOutputStream());
					requestForRFCToPeer(rfcIndex.getRfcNumber(), rfcIndex.getHostnameOfPeerContainingRFC(), outputStream );
					
					inputStream = clientSocket.getInputStream();
					storeRFCinFileOnReception(inputStream, rfcIndex.getRfcNumber());
					endTimeToReceiveRFCFile = System.nanoTime();
					timeElapsedToDownloadRFCFile = (endTimeToReceiveRFCFile - startTimeToReceiveRFCFile)/ 1000000000.0;
					cumulativeTimeToDownloadAllRFC += timeElapsedToDownloadRFCFile;
					bufferedWriter.write(String.valueOf(cumulativeTimeToDownloadAllRFC) + "\n");
				}
				else if(rfcIndex.getHostnameOfPeerContainingRFC().equals("P4")) {
					startTimeToReceiveRFCFile = System.nanoTime();
					clientSocket = new Socket("127.0.0.1", (int) rfcServerPortOfP4);
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
	
public void keepAliveCycle() throws IOException {
	Socket clientSocket = null;
	DataOutputStream outputStream = null;
	try {
		clientSocket = new Socket("127.0.0.1", 65423);
		outputStream = new DataOutputStream(clientSocket.getOutputStream());
		keepAliveMessageToRS = "KeepAlive" + " " + hostname + " " + cookieNumber + " ";
		System.out.println("Keepalive P2P-DI/1.0");
		System.out.println("Host: Registration server");
		System.out.println("OS: " + System.getProperty("os.name") + "\n");
		outputStream.writeBytes(keepAliveMessageToRS);	
	} 
	catch (Exception e) {
		e.printStackTrace();
	}
	finally {
		if(outputStream != null) {
			outputStream.close();
		}
		if(clientSocket != null) {
			clientSocket.close();
		}
	}
}

public void addRFCInIndexForP2P() {
	rfcDatabase.add(new RFCIndex(8208, "BGPsec_Algorithms,_Key_Formats,_and_Signature_Formats", "P5", 7200));
	rfcDatabase.add(new RFCIndex(8207, "BGPsec_Operational_Considerations", "P5", 7200));
	rfcDatabase.add(new RFCIndex(8206, "BGPsec_Considerations_for_Autonomous_System_(AS)_Migration", "P5", 7200));
	rfcDatabase.add(new RFCIndex(8205, "BGPsec_Protocol_Specification", "P5", 7200));
	rfcDatabase.add(new RFCIndex(8204, "Benchmarking_Virtual_Switches_in_the_Open_Platform_for_NFV_(OPNFV).", "P5", 7200));
	rfcDatabase.add(new RFCIndex(8203, "BGP_Administrative_Shutdown_Communication", "P5", 7200));
	rfcDatabase.add(new RFCIndex(8202, "IS-IS_Multi-Instance", "P5", 7200));
	rfcDatabase.add(new RFCIndex(8201, "Path_MTU_Discovery_for_IP_version_6", "P5", 7200));
	rfcDatabase.add(new RFCIndex(8200, "Internet_Protocol,_Version_6_(IPv6)_Specification", "P5", 7200));
	rfcDatabase.add(new RFCIndex(8199, "YANG_Module_Classification", "P5", 7200));
}

public void leaveCycle() throws IOException {
	Socket clientSocket = null;
	DataOutputStream outputStream = null;
	InputStream inputStream = null;
	try {
		clientSocket = new Socket("127.0.0.1", 65423);
		outputStream = new DataOutputStream(clientSocket.getOutputStream());
		leaveMessageToRS = "Leave" + " " + hostname + " " + cookieNumber + " ";
		System.out.println("Leave P2P-DI/1.0");
		System.out.println("Host: Registration server");
		System.out.println("OS: " + System.getProperty("os.name") + "\n");
		outputStream.writeBytes(leaveMessageToRS);	
		
		inputStream = clientSocket.getInputStream();
		Scanner scanner = new Scanner(inputStream).useDelimiter(" ");
		if(scanner.hasNext()) {
			responseToLeaveFromRS = scanner.next();
		}
		if(responseToLeaveFromRS.equalsIgnoreCase("LeaveSuccess")) {
			System.out.println("P2P-DI/1.0 200 Leave successful for host " + hostname + "\n");
		}
	} 
	catch (Exception e) {
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

	public static void main(String[] args) throws IOException {
		
		P5 p5 = new P5();
		Scanner reader = new Scanner(System.in);
		
		System.out.println("Please press 'y' to register with the registration server");
		p5.registration = reader.next();
		if(p5.registration.equalsIgnoreCase("y")) {
			p5.registerCycle();
		}
		/*
		 * Creating a thread in which RFC server port of the client would be exposed.
		 * The RFC server of the client would be running always in the background.
		 * The RFC server would keep accepting requests for RFC index or RFC file
		 * and inside this thread there would be multiple threads in which each thread 
		 * would perform a task that needs to take place after a connectionSocket is made. 
		 * Method in which RFC server port of peer is exposed and
		 * an ExecutorService interface is used in order to support multiple 
		 * requests simultaneously. This assigns the task written in
		 * ThreadToSendRFCIndexOrRFCToPeer(connectionSocket) to a thread from the
		 * pool and continues with the execution without waiting for the
		 * task to get completed i.e puts the task in background and goes into
		 * the listening state to accept a connection from another client.
		 */
		Runnable r1 = new Runnable() {
			
			@Override
			public void run() {
				ServerSocket rfcServerSocketForP5 = null;
				try {
					rfcServerSocketForP5 = new ServerSocket(65405);
					ExecutorService executorService = Executors.newCachedThreadPool();
					while(true) {
						Socket connectionSocket = rfcServerSocketForP5.accept();
						Runnable r2 = p5.new ThreadToSendRFCIndexOrRFCToPeer(connectionSocket);
						executorService.submit(r2);
						continue;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					try {
						rfcServerSocketForP5.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		new Thread(r1).start();
		
		System.out.println("Do you want to carry out Centralized file distribution (Task 1) or P2P file distribution (Task 2)?");
		System.out.println("Please press 1 to execute task 1 and 2 to execute task 2.");
		p5.task = reader.nextInt();
		switch(p5.task) {
		case 1: break;
		case 2: p5.addRFCInIndexForP2P();
				break;
		}
		
		System.out.println("Please press 'y' to get list of active peers from Registration Server");
		p5.pQuery = reader.next();
		if(p5.pQuery.equalsIgnoreCase("y")) {
			p5.getActivePeersFromRS();
		}
		
		System.out.println("Please press 'y' to get RFC index from all other active peers");
		p5.rfcIndexQuery = reader.next();
		if(p5.rfcIndexQuery.equalsIgnoreCase("y")) {
			p5.getRFCIndexFromOtherPeers();
		}
		
		System.out.println("Please press 'y' to download all the RFC files");
		p5.rfcFileQuery = reader.next();
		if(p5.rfcFileQuery.equalsIgnoreCase("y")) {
			p5.getRFCFileFromOtherPeers();
		}
		
		
		Timer time = new Timer();
		PeriodicTasks periodicTasks = p5.new PeriodicTasks();
		time.schedule(periodicTasks, 0, 30000);

	}
}
