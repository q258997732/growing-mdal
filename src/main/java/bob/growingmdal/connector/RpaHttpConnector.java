package bob.growingmdal.connector;

import bob.growingmdal.config.AdapterProperties;
import bob.growingmdal.dto.rpa.RpaRequestBean;
import bob.growingmdal.util.PascalCaseNamingStrategy;
import bob.growingmdal.util.RpaUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.util.List;

/**
 * K-RPA HTTP 连接池客户端。
 */
@Slf4j
@Component
public class RpaHttpConnector {

    private final AdapterProperties adapterProperties;
    private final ObjectMapper objectMapper;

    private CloseableHttpClient httpClient;
    private String url;

    public RpaHttpConnector(AdapterProperties adapterProperties, ObjectMapper objectMapper) {
        this.adapterProperties = adapterProperties;
        this.objectMapper = objectMapper.copy();
        this.objectMapper.setPropertyNamingStrategy(new PascalCaseNamingStrategy());
    }

    @PostConstruct
    public void init() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(20);
        connectionManager.setDefaultMaxPerRoute(5);
        this.httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .build();
        this.url = String.format("http://%s:%s/CallFunc.aom",
                adapterProperties.getRpaHost(), adapterProperties.getRpaPort());
        log.info("RpaHttpConnector initialized, url={}", url);
    }

    /**
     * 发送 K-RPA 请求并返回响应。
     *
     * @param requestBeans 请求参数列表
     * @return 响应列表，失败返回 null
     */
    public List<RpaRequestBean> sendRequest(List<RpaRequestBean> requestBeans) {
        String json;
        try {
            json = objectMapper.writeValueAsString(requestBeans);
            log.debug("K-RPA request json: {}", json);
        } catch (Exception e) {
            log.error("Failed to serialize K-RPA request: {}", e.getMessage());
            return null;
        }

        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            String responseBody = EntityUtils.toString(response.getEntity());
            log.debug("K-RPA response json: {}", responseBody);

            List<RpaRequestBean> result = RpaUtil.result2KRpaRequestBean(responseBody, objectMapper);
            if (RpaUtil.callFunStatus(result)) {
                return result;
            } else {
                log.error("K-RPA call failed: {}", responseBody);
                return null;
            }
        } catch (Exception e) {
            log.error("Failed to send K-RPA request: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 健康检查：HTTP 可达性探测。
     *
     * @return true=可达
     */
    public boolean isReachable() {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity("[]", ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
            return response.getCode() < 500;
        } catch (Exception e) {
            log.warn("K-RPA host not reachable: {}", e.getMessage());
            return false;
        }
    }

    public String getUrl() {
        return url;
    }

    @PreDestroy
    public void destroy() {
        if (httpClient != null) {
            try {
                httpClient.close();
                log.info("RpaHttpConnector closed");
            } catch (IOException e) {
                log.warn("Failed to close RPA http client: {}", e.getMessage());
            }
        }
    }
}
