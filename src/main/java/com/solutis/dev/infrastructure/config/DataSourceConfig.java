package com.solutis.dev.infrastructure.config;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.DelegatingDataSource;

@Configuration
@Profile({"postgres", "prod"})
public class DataSourceConfig {

    @Bean
    public DataSource primaryDataSource(
            @Value("${app.datasource.primary.url}") String url,
            @Value("${app.datasource.primary.username}") String username,
            @Value("${app.datasource.primary.password}") String password,
            @Value("${app.datasource.primary.driver-class-name}") String driverClassName) {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    @Bean
    public DataSource fallbackDataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            @Value("${spring.datasource.driver-class-name}") String driverClassName) {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("primaryDataSource") DataSource primaryDataSource,
            @Qualifier("fallbackDataSource") DataSource fallbackDataSource) {
        return new FailoverDataSource(primaryDataSource, fallbackDataSource);
    }

    public static class FailoverDataSource extends DelegatingDataSource {

        private static final Logger log = LoggerFactory.getLogger(FailoverDataSource.class);

        private final DataSource fallbackDataSource;

        public FailoverDataSource(DataSource primaryDataSource, DataSource fallbackDataSource) {
            super(primaryDataSource);
            this.fallbackDataSource = fallbackDataSource;
        }

        @Override
        public Connection getConnection() throws SQLException {
            try {
                return super.getConnection();
            } catch (SQLException e) {
                log.warn("Falha ao conectar no datasource primário, usando fallback", e);
                return fallbackDataSource.getConnection();
            }
        }

        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            try {
                return super.getConnection(username, password);
            } catch (SQLException e) {
                log.warn("Falha ao conectar no datasource primário, usando fallback", e);
                return fallbackDataSource.getConnection(username, password);
            }
        }
    }
}
