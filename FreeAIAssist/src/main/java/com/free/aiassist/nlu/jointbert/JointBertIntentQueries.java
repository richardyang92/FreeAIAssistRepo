package com.free.aiassist.nlu.jointbert;

import com.free.aiassist.nlu.api.NluHandlerBase;
import com.free.aiassist.nlu.api.NluRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
public class JointBertIntentQueries extends NluHandlerBase {
    private final CloseableHttpClient httpClient;
    private final String apiUrl;

    public JointBertIntentQueries(String apiUrl) {
        super();
        this.apiUrl = apiUrl;
        this.httpClient = HttpClients.createDefault();
    }

    @Override
    public void handleNluRequest(NluRequest request) {
        HttpPost httpPost = new HttpPost(this.apiUrl);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setEntity(new StringEntity(request.getQuery(), Charset.defaultCharset()));

        try (CloseableHttpResponse response = this.httpClient.execute(httpPost)) {
            if (response.getCode() == 200 && response.getEntity() != null) {
                String content = EntityUtils.toString(response.getEntity(), "utf-8");
                request.setContent(content);
            }
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } finally {
            if (!"".equals(request.getContent())
                    && this.nextHandler != null) {
                this.nextHandler.handleNluRequest(request);
            }
        }
    }
}
