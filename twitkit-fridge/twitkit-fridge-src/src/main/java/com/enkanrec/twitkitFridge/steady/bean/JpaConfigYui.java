/*
 * Author : Rinka
 * Date   : 2020/1/30
 */
package com.enkanrec.twitkitFridge.steady.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.util.Map;

/**
 * Class : JpaConfigYui
 * Usage : Yui数据库的数据源配置项
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManagerFactoryYui",
        transactionManagerRef = "transactionManagerYui",
        basePackages = {"com.enkanrec.twitkitFridge.steady.yui.repository"})
public class JpaConfigYui {

    @Autowired
    @Qualifier("yuiDataSource")
    private DataSource yuiDataSource;

    @Autowired
    private JpaProperties jpaProperties;

    @Autowired
    private HibernateProperties hibernateProperties;

    @Primary
    @Bean(name = "entityManagerYui")
    public EntityManager entityManager(EntityManagerFactoryBuilder builder) {
        return entityManagerFactoryYui(builder).getObject().createEntityManager();
    }

    @Primary
    @Bean(name = "entityManagerFactoryYui")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryYui(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(yuiDataSource)
                .packages("com.enkanrec.twitkitFridge.steady.yui.entity")
                .persistenceUnit("yuiPersistenceUnit")
                .properties(getVendorProperties())
                .build();
    }

    private Map<String, Object> getVendorProperties() {
        return hibernateProperties.determineHibernateProperties(jpaProperties.getProperties(), new HibernateSettings());
    }

    @Primary
    @Bean(name = "transactionManagerYui")
    public PlatformTransactionManager transactionManagerYui(EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(entityManagerFactoryYui(builder).getObject());
    }
}
