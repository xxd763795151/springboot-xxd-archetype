package ${package}.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import ${package}.util.AESCoderUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;

@Configuration
public class CustomDatasource extends DruidDataSource{

    private final DataSourceProperties properties;

    private final boolean unEncrypt;

    public CustomDatasource(DataSourceProperties properties) {
        this.properties = properties;
        String url = this.properties.getUrl();
        unEncrypt = url.contains("mysql") || url.contains("jdbc") || url.contains("localhost") || url.contains("127.0.0.1");
    }

    @Override
    public void setPassword(String password) {
        super.setPassword(unEncrypt ? password : AESCoderUtil.decode(password));
    }

    @Override
    public void setUsername(String username) {
        super.setUsername(unEncrypt ? username : AESCoderUtil.decode(username));
    }

    @Override
    public synchronized void setUrl(String url) {
        super.setUrl(unEncrypt ? url : AESCoderUtil.decode(url));
    }
}

