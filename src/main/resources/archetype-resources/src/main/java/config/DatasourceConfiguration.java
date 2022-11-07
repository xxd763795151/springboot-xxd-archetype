package ${package}.config;

import ${package}.datasource.CustomDatasource;
import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatasourceConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource getDataSource(DataSourceProperties properties) {
        DruidDataSource druidDataSource = new CustomDatasource(properties);
        return druidDataSource;
    }
}
