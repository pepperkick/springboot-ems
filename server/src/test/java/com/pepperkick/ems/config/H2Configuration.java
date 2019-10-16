package com.pepperkick.ems.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.pepperkick.ems.repository")
@PropertySource(value = "application.properties")
@EnableTransactionManagement
public class H2Configuration { }
