package com.example.mysqltest.db;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DbConfig {

    private final DbProperty dbProperty;
    private final JpaProperties jpaProperties;

    // 바로 아래 routingDataSource 에서 사용할 메서드
    public DataSource createDataSource(String url) {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(url);
        hikariDataSource.setDriverClassName(dbProperty.getDriverClassName());
        hikariDataSource.setUsername(dbProperty.getUsername());
        hikariDataSource.setPassword(dbProperty.getPassword());
        return hikariDataSource;
    }

    @Bean
    public DataSource routingDataSource() {
        // 앞서 AbstractRoutingDataSource 를 상속받아 재정의한 ReplicationRoutingDataSource 생성
        ReplicationRoutingDataSource replicationRoutingDataSource = new ReplicationRoutingDataSource();

        // master와 slave 정보를 키(name), 밸류(dataSource) 형식으로 Map에 저장
        Map<Object, Object> dataSourceMap = new LinkedHashMap<>();
        DataSource masterDataSource = createDataSource(dbProperty.getUrl());
        dataSourceMap.put("master", masterDataSource);
        dbProperty.getSlaveList().forEach(slave -> {
            dataSourceMap.put(slave.getName(), createDataSource(slave.getUrl()));
        });

        // ReplicationRoutingDataSource의 replicationRoutingDataSourceNameList 세팅 -> slave 키 이름 리스트 세팅
        replicationRoutingDataSource.setTargetDataSources(dataSourceMap);

        // 디폴트는 Master 로 설정
        replicationRoutingDataSource.setDefaultTargetDataSource(masterDataSource);
        return replicationRoutingDataSource;
    }

    @Bean
    public DataSource dataSource() {
        // 아래서 설명
        return new LazyConnectionDataSourceProxy(routingDataSource());
    }


    // JPA 에서 사용할 entityManager 설정
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();

        entityManagerFactoryBean.setJpaVendorAdapter(vendorAdapter);
        entityManagerFactoryBean.setDataSource(dataSource());
        entityManagerFactoryBean.setPackagesToScan("com.example.mysqltest");

        // 네이밍 전략 snake_case로 설정
        Map<String, String> properties = jpaProperties.getProperties();
        properties.put("hibernate.physical_naming_strategy", "org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy");

        entityManagerFactoryBean.setJpaPropertyMap(properties);

        return entityManagerFactoryBean;
    }

    // JPA 에서 사용할 TransactionManager 설정
    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        JpaTransactionManager tm = new JpaTransactionManager();
        tm.setEntityManagerFactory(entityManagerFactory);
        return tm;
    }

    // jdbcTemplate 세팅
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}