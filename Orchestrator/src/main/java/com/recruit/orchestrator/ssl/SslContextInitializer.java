package com.recruit.orchestrator.ssl;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

@Component
@ConfigurationProperties("server.ssl")
public class SslContextInitializer {

    private String trustStore;

    @Getter
    @Setter
    private char[] trustStorePassword;

    private String keyStore;

    @Getter
    @Setter
    private char[] keyStorePassword;

    private final String dockerEnv = System.getenv("DOCKER_ENV");

    private static final String PROTOCOL = "SSL";

    public SSLContext initSslContext() throws Exception {
        // Load the keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");

        InputStream keyStoreIS = (dockerEnv != null && dockerEnv.equals("true")) ?
                getClass().getResourceAsStream(this.keyStore)
                : new FileInputStream(this.keyStore);

        InputStream trustStoreIS = (dockerEnv != null && dockerEnv.equals("true")) ?
                getClass().getResourceAsStream(this.trustStore)
                : new FileInputStream(this.trustStore);

        keyStore.load(keyStoreIS, this.keyStorePassword);

        // Initialize a KeyManagerFactory with the loaded keystore
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, this.keyStorePassword);

        // Load the truststore
        KeyStore trustStore = KeyStore.getInstance("JKS");
        //new FileInputStream(this.trustStore)
        trustStore.load(trustStoreIS, this.trustStorePassword);

        // Initialize a TrustManagerFactory with the loaded truststore
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        // Initialize an SSLContext with the KeyManager and TrustManager from the factories
        SSLContext sslContext = SSLContext.getInstance(PROTOCOL);

        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        return sslContext;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
            this.trustStore = formatPath(trustStore);
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
            this.keyStore = formatPath(keyStore);
    }

    private String formatPath(String path) {
        if (dockerEnv != null && dockerEnv.equals("true"))
            return path.replace("classpath:", "/");
        else
            return path.replace("classpath:", "./src/main/resources/");
    }
}
