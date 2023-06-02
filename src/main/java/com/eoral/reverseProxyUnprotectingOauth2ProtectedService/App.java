package com.eoral.reverseProxyUnprotectingOauth2ProtectedService;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.proxy.ProxyServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

import static com.eoral.reverseProxyUnprotectingOauth2ProtectedService.Config.*;

/**
 * Links:
 * - https://medium.com/sandwich-bytes/reverse-proxy-using-jetty-undertow-96a29bb53ca8
 * - https://github.com/jobinbasani/java-reverse-proxy/blob/master/src/main/java/com/jb/proxy/services/impl/JettyProxy.java
 * - https://alanhohn.com/posts/2013/jetty-proxy-servlet
 * - https://stackoverflow.com/questions/9235809/how-to-enable-debug-level-logging-with-jetty-embedded
 * - https://github.com/tomkraljevic/jetty-embed-reverse-proxy-example/blob/master/src/main/java/org/eclipse/jetty/embedded/ProxyServer.java
 * - https://dzone.com/articles/configuring-jetty-servlet-proxy
 */
public class App {

    public static void main(final String[] args) throws Exception {
        runProxyUsingJetty();
    }

    private static void runProxyUsingJetty() throws Exception {
        Server server = new Server(PROXY_PORT);
        ServletContextHandler contextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        contextHandler.setContextPath("/");
        server.setHandler(contextHandler);
        contextHandler.addServlet(new ServletHolder(new ProxyServlet() {
            @Override
            protected HttpClient newHttpClient() {
                SslContextFactory sslContextFactory = new SslContextFactory(true); // skip SSL validation
                return new HttpClient(sslContextFactory);
            }
            @Override
            protected void customizeProxyRequest(Request proxyRequest, HttpServletRequest request) {
                proxyRequest.getHeaders().remove("Host"); // If we don't do this, requests to AWS API Gateway fail.
                String token = TokenProvider.getToken();
                boolean isBearerPrefixToBeAdded = BEARER_PREFIX_ADDITION_PREDICATE.test(request);
                String authorizationHeaderValue = isBearerPrefixToBeAdded ? "Bearer " + token : token;
                proxyRequest.getHeaders().add("Authorization", authorizationHeaderValue);
            }
            @Override
            protected URI rewriteURI(HttpServletRequest request) {
                System.out.println("new request...");
                String requestURI = request.getRequestURI();
                String queryString = request.getQueryString();
                String newURI = REMOTE_URL + requestURI;
                if (queryString != null) {
                    newURI = newURI + "?" + queryString;
                }
                System.out.println("underlying request will be sent to: " + newURI);
                return URI.create(newURI);
            }
        }), "/*");
        server.start();
        server.join();
    }
}
