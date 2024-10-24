package com.triple.backend.batch.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {

    @Bean(name = "batchDataSource")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource.batch.hikari")
    public DataSource batchDataSource() {
        return DataSourceBuilder
                .create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "mainDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.main.hikari")
    public DataSource mainDataSource() {
        return DataSourceBuilder
                .create()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean(name = "mainNamedParameterJdbcTemplate")
    public NamedParameterJdbcTemplate mainNamedParameterJdbcTemplate(@Qualifier("mainDataSource") DataSource mainDataSource) {
        return new NamedParameterJdbcTemplate(mainDataSource);
    }
}
