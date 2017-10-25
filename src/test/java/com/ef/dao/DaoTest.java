package com.ef.dao;

import static org.junit.Assert.assertEquals;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.ef.model.LogBean;

@RunWith(SpringRunner.class)
@ContextConfiguration
public class DaoTest {

	@Configuration //Test Context Configuration
	static class Config{
		@Bean
		public DriverManagerDataSource dataSources(){
			DriverManagerDataSource dataSource = new DriverManagerDataSource();
			dataSource.setDriverClassName("com.mysql.jdbc.Driver");
			dataSource.setUrl("jdbc:mysql://localhost:3306/server_logging");
			dataSource.setUsername("appuser");
			dataSource.setPassword("appuser");
			return dataSource;
		}
		
		@Bean
		public JdbcTemplate jdbcTemplate(){
			return new JdbcTemplate(dataSources());
		}

		@Bean
		LogBean logBean(){
			return new LogBean();
		}
		
		@Bean
		LogDao logDao(){
			return new LogDao();
		}
	}//end of Config static class
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private LogBean logBean;
	@Autowired
	private LogDao logDao;
	
	
	@Test
	public void testSaveLogLine() {
		LocalDateTime ldt = LocalDateTime.of(2017, 10, 23, 22, 22, 22);
		logBean.setDate(ldt);//setting DateTime field in Log_Bean
		logBean.setIp("222.222.222.222");//setting ip
		logBean.setRequest("GET / HTTP/1.1");//setting request
		logBean.setStatus("200");//setting status
		logBean.setUserAgent("USER AGENT");//setting user agent
		
		//saving test data
		logDao.saveLogLine(logBean);
		
		//Getting data from the data base. Putting them into the bean LogBean
		LogBean logBean = this.jdbcTemplate.queryForObject(
				"select date, ip, request, status, user_agent from logfile where id = ?",
				new Object[]{1L},
				new RowMapper<LogBean>(){
					public LogBean mapRow(ResultSet rs, int rowNum) throws SQLException{
						LogBean log = new LogBean();
						log.setDate(rs.getObject(1, LocalDateTime.class));
						log.setIp(rs.getString(2));
						log.setRequest(rs.getString(3));
						log.setStatus(rs.getString(4));
						log.setUserAgent(rs.getString(5));
						return log;
					}
				});
//ANOTHER WAY OF CHECKING FIELDS
//		String ip = this.jdbcTemplate.queryForObject("select ip from logfile where id = ?", 
//				new Object[]{1L}, String.class);
				assertEquals("Date is not correct",ldt, logBean.getDate()); //checking date
				assertEquals("Ip is not correct", "222.222.222.222", logBean.getIp()); //checking ip
				assertEquals("Request is not correct", "GET / HTTP/1.1", logBean.getRequest()); //checking request
				assertEquals("Status is not correct", "200", logBean.getStatus()); //checking status
				assertEquals("User Agent is not correct", "USER AGENT", logBean.getUserAgent()); //checking user_agent

	
	}
	
	@Test
	
	public void testSaveThresholdIPs(){
		String setIp1 = "111.111.111.111";
		String setIp2 = "222.222.222.222";
		String setIp3 = "333.333.333.333";
		Set<String> threshIps = null;

		threshIps = new TreeSet<>();
		threshIps.add(setIp1);
		threshIps.add(setIp2);
		threshIps.add(setIp3);
		
		//saving data to the database
		logDao.saveThresholdIPs(threshIps);
		
		String sql = "select ip from blockedip where id = ?";
		
		String ip1 = this.jdbcTemplate.queryForObject(sql, new Object[]{1L}, String.class);
		System.out.println("ip1 = " + ip1);
		
		String ip2 = this.jdbcTemplate.queryForObject(sql, new Object[]{2L}, String.class);
		System.out.println("ip2 = " + ip2);

		String ip3 = this.jdbcTemplate.queryForObject(sql, new Object[]{3L}, String.class);
		System.out.println("ip3 = " + ip3);
		
		
		assertEquals("Ip1 is not correct", setIp1, ip1);
		assertEquals("Ip2 is not correct", setIp2, ip2);
		assertEquals("Ip3 is not correct", setIp3, ip3);
	}
	

}
