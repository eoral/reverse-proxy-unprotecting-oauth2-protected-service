package com.eoral.reverseProxyUnprotectingOauth2ProtectedService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.Base64;

import static com.eoral.reverseProxyUnprotectingOauth2ProtectedService.Config.*;

public class TokenProvider {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final LoadingCache<String, String> TOKEN_CACHE = getLoadingCache();

    private static LoadingCache<String, String> getLoadingCache() {
        CacheLoader<String, String> cacheLoader = new CacheLoader<String, String>() {
            @Override
            public String load(String key) {
                try {
                    return getTokenFromScratch();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        RemovalListener<String, String> removalListener = notification -> System.out.println("Cached token removed.");
        return CacheBuilder.newBuilder()
                .maximumSize(1)
                .expireAfterWrite(TOKEN_TTL_IN_CACHE)
                .removalListener(removalListener)
                .build(cacheLoader);
    }
    
    public static String getToken() {
        String key = "my-default-key"; // Since we store only one item in cache, we are using the same key everytime.
        return TOKEN_CACHE.getUnchecked(key);
    }
    
    private static String getTokenFromScratch() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        System.out.println("Getting token from scratch...");
        try (CloseableHttpClient client = generateHttpClient()) {
            String uri = MessageFormat.format("{0}?scope={1}&grant_type={2}",
                    CLIENT_CREDENTIALS_FLOW_TOKEN_URL,
                    CLIENT_CREDENTIALS_FLOW_SCOPE,
                    CLIENT_CREDENTIALS_FLOW_GRANT_TYPE);
                    HttpPost httpPost = new HttpPost(uri);
            httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
            httpPost.addHeader("Authorization", generateBasicAuthorizationHeader(CLIENT_CREDENTIALS_FLOW_USERNAME, CLIENT_CREDENTIALS_FLOW_PASSWORD));
            CloseableHttpResponse httpResponse = client.execute(httpPost);
            TokenResponse tokenResponse = OBJECT_MAPPER.readValue(extractResponse(httpResponse), TokenResponse.class);
            String token = tokenResponse.getAccess_token();
            System.out.println("token:\n" + token);
            return token;
        }
    }
    
    private static CloseableHttpClient generateHttpClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return HttpClients
                .custom()
                .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                .build();
    }
    
    private static String extractResponse(CloseableHttpResponse httpResponse) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()))) {
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            return response.toString();
        }
    }
    
    private static String generateBasicAuthorizationHeader(String username, String password) {
        return "Basic " + toBase64EncodedString(username + ":" + password);
    }

    private static String toBase64EncodedString(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes());
    }
}