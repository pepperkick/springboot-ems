package com.pepperkick.ems.batch.config;

import com.pepperkick.ems.batch.dto.Employee;
import com.pepperkick.ems.batch.mapper.EmployeeRowMapper;
import com.pepperkick.ems.batch.processor.EmployeeProcessor;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class BatchConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Autowired
    public BatchConfiguration(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
        this.jobBuilderFactory = jobBuilderFactory;
        this.stepBuilderFactory = stepBuilderFactory;
    }

    @Bean
    public Job processJob() {
        return jobBuilderFactory.
            get("processJob").
            incrementer(new RunIdIncrementer()).
            flow(processStep()).
            end().
            build();
    }

    @Bean
    public Step processStep() {
        return stepBuilderFactory.
            get("processStep").
            <Employee, String> chunk(1).
            reader(createItemReader()).
            writer(createItemWriter()).
            processor(new EmployeeProcessor()).
            build();
    }

    private ItemReader<Employee> createItemReader() {
        JdbcCursorItemReader<Employee> itemReader = new JdbcCursorItemReader<>();

        itemReader.setDataSource(getDataSource());
        itemReader.setSql("SELECT * FROM employee ORDER BY id");
        itemReader.setRowMapper(new EmployeeRowMapper());

        return itemReader;
    }

    private ItemWriter<String> createItemWriter() {
        FlatFileItemWriter<String> itemWriter = new FlatFileItemWriter<>();
        ResourceLoader resourceLoader = new DefaultResourceLoader();

        itemWriter.setResource(resourceLoader.getResource("file:target/output/employee.txt"));
        itemWriter.setLineAggregator(new PassThroughLineAggregator<>());

        return itemWriter;
    }

    private DataSource getDataSource() {
        Dotenv dotenv = Dotenv.load();

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://" +
            dotenv.get("MYSQL_HOSTNAME", "localhost") +
            ":" +
            dotenv.get("MYSQL_PORT", "3306") +
            "/" +
            dotenv.get("MYSQL_DATABASE", "ems")
        );
        dataSource.setUsername(dotenv.get("MYSQL_USERNAME", "root"));
        dataSource.setPassword(dotenv.get("MYSQL_PASSWORD", "root"));

        return dataSource;
    }
}
