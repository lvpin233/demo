package com.example.common;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

/**
 * Created by Lvpin on 2018/12/21.
 */
public class HttpUtils {
    private final static Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * 发送微信转账的ssl请求
     *
     * @param url      请求URL
     * @param data     请求内容
     * @param charset  编码格式
     * @param certPath 证书地址
     * @param mchId    证书密码
     * @return
     * @author scott
     */
    public static String httpPostSsl(String url, String data, String charset, String certPath, String mchId) {
        String responseData = null;
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            //P12文件目录
            InputStream inputStream = new FileInputStream(new File(certPath));
            try {
                //密码,默认是MCHID
                keyStore.load(inputStream, mchId.toCharArray());
            } catch (Exception e) {
                logger.error("Error:", e);
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            SSLContext sslcontext;
            sslcontext = SSLContexts.custom().loadKeyMaterial(keyStore, mchId.toCharArray()).build();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,
                    new String[]{"TLSv1"}, null, SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
            CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

            try {
                // 设置响应头信息
                HttpPost httpPost = new HttpPost(url);

                httpPost.addHeader("Content-Type", "application/xml");
                httpPost.addHeader("Host", "api.mch.weixin.qq.com");
                httpPost.addHeader("Cache-Control", "max-age=0");
                httpPost.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0) ");
                httpPost.addHeader("Connection", "keep-alive");
                httpPost.addHeader("Accept", "*/*");
                httpPost.setEntity(new StringEntity(data, "UTF-8"));
                logger.info("request url: {}, charset:{}, data:{}", httpPost.getURI(), charset, data);

                CloseableHttpResponse response = httpclient.execute(httpPost);
                try {
                    HttpEntity entity = response.getEntity();
                    responseData = EntityUtils.toString(entity, charset);
                    EntityUtils.consume(entity);
                    logger.info("Response content:{} ", responseData);
                } finally {
                    if (response != null) {
                        response.close();
                    }
                }
            } finally {
                // 关闭连接,释放资源
                try {
                    if (httpclient != null) {
                        httpclient.close();
                    }
                } catch (IOException e) {
                    logger.error("Error:{}", e);
                }
            }
        } catch (Exception e) {
            logger.error("Error:{}", e);
        }
        return responseData;
    }
}
