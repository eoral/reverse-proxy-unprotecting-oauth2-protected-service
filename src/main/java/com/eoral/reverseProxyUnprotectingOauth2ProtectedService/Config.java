package com.eoral.reverseProxyUnprotectingOauth2ProtectedService;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.function.Predicate;

public class Config {

    public static final String CLIENT_CREDENTIALS_FLOW_TOKEN_URL = "https://my-authentication-provider.com/oauth2/token";
    public static final String CLIENT_CREDENTIALS_FLOW_SCOPE = "my-scope";
    public static final String CLIENT_CREDENTIALS_FLOW_GRANT_TYPE = "client_credentials";
    public static final String CLIENT_CREDENTIALS_FLOW_USERNAME = "my-client-id";
    public static final String CLIENT_CREDENTIALS_FLOW_PASSWORD = "my-client-secret";
    public static final Duration TOKEN_TTL_IN_CACHE = Duration.ofSeconds(3540); // This should be a little less than actual token lifetime.
    public static final int PROXY_PORT = 8080;
    public static final String REMOTE_URL = "https://my-api.com";
    public static final Predicate<HttpServletRequest> BEARER_PREFIX_ADDITION_PREDICATE = httpServletRequest -> {
        /**
         * Return true to add 'Bearer' prefix.
         * If you don't want the 'Bearer' prefix for a specific context path, here is an example:
         * <pre>
         *     if (httpServletRequest.getRequestURI().startsWith("/my-context-path/")) {
         *         return false;
         *     }
         * </pre>
         */
        return true;
    };
}