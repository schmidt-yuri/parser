package com.ef;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.ef.config.AppConfig;
import com.ef.dao.LogDao;
import com.ef.model.IpCounter;
import com.ef.model.LogBean;

/**
 * A parser that parses web server access log file, loads the log to MySQL
 * and checks if a given IP makes more than a certain number of requests
 * for the given duration.
 * 
 * @author Yuri Schmidt
 *
 */
public class Parser {
	
	private static Set<IpCounter> ipset = new HashSet<>();
	private static Set<String> ipOverThreshold = new HashSet<>();

	
	public static void main(String[] args) {
		
	    Logger logger = LoggerFactory.getLogger(Parser.class);

		
		//AnnotationConfigApplicationContext
		if(args.length != 4){
			System.out.println("Please, enter arguments according to the following sample "
					+ "java -jar /path/to/file/parser.jar "
					+ "--accesslog=/path/to/file --startDate=2017-01-01.13:00:00 "
					+ "--duration=hourly/dayly --threshold=100");
			return;
		}else{
			//method which validates command line arguments and parses them
			String[] arguments = verifyArguments(args);
			
			//Application context
			ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
			LogBean logBean = ctx.getBean(LogBean.class);
			LogDao logDao = ctx.getBean(LogDao.class);
	
		
			
			String pathToFile = arguments[0]; //path to file
			String dateTime = arguments[1]; //dateTime
			String duration = arguments[2];//duration
			int threshold = Integer.parseInt(arguments[3]) ;//threshold
			
//set
			
			DateTimeFormatter formatterCommandLineArg = DateTimeFormatter.ofPattern("yyyy-MM-dd.HH:mm:ss");
			DateTimeFormatter formatterInLog = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
			LocalDateTime dtCommandLineArg = LocalDateTime.parse(dateTime, formatterCommandLineArg);
			LocalDateTime addedDateTime = null;
			
			switch(duration){
			case "hourly":
				addedDateTime = dtCommandLineArg.plusHours(1L);
				break;
			case "daily":
				addedDateTime = dtCommandLineArg.plusDays(1L);
				break;
			}
			
			
			
			final Path p = Paths.get(pathToFile);
			Charset charset = Charset.forName("US-ASCII");
			try (BufferedReader reader = Files.newBufferedReader(p, charset)){
				String line = null;
				while((line = reader.readLine()) !=null){//read line
					String delimiter = "\\|";//delimiter
					String[] splittedLine = line.split(delimiter);//parse line and put the parts into array
				LocalDateTime dtInLog = LocalDateTime.parse(splittedLine[0], formatterInLog);
				logBean.setDate(dtInLog);//setting DateTime field in Log_Bean
				logBean.setIp(splittedLine[1]);//setting ip
				logBean.setRequest(splittedLine[2]);//setting request
				logBean.setStatus(splittedLine[3]);//setting status
 				logBean.setUserAgent(splittedLine[4]);//setting user agent
				
				if(dtInLog.isAfter(dtCommandLineArg)&&(dtInLog.isBefore(addedDateTime))){ //extracting ips from requests that fall into the time period
					int a = 0; //flag

				if(ipset.size()>=1){	
					for(IpCounter c : ipset){
						if(c.getIp().equals(logBean.getIp())){// if there is a IpCounter obj in the Set ipset, and it's ip is equal to the one obtained from the list
							c.setCounter(); //then obj's counter is incremented
							a = 1;//and the flag is set to 1.
					break;		
						}//end of if(c.getIp()
					}//end of for
				}//end of if(ipset.size()>=1)

					if(((a == 0)||(ipset.isEmpty()))){
						IpCounter ipcounter = new IpCounter();
						ipcounter.setIp(logBean.getIp());//logBean.getIp()
						ipcounter.setCounter();
						ipset.add(ipcounter);
					//	System.out.println("ipset cycled: " + ipset.size());
					}//end of if
				
				
				}//end of if(dtInLog.isAfter(...
				}//end of while
			} catch (IOException e) {
				System.out.println("Can't read the file. The file is missing or corrupted!");
				System.err.format("IOException: %s%n", e);
				e.printStackTrace();
			return;
			}//end of catch

			//String format of LocalDateTime
			String strComLineArg = dtCommandLineArg.format(formatterInLog);
			String strAddedDateTime = addedDateTime.format(formatterInLog);

			System.out.println("IPs that made more than " + threshold + " requests");
			System.out.println("starting from " + strComLineArg + " to " + strAddedDateTime);				
				for(IpCounter c : ipset){//searching for ips which exceeded a permitted number of requests
					if(c.getCounter() >= threshold){
						//System.out.println("The IP: " + c.getIp() + " - blocked as it has exceeded a permitted number of requests for the given duration");
						System.out.println(c.getIp()+ " - " + c.getCounter() + " requests");
						ipOverThreshold.add(c.getIp());
					}//end of if
				}//end of for
//NEW THREAD SAVING IPs THAT EXCEEDED THRESHOLD
				Runnable r = ()-> {
					logDao.saveThresholdIPs(ipOverThreshold);
				//	System.out.println("IPs are saved in the data base");
									}; //saving ips in the MySQL database 
				Thread t2 = new Thread(r);
				t2.start();
			//	logger.debug("IPs to database saving  thread started");
				
//NEW THREAD  SAVING LOG TO THE DATABASE
				
				Runnable saveLog = ()-> {
					try (BufferedReader reader = Files.newBufferedReader(p, charset)){
						String line = null;
						
						while((line = reader.readLine())!=null){//read line
							String delimiter = "\\|";//delimiter
							String[] splittedLine = line.split(delimiter);//parse line and put the parts into array
						LocalDateTime dtInLog = LocalDateTime.parse(splittedLine[0], formatterInLog);
						logBean.setDate(dtInLog);//setting DateTime field in Log_Bean
						logBean.setIp(splittedLine[1]);//setting ip
						logBean.setRequest(splittedLine[2]);//setting request
						logBean.setStatus(splittedLine[3]);//setting status
		 				logBean.setUserAgent(splittedLine[4]);//setting user agent
		 				
		 				logDao.saveLogLine(logBean); //DAO
						
						}//end of WHILE
						
				} catch (IOException e) {
					System.out.println("Can't read the file. The file is missing or corrupted!");
					System.err.format("IOException: %s%n", e);
					e.printStackTrace();
				System.exit(0);
				}//end of catch
	//			logger.debug("End of saving server log file");
				System.out.println("The log file has been saved to the database");
			};//END OF Runnable
			
			Thread t = new Thread(saveLog);
			t.start();
		//	logger.debug("Log saving thread started");

			//EXIT OF THE PROGRAMM
			try(Scanner scanner = new Scanner(System.in)){
				do{
				System.out.println("Please, wait...");
				System.out.println("The programm is loading log data into the database");
				System.out.println("To quit type 'exit': ");
				String name = scanner.nextLine();
				if(name.toLowerCase().equals("exit".toLowerCase())){
					System.exit(0);
					}
				}while(true);
			}//end of try block
			
		}//end of else statement

	}//end of main method
	
	
	/**
	 * Verifies command line arguments and parses them
	 * @param arg An array of command line arguments
	 * @return An array of parsed command line arguments
	 */
	public static String[] verifyArguments(String[] arg){
		String path = arg[0];
		String startDate = arg[1];
		String duration = arg[2];
		String threshold = arg[3];
		String[] result; //auxiliary array
		String[] output = new String[4]; //the returning array
		
		String pathRegEx = "([a-zA-Z]:)?(\\/[a-zA-Z0-9_.-]+)+\\/?";
		String startDateRegEx = "((19|20)\\d\\d)-(0?[1-9]|1[012])-(0?[1-9]|[12][0-9]|3[01])\\.([2][0-3]|[0-1][0-9]|[1-9]):[0-5][0-9]:([0-5][0-9]|[6][0])$";
		String durationRegEx = "hourly|daily";
		String thresholdRegEx = "^([1-4][0-9]{0,2}|500)$";
		
		result = path.split("=");
		if(!result[0].equals("--accesslog")){
			System.out.println("Error typing the name of argument --accesslog");
			System.exit(0);
		}
		if(!Pattern.matches(pathRegEx, result[1])){
			System.out.println("Error typing file path!");
			System.exit(0);
		}
		output[0] = result[1];
		
		result = startDate.split("=");
		if (!result[0].equals("--startDate")){
			System.out.println("Error typing the name of argument --startDate");
			System.exit(0);
		}
		
		if(!Pattern.matches(startDateRegEx, result[1])){
			System.out.println("Error typing argument --startDate. Please, keep to the sample 'yyyy-MM-dd.HH:mm:ss' e.g. --startDate=2017-01-01.13:00:00");
			System.exit(0);
			}
		output[1] = result[1];

		result = duration.split("=");
		if (!result[0].equals("--duration")){
			System.out.println("Error typing the name of argument --duration");
			System.exit(0);
		}
		
		if(!Pattern.matches(durationRegEx, result[1])){
			System.out.println("Error typing argument duration. Must be --duration=daily or --duration=hourly");
			System.exit(0);
			}
		output[2] = result[1];


		result = threshold.split("=");
		if (!result[0].equals("--threshold")){
			System.out.println("Error typing the name of argument --threshold");
			System.exit(0);
		}
		
		if(!Pattern.matches(thresholdRegEx, result[1])){
			System.out.println("Error typing argument threshold. It must be not more then 500. e.g. --threshold=250");
			System.exit(0);
			}
		output[3] = result[1];
		
		
		return output;
	}//end of method verifyArguments()

}//end of Parser class
