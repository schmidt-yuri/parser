package com.ef.model;

public class IpCounter {
	private String ip;
	private int counter = 0;
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getCounter() {
		return counter;
	}
	public void setCounter() {
		counter++;
	}

}
