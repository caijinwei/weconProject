package com.wecon.box.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by caijw on 2017/10/30.
 */
public class SSLUtil {

    /**
     * 获取 tls 安全套接字工厂
     * @param caCrtFile null:使用系统默认的 ca 证书来验证。 非 null:指定使用的 ca 证书来验证服务器的证书。
     * @return tls 套接字工厂
     * @throws Exception
     */
    public static SSLSocketFactory getSocketFactory (final InputStream in) throws NoSuchAlgorithmException, IOException, KeyStoreException, CertificateException, KeyManagementException {
        Security.addProvider(new BouncyCastleProvider());


        //===========加载 ca 证书==================================
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        if( null != in ){
            // 加载本地指定的 ca 证书
            PEMReader reader = new PEMReader(new InputStreamReader(in));
            X509Certificate caCert = (X509Certificate)reader.readObject();
            reader.close();

            // CA certificate is used to authenticate server
            KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
            caKs.load(null, null);
            caKs.setCertificateEntry("ca-certificate", caCert);
            // 把ca作为信任的 ca 列表,来验证服务器证书
            tmf.init(caKs);
        }else {
            //使用系统默认的安全证书
            tmf.init((KeyStore)null);
        }

        // ============finally, create SSL socket factory==============
        SSLContext context = SSLContext.getInstance("TLSv1");
        context.init(null,tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    }


}
