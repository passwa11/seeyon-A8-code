package com.seeyon.ctp.rest.util;


import com.seeyon.ctp.common.log.CtpLogFactory;
import org.apache.commons.httpclient.params.HttpConnectionParams;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.logging.Log;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class MySSLSocketFactory implements ProtocolSocketFactory {
    private static final Log logger = CtpLogFactory.getLog(MySSLSocketFactory.class);
    private SSLContext sslcontext = null;

    private SSLContext createSSLContext() {
        SSLContext sslcontext = null;
        try {
            sslcontext = SSLContext.getInstance("SSL");
            sslcontext.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        } catch (KeyManagementException e) {
            logger.error(e.getMessage(), e);
        }
        return sslcontext;
    }

    private SSLContext getSSLContext() {
        if (this.sslcontext == null) {
            this.sslcontext = createSSLContext();
        }
        return this.sslcontext;
    }

    public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
        SSLSocketFactory socketFactory = getSocketFactory();
        if (socketFactory == null) {
            throw new UnsupportedOperationException("");
        }
        return socketFactory.createSocket(socket, host, port, autoClose);
    }

    public Socket createSocket(String host, int port) throws IOException {
        SSLSocketFactory socketFactory = getSocketFactory();
        if (socketFactory == null) {
            throw new UnsupportedOperationException("");
        }
        return socketFactory.createSocket(host, port);
    }

    public Socket createSocket(String host, int port, InetAddress clientHost, int clientPort) throws IOException {
        SSLSocketFactory socketFactory = getSocketFactory();
        if (socketFactory == null) {
            return null;
        }
        return socketFactory.createSocket(host, port, clientHost, clientPort);
    }

    private SSLSocketFactory getSocketFactory() {
        SSLContext context = getSSLContext();
        if (context == null) {
            return null;
        }
        return context.getSocketFactory();
    }

    public Socket createSocket(String host, int port, InetAddress localAddress, int localPort, HttpConnectionParams params) throws IOException {
        if (params == null) {
            throw new IllegalArgumentException("Parameters may not be null");
        }
        int timeout = params.getConnectionTimeout();
        SocketFactory socketfactory = getSocketFactory();
        if (socketfactory == null) {
            throw new IllegalArgumentException("socketfactory is null");
        }
        if (timeout == 0) {
            return socketfactory.createSocket(host, port, localAddress, localPort);
        } else {
            Socket socket = socketfactory.createSocket();
            try {
                SocketAddress localaddr = new InetSocketAddress(localAddress, localPort);
                SocketAddress remoteaddr = new InetSocketAddress(host, port);
                socket.bind(localaddr);
                socket.connect(remoteaddr, timeout);
                return socket;
            } catch (IOException e) {
                throw new IOException(e);
            } finally {
                socket.close();
            }
        }
    }

    private static class TrustAnyTrustManager implements X509TrustManager {

        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }
    }
}
