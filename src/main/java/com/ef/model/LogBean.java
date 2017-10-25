package com.ef.model;

import java.time.LocalDateTime;

public class LogBean {
private Long id;
private LocalDateTime date;
private String ip;
private String request;
private String status;
private String userAgent;

public Long getId() {
	return id;
}
public void setId(Long id) {
	this.id = id;
}

public LocalDateTime getDate() {
	return date;
}
public void setDate(LocalDateTime date) {
	this.date = date;
}
public String getIp() {
	return ip;
}
public void setIp(String ip) {
	this.ip = ip;
}
public String getRequest() {
	return request;
}
public void setRequest(String request) {
	this.request = request;
}
public String getStatus() {
	return status;
}
public void setStatus(String status) {
	this.status = status;
}
public String getUserAgent() {
	return userAgent;
}
public void setUserAgent(String userAgent) {
	this.userAgent = userAgent;
}
}
