/*
 * Author : Rinka
 * Date   : 2020/1/30
 */
package com.enkanrec.twitkitFridge.steady.bean;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Class : DataSourceConfig
 * Usage : 配置数据源在Spring的托管Bean
 */
@Configuration
public class DataSourceConfig {

    @Primary
    @Bean(name = "yuiDataSource")
    @Qualifier("yuiDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.yui")
    public DataSource yuiDataSource() {
        return DataSourceBuilder.create().build();
    }
}
