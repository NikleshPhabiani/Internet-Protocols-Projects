package org.ncsu.edu;

import java.util.Date;

public class PeerDetails {
	
	private String hostname;
	private int cookie;
	private Boolean activationFlag;
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

	public Boolean getActivationFlag() {
		return activationFlag;
	}

	public void setActivationFlag(Boolean activationFlag) {
		this.activationFlag = activationFlag;
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
	
	PeerDetails(String hostname,int cookie, Boolean activationFlag, int ttl, long portNumberofRFCServerOfPeer, int numberOfRegistrations, String latestRegistrationDate){
		this.hostname = hostname;
		this.cookie = cookie;
		this.activationFlag = activationFlag;
		this.ttl = ttl;
		this.portNumberofRFCServerOfPeer = portNumberofRFCServerOfPeer;
		this.numberOfRegistrations = numberOfRegistrations;
		this.latestRegistrationDate = latestRegistrationDate;
	}
}
