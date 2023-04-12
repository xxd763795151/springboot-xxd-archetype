package ${package}.httpclient;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "httpclient")
public class HttpClientProperties {

    private int readTimeout;

    private int connectTimeout;

    private int acquireConnectionTimeout = 3000;

    private int maxConnection;

    private int maxConnectionRoute;

    private int retryTimes;

    private int idleTime;
}
