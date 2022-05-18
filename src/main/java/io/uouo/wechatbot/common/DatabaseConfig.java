package io.uouo.wechatbot.common;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;
import java.util.Properties;

@Configuration
public class DatabaseConfig {

    @Value("${database.url}")
    private String url;

    @Value("${database.username}")
    private String username;

    @Value("${database.password}")
    private String password;

    @Bean
    public DruidDataSource dataSource() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setUrl(url);
        druidDataSource.setUsername(username);
        druidDataSource.setPassword(password);
        druidDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        druidDataSource.setInitialSize(5);
        druidDataSource.setMinIdle(2);
        druidDataSource.setMaxActive(32);
        druidDataSource.setMaxWait(128);
        druidDataSource.setTimeBetweenEvictionRunsMillis(60000);
        druidDataSource.setMinEvictableIdleTimeMillis(25200000);
        druidDataSource.setValidationQuery("select now()");
        druidDataSource.setTestWhileIdle(true);
        druidDataSource.setTestOnBorrow(true);
        druidDataSource.setTestOnReturn(false);
        druidDataSource.setRemoveAbandoned(true);
        druidDataSource.setRemoveAbandonedTimeout(1800);
        druidDataSource.setLogAbandoned(true);

        Properties properties = new Properties();
        properties.setProperty("defaultRowPrefetch", "5000");
        druidDataSource.setConnectProperties(properties);
        try {
            druidDataSource.setFilters("stat");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return druidDataSource;
    }
}
