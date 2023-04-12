package ${package}.httpclient;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class RestTemplateConfiguration {

    private final HttpClientProperties httpClientProperties;

    public RestTemplateConfiguration(HttpClientProperties httpClientProperties) {
        this.httpClientProperties = httpClientProperties;
    }


    @Resource(name = "clientHttpRequestFactory")
    @Bean("restTemplate")
    @Order(2)
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getMessageConverters().set(1,
                new StringHttpMessageConverter(Charset.forName("UTF-8")));
        log.info("Create restTemplate");
        return restTemplate;
    }

    @Resource(name = "clientHttpRequestFactory")
    @Bean("restTemplateDisableErrorResponse")
    @Order(2)
    public RestTemplate restTemplateDisableErrorResponse(ClientHttpRequestFactory factory) {
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getMessageConverters().set(1,
                new StringHttpMessageConverter(Charset.forName("UTF-8")));
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {

            }
        });
        log.info("Create restTemplateDisableErrorResponse");
        return restTemplate;
    }

    /**
     * 创建HTTP客户端工厂
     */
    @Bean(name = "clientHttpRequestFactory")
    public ClientHttpRequestFactory clientHttpRequestFactory(HttpClient client) {

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new
                HttpComponentsClientHttpRequestFactory(client);
        clientHttpRequestFactory.setConnectTimeout(httpClientProperties.getConnectTimeout());
        clientHttpRequestFactory.setReadTimeout(httpClientProperties.getReadTimeout());
        clientHttpRequestFactory.setConnectionRequestTimeout(httpClientProperties.getAcquireConnectionTimeout());
        return clientHttpRequestFactory;
    }

    @Bean
    public HttpClient httpClient() {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        try {
            // 针对https协议相关配置
            SSLContext sslContext = SSLContext.getInstance("SSL");// 获取一个SSLContext实例
            TrustManager[] trustAllCerts = {new InsecureTrustManager()};
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());// 初始化SSLContext实例

            //设置信任ssl访问
//            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (arg0, arg1) -> true).build();
            httpClientBuilder.setSSLContext(sslContext);
            HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
            SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    // 注册http和https请求
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", sslConnectionSocketFactory).build();
            PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            poolingHttpClientConnectionManager.setMaxTotal(httpClientProperties.getMaxConnection());
            poolingHttpClientConnectionManager.setDefaultMaxPerRoute(httpClientProperties.getMaxConnectionRoute());
            httpClientBuilder.setConnectionManager(poolingHttpClientConnectionManager);
            httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(httpClientProperties.getRetryTimes(), true));

            //设置默认请求头
            List<Header> headers = getDefaultHeaders();
            httpClientBuilder.setDefaultHeaders(headers);

            httpClientBuilder.evictExpiredConnections();
            httpClientBuilder.evictIdleConnections(httpClientProperties.getIdleTime(), TimeUnit.MINUTES);
            CloseableHttpClient httpClient = httpClientBuilder.build();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    log.error("close http client error.", e);
                }
            }));
            return httpClient;
        } catch (Exception e) {
            log.error("HttpClient create error.", e);
        }
        return null;
    }

    private List<Header> getDefaultHeaders() {
        List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Connection", "Keep-Alive"));
        return headers;
    }

    class InsecureTrustManager implements X509TrustManager {

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            // TODO Auto-generated method stub

        }

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        /**
         * 返回受信任的X509证书数组
         */
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }


}
