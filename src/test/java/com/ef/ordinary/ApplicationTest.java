package com.ef.ordinary;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ef.Parser;

public class ApplicationTest {

	@Test
	public void testOfVerifyArguments() {
		   
	String[] arg = {"--accesslog=c:/hello.txt", "--startDate=2017-01-01.15:00:00",
			"--duration=hourly", "--threshold=100"};	
String[] verArg = Parser.verifyArguments(arg);
	assertEquals("Path not correct", "c:/hello.txt", verArg[0]);
	assertEquals("StartDate not correct", "2017-01-01.15:00:00", verArg[1]);
	assertEquals("Duration not correct", "hourly", verArg[2]);
	assertEquals("Path not correct", "100", verArg[3]);
	
	}

}
