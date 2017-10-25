package com.ef.config;

import java.beans.PropertyVetoException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import com.ef.dao.LogDao;
import com.ef.model.LogBean;
import com.mchange.v2.c3p0.ComboPooledDataSource;

@Configuration
@ComponentScan("com.ef")
public class AppConfig {
	
//	@Bean
//	public DriverManagerDataSource dataSources(){
//		DriverManagerDataSource dataSource = new DriverManagerDataSource();
//		dataSource.setDriverClassName("com.mysql.jdbc.Driver");
//		dataSource.setUrl("jdbc:mysql://localhost:3306/server_logging");
//		dataSource.setUsername("appuser");
//		dataSource.setPassword("appuser");
//		return dataSource;
//	}
	
	@Bean
	public ComboPooledDataSource dataSources(){
		ComboPooledDataSource cpds = new ComboPooledDataSource();
		try {
			cpds.setDriverClass("com.mysql.jdbc.Driver");
		} catch (PropertyVetoException e) {
			System.out.println("There is problem with driver for dataSource");
			e.printStackTrace();
		}
		cpds.setJdbcUrl("jdbc:mysql://localhost:3306/server_logging");
		cpds.setUser("appuser");
		cpds.setPassword("appuser");
		
		cpds.setMinPoolSize(1);
		cpds.setAcquireIncrement(5);
		cpds.setMaxPoolSize(20);
		cpds.setMaxStatements(180);
		return cpds;
		
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
}
