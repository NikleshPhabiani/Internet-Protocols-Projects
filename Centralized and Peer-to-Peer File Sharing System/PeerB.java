package org.ncsu.edu;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PeerB {

	private static String hostname = "PB";
	private String receivedHostname;
	private long portNumberOfRFCServerOfPeer;
	private long receivedPortNumberOfRFCServerOfActivePeer;
	private String registrationMessageToRS;
	private String queryMessageToRS;
	private String rfcQueryMessageToOtherPeers;
	private int cookieNumber = 0;
	private int counter = 0;
	private int numberOfActivePeersReceived;
	private LinkedList<ActivePeerDetailsForPB> activePeersDatabase = new LinkedList<ActivePeerDetailsForPB>();
	private LinkedList<RFCIndex> rfcDatabase = new LinkedList<RFCIndex>();
	private String messageType;
	private String completeRFCIndex = "";
	private int receivedRFCNumber;
	private String receivedRFCTitle;
	private String receivedHostnameOfPeerContainingRFC;
	private long rfcServerPortOfP1;
	private long rfcServerPortOfP2;
	private long rfcServerPortOfP3;
	private long rfcServerPortOfP4;
	private long rfcServerPortOfP5;
	private String getRFCFileMessageToPeers;
	private static final String RECEIVE_ON_FILE_PATH = "C:\\Users\\nikle\\Downloads\\Internet Protocols ECE 573\\RFCP0";
	private static final String SEND_FROM_FILE_PATH = "C:\\Users\\nikle\\Downloads\\Internet Protocols ECE 573\\RFCP0";
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
	
	/*	Class which stores values for hostname and port number of RFC server of active peers.
	 *	A linked list of this class's data type would be made and details for each active peer
	 *	would be stored in a single node. 
	 */
	private class ActivePeerDetailsForPB {
		
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
		
		ActivePeerDetailsForPB(String receivedHostname, long receivedPortNumberOfRFCServerOfActivePeer){
			
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
									File file = new File(SEND_FROM_FILE_PATH + "\\" + rfcIndex.getRfcNumber() + ".pdf");
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
					portNumberOfRFCServerOfPeer = 65407;
					registrationMessageToRS = "Register" + " " + hostname + " " + portNumberOfRFCServerOfPeer + " ";
					System.out.println("Register " + hostname + "P2P-DI/1.0");
					System.out.println("Host: Registration Server");
					System.out.println("RFC server port of Peer B is " + portNumberOfRFCServerOfPeer + "\n");
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
	
	public void addRFCInIndex() {
		rfcDatabase.add(new RFCIndex(8277, "Using_BGP_to_Bind_MPLS_Labels_to_Address_Prefixes", "PB", 7200));
		rfcDatabase.add(new RFCIndex(8269, "The_ARIA_Algorithm_and_Its_Use_with_the_Secure_Real-Time_Transport_Protocol_(SRTP)", "PB", 7200));
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
				System.out.println("P2P-DI/1.0 200 Leave successful for host " + hostname + "at" + new Date());
//				System.exit(0);
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
	
	public static void main(String[] args) throws InterruptedException, IOException {
		PeerB peerB = new PeerB();
		Scanner reader = new Scanner(System.in);
		
		System.out.println("Please press 'y' to register with the registration server");
		peerB.registration = reader.next();
		if(peerB.registration.equalsIgnoreCase("y")) {
			peerB.registerCycle();
		}
		
		Runnable r1 = new Runnable() {
			
			@Override
			public void run() {
				ServerSocket rfcServerSocketForPB= null;
				try {
					rfcServerSocketForPB = new ServerSocket(65407);
					ExecutorService executorService = Executors.newCachedThreadPool();
					while(true) {
						Socket connectionSocket = rfcServerSocketForPB.accept();
						Runnable r2 = peerB.new ThreadToSendRFCIndexOrRFCToPeer(connectionSocket);
						executorService.submit(r2);
						continue;
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					try {
						rfcServerSocketForPB.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		new Thread(r1).start();
		peerB.addRFCInIndex();
		
		Thread.sleep(40000);
		peerB.leaveCycle();
	}

}
