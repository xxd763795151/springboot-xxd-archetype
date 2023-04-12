package ${package}.httpclient;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RestClient {
    private final RestTemplate restTemplate;

    public RestClient(RestTemplate restTemplate) {

        this.restTemplate = restTemplate;
    }

    public <T> T postJson(String url, Object param, Class<T> res) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity httpEntity = new HttpEntity(param, headers);
        return restTemplate.postForObject(url, httpEntity, res);
    }

    public <T> ResponseEntity<T> postJsonForEntity(String url, Object param, Class<T> res) {
        return postJsonForEntity(url, param, res, null);
    }

    public <T> ResponseEntity<T> postJsonForEntity(String url, Object param, Class<T> res, HttpHeaders headers) {
        if (headers == null) {
            headers = new HttpHeaders();
        }
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        HttpEntity httpEntity = new HttpEntity(param, headers);
        ResponseEntity<T> tResponseEntity = restTemplate.postForEntity(url, httpEntity, res);
        return tResponseEntity;
    }

    public <T> ResponseEntity<T> postFormDataForEntity(String url, MultiValueMap<String, HttpEntity<?>> body, Class<T> res) {
        return postFormDataForEntity(url, body, res, null);
    }

    public <T> ResponseEntity<T> postFormDataForEntity(String url, MultiValueMap<String, HttpEntity<?>> body, Class<T> res, HttpHeaders headers) {
        if (headers == null) {
            headers = new HttpHeaders();
        }
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, HttpEntity<?>>> requestEntity
                = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(url, requestEntity, res);
    }

    public <T> ResponseEntity<T> getForEntity(String url, Map<String, Object> params, Class<T> res, HttpHeaders headers) {
        if (headers == null) {
            headers = new HttpHeaders();
        }

        HttpEntity<Object> httpEntity = CollectionUtil.isEmpty(params) ? new HttpEntity<>(headers) : new HttpEntity<>(params, headers);
        return restTemplate.exchange(url, HttpMethod.GET, httpEntity, res, params);
    }

    public void download(String url, HttpHeaders headers, String localPath) {
        if (headers == null) {
            headers = new HttpHeaders();
        }
        HttpEntity<String> entity = new HttpEntity<>("", headers);
        ResponseEntity<Resource> response = restTemplate.exchange(url, HttpMethod.GET, entity, Resource.class);
        if (!FileUtil.exist(localPath)) {
            FileUtil.touch(localPath);
        }
        try (InputStream inputStream = response.getBody().getInputStream()) {
            try (BufferedInputStream bis = new BufferedInputStream(inputStream)) {
                FileUtil.writeFromStream(bis, localPath);
                // linux默认flush应该是5秒，这里就休眠5秒
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ignore) {

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
