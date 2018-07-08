package org.ncsu.edu;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegistrationServer{
	private static String activePeersList = "";	//else printing with null
	private static LinkedList<PeerDetails> rsDatabase = new LinkedList<PeerDetails>();
	private static String hostname;
	private static long portNumberOfRFCServerOfPeer;
	private static int counter = 0;
	private static int cookieNumberForPeer = 1000;
	private static int cookieNumberFromPeer;
	private static int numberOfRegistrations = 1;
	private static Boolean registerCycle = false;
	private static Boolean queryCycle = false;
	private static Boolean keepAlive = false;
	private static Boolean leaveCycle = false;
	private static String messageType;
	private static final DateFormat dateFormat = new SimpleDateFormat("MM/dd/YYYY HH:mm:ss");
	private static int numberOfActivePeersSentToARequestingPeer = 0;
	Date date = new Date();
	
	private class PeerDetails{
		private String hostname;
		private int cookie;
		private Boolean isActive;
		private int ttl;
		private long portNumberofRFCServerOfPeer;
		private int numberOfRegistrations;
		public String getHostname() {
			return hostname;
		}

		public void setHostname(String hostname) {
			this.hostname = hostname;
		}

		public int getCookie() {
			return cookie;
		}

		public void setCookie(int cookie) {
			this.cookie = cookie;
		}

		public Boolean getIsActive() {
			return isActive;
		}

		public void setIsActive(Boolean isActive) {
			this.isActive = isActive;
		}

		public int getTtl() {
			return ttl;
		}

		public void setTtl(int ttl) {
			this.ttl = ttl;
		}

		public long getPortNumberofRFCServerOfPeer() {
			return portNumberofRFCServerOfPeer;
		}

		public void setPortNumberofRFCServerOfPeer(long portNumberofRFCServerOfPeer) {
			this.portNumberofRFCServerOfPeer = portNumberofRFCServerOfPeer;
		}

		public int getNumberOfRegistrations() {
			return numberOfRegistrations;
		}

		public void setNumberOfRegistrations(int numberOfRegistrations) {
			this.numberOfRegistrations = numberOfRegistrations;
		}

		public String getLatestRegistrationDate() {
			return latestRegistrationDate;
		}

		public void setLatestRegistrationDate(String latestRegistrationDate) {
			this.latestRegistrationDate = latestRegistrationDate;
		}

		private String latestRegistrationDate;
		
		PeerDetails(String hostname,int cookie, Boolean isActive, int ttl, long portNumberofRFCServerOfPeer, int numberOfRegistrations, String latestRegistrationDate){
			this.hostname = hostname;
			this.cookie = cookie;
			this.isActive = isActive;
			this.ttl = ttl;
			this.portNumberofRFCServerOfPeer = portNumberofRFCServerOfPeer;
			this.numberOfRegistrations = numberOfRegistrations;
			this.latestRegistrationDate = latestRegistrationDate;
		}
	}
	
	private class PeriodicTasks extends TimerTask {
		public void run() {
			for(PeerDetails peerDetails: rsDatabase) {
				peerDetails.setTtl((peerDetails.getTtl() - 30));
//				System.out.format("Current TTL for host %s is %s \n", peerDetails.getHostname(), String.valueOf(peerDetails.getTtl()));
			}
		}
	}
		
	public static void main(String [] args) throws IOException, InterruptedException {
		RegistrationServer registrationServer = new RegistrationServer();
		Runnable runnable = new Runnable() {
			
			@Override
			public void run() {
				DataOutputStream outputStream = null;
				ServerSocket registrationServerSocket = null;
				try {
					registrationServerSocket = new ServerSocket(65423);
					Socket connectionSocket = null;
					try {
						while(true) {
							connectionSocket = registrationServerSocket.accept();
							InputStream inputStream = connectionSocket.getInputStream();
							Scanner scanner = new Scanner(inputStream).useDelimiter(" ");
							if(scanner.hasNext()) {
								messageType = scanner.next();
							}
							while(scanner.hasNext()) {
								if(messageType.equalsIgnoreCase("Register") && !registerCycle) {
									System.out.println("Hello first time user. You are trying to connect to the registration server at 65423.");
									System.out.println("Please wait while you are being registered");
									System.out.println("Registering...");
									registerCycle = true;
									continue;
								}
								if(registerCycle && counter == 0) {
									hostname = scanner.next();
									counter++;
									continue;
								}
								if(registerCycle && counter == 1) {
									portNumberOfRFCServerOfPeer = Integer.parseInt(scanner.next());
									
									/*
									 * Registering first time users.
									 */
									rsDatabase.add(registrationServer.new PeerDetails(hostname, cookieNumberForPeer, true, 7200, portNumberOfRFCServerOfPeer, numberOfRegistrations, dateFormat.format(new Date())));
									System.out.format("Peer %s registered successfully.\n", hostname);
									outputStream = new DataOutputStream(connectionSocket.getOutputStream());
									outputStream.writeBytes(String.valueOf(cookieNumberForPeer)+ " ");
									outputStream.close();
									cookieNumberForPeer++;
									
									registerCycle = false;
									continue;
								}
								
								
								if(messageType.equals("PQuery") && !queryCycle) {
									queryCycle = true;
									continue;
								}
								if(queryCycle && counter == 0) {
									hostname = scanner.next();
									System.out.println("List of active peers getting forwarded to " + hostname + "\n");
									counter++;
									continue;
								}
								
								if(queryCycle && counter == 1) {
									cookieNumberFromPeer = Integer.parseInt(scanner.next());
									
									//sending active peer list to when PQuery is received 
									for(PeerDetails peerDetails : rsDatabase) {
										if(peerDetails.getCookie() != cookieNumberFromPeer && peerDetails.getIsActive()) {
											activePeersList += peerDetails.getHostname() + " " + peerDetails.getPortNumberofRFCServerOfPeer() + " ";
											numberOfActivePeersSentToARequestingPeer++;
										}
									}
									outputStream = new DataOutputStream(connectionSocket.getOutputStream());
									if(activePeersList != null && !activePeersList.isEmpty()) {
										outputStream.writeBytes(String.valueOf(numberOfActivePeersSentToARequestingPeer) + " " + activePeersList);
										outputStream.close();
									}
									else {
										outputStream.writeBytes(String.valueOf(numberOfActivePeersSentToARequestingPeer) + " ");
										outputStream.close();
									}
									
									numberOfActivePeersSentToARequestingPeer = 0;
									activePeersList = "";		//else contains active peer list of previous peers also
									queryCycle = false;
									continue;
								}
								
								
								if(messageType.equals("KeepAlive") && !keepAlive) {
									keepAlive = true;
									continue;
								}
								
								if(keepAlive && counter == 0) {
									hostname = scanner.next();
									System.out.println("KeepAlive message received. Updating TTL value for " + hostname + "\n");
									counter++;
									continue;
								}
								
								if(keepAlive && counter == 1) {
									cookieNumberFromPeer = Integer.parseInt(scanner.next());
									for(PeerDetails peerDetails : rsDatabase) {
										if(cookieNumberFromPeer == peerDetails.getCookie()) {
											peerDetails.setTtl(7200);
										}
									}
									keepAlive = false;
									continue;
								}
								
								if(messageType.equals("Leave") && !leaveCycle) {
									leaveCycle = true;
									continue;
								}
								
								if(leaveCycle && counter == 0) {
									hostname = scanner.next();
									System.out.println("Leave request received from " + hostname + "\n");
									counter++;
									continue;
								}
								
								if(leaveCycle && counter == 1) {
									cookieNumberFromPeer = Integer.parseInt(scanner.next());
									for(PeerDetails peerDetails : rsDatabase) {
										if(cookieNumberFromPeer == peerDetails.getCookie()) {
											peerDetails.setIsActive(false);
											outputStream = new DataOutputStream(connectionSocket.getOutputStream());
											outputStream.writeBytes("LeaveSuccess" + " ");
											outputStream.close();
										}
									}
									leaveCycle = false;
									continue;
								}
							}
							counter = 0;
							if(inputStream != null) {
								inputStream.close();
							}
							if(connectionSocket != null) {
								connectionSocket.close();
							}
						}
						
					}
					catch (IOException e) {
					System.out.println(e);
					}
					finally {
						if(connectionSocket != null) {
							connectionSocket.close();
						}
					}
				} 
				catch (IOException e1) {
					e1.printStackTrace();
				}
				finally {
					if(registrationServerSocket != null) {
						try {
							registrationServerSocket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
		new Thread(runnable).start();
		Timer time = new Timer();
		PeriodicTasks periodicTasks = registrationServer.new PeriodicTasks();
		time.schedule(periodicTasks, 0, 30000);
	}
}
