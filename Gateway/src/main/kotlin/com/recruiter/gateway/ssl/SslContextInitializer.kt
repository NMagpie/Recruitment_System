package com.recruiter.gateway.ssl

import io.netty.handler.ssl.SslContext
import io.netty.handler.ssl.SslContextBuilder
import io.netty.handler.ssl.util.InsecureTrustManagerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("server.ssl")
class SslContextInitializer {

    @Value("\${cert-key}")
    lateinit var certificate : String

    @Value("\${server.ssl.trust-store}")
    lateinit var trustStore : String

    @Value("\${server.ssl.trust-store-password}")
    lateinit var trustStorePassword : String

    @Value("\${server.ssl.key-store}")
    lateinit var keyStore : String

    @Value("\${server.ssl.key-store-password}")
    lateinit var keyStorePassword : String

    private val dockerEnv = System.getenv("DOCKER_ENV")

    @Bean
    fun sslContext() : SslContext {

        //If there will be correct keyStores and trustStores

//        //truststore
//
//        val truststorePath: Path = Paths.get(formatPath(trustStore))
//        val truststoreInputStream: InputStream = Files.newInputStream(truststorePath, StandardOpenOption.READ)
//
//        val truststore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
//
//        truststore.load(truststoreInputStream, trustStorePassword.toCharArray())
//
//        //keystore
//
//        val keystorePath: Path = Paths.get(formatPath(keyStore))
//        val keystoreInputStream: InputStream = Files.newInputStream(keystorePath, StandardOpenOption.READ)
//
//        val keystore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType())
//
//        keystore.load(keystoreInputStream, keyStorePassword.toCharArray())
//
//        //certificate
//
//        val certificatePath: Path = Paths.get(formatPath(certificate))
//        val certificateInputStream: InputStream = Files.newInputStream(certificatePath, StandardOpenOption.READ)
//
//        val certificateFactory: CertificateFactory = CertificateFactory.getInstance("X.509")
//        val certificate: X509Certificate = certificateFactory.generateCertificate(certificateInputStream) as X509Certificate
//
//        truststore.setCertificateEntry("localhost", certificate)
//
//        val trustManagerFactory: TrustManagerFactory =
//            TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
//
//        trustManagerFactory.init(truststore)
//
//        val keyManagerFactory: KeyManagerFactory =
//            KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
//        keyManagerFactory.init(keystore, keyStorePassword.toCharArray())

        val sslContext: SslContext = SslContextBuilder.forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            //.keyManager(keyManagerFactory)
            //.keyStoreType("JKS")
            .build()

        return sslContext
    }

    private fun formatPath(path: String): String {
        return if (dockerEnv != null && dockerEnv == "true") path.replace(
            "classpath:",
            "/"
        ) else path.replace("classpath:", "./src/main/resources/")
    }
}
