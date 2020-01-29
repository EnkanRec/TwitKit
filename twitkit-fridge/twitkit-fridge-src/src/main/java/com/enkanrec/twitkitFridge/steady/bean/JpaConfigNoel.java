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
 * Class : JpaConfigNoel
 * Usage : Noel数据库的数据源配置项
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "entityManagerFactoryNoel",
        transactionManagerRef = "transactionManagerNoel",
        basePackages = {"com.enkanrec.twitkitFridge.steady.noel.repository"})
public class JpaConfigNoel {

    @Autowired
    @Qualifier("noelDataSource")
    private DataSource noelDataSource;

    @Autowired
    private JpaProperties jpaProperties;

    @Autowired
    private HibernateProperties hibernateProperties;

    @Primary
    @Bean(name = "entityManagerNoel")
    public EntityManager entityManager(EntityManagerFactoryBuilder builder) {
        return entityManagerFactoryNoel(builder).getObject().createEntityManager();
    }

    @Primary
    @Bean(name = "entityManagerFactoryNoel")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryNoel(EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(noelDataSource)
                .packages("com.enkanrec.twitkitFridge.steady.noel.entity")
                .persistenceUnit("noelPersistenceUnit")
                .properties(getVendorProperties())
                .build();
    }

    private Map<String, Object> getVendorProperties() {
        return hibernateProperties.determineHibernateProperties(jpaProperties.getProperties(), new HibernateSettings());
    }

    @Primary
    @Bean(name = "transactionManagerNoel")
    public PlatformTransactionManager transactionManagerNoel(EntityManagerFactoryBuilder builder) {
        return new JpaTransactionManager(entityManagerFactoryNoel(builder).getObject());
    }
}
