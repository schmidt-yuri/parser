package com.ef.dao;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import com.ef.model.LogBean;

public class LogDao {
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
//	@Autowired
//	LogBean logBean;
	
	public void saveLogLine(LogBean lb){
		
	LocalDateTime ldt = lb.getDate();
	String ip = lb.getIp();
	String request = lb.getRequest();
	String status = lb.getStatus();
	String userAgent = lb.getUserAgent();
	
	String sql = "insert into logfile (rqdate, ip, request, status, user_agent) values (?, ?, ?, ?, ?)";
	
	this.jdbcTemplate.update(sql, ldt, ip, request, status, userAgent);
	}
	
	public void saveThresholdIPs(Set<String> ipc){
		
		String info = "BLOCKED as it has exceeded a permitted number of requests for the given duration";
		for(String s: ipc){
		this.jdbcTemplate.update("insert into blockedip (ip, comments) values (?, ?)", s, info);	
		}
		
	}

}
