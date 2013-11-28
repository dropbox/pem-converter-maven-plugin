package com.dropbox.maven.pem_converter;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class RawLoader
{
    public static final class LoadException extends Exception
    {
        public LoadException(String message) { super(message); }
    }

    public static KeyStore load(InputStream in)
        throws IOException, LoadException, KeyStoreException
    {
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
        }
        catch (Exception ex) {
            throw new AssertionError("Unable to create new KeyStore: " + ex.getMessage());
        }
        load(keyStore, in);
        return keyStore;
    }

    public static void load(KeyStore keyStore, InputStream in)
        throws IOException, LoadException
    {
        CertificateFactory x509CertFactory = Utils.createX509CertificateFactory();
        DataInputStream din = new DataInputStream(in);
        byte[] data = new byte[Utils.MaxCertLength];
        while (true) {
            int length = din.readUnsignedShort();
            if (length == 0) break;
            if (length > Utils.MaxCertLength) {
                throw new LoadException("Invalid length for certificate entry: " + length);
            }
            din.readFully(data, 0, length);
            X509Certificate cert;
            try {
                cert = (X509Certificate) x509CertFactory.generateCertificate(new ByteArrayInputStream(data, 0, length));
            }
            catch (CertificateException ex) {
                throw new LoadException("Error loading certificate: " + ex.getMessage());
            }
            String alias = cert.getSubjectX500Principal().getName();
            try {
                keyStore.setCertificateEntry(alias, cert);
            }
            catch (KeyStoreException ex) {
                throw new LoadException("Error loading certificate: " + ex.getMessage());
            }
        }
        if (din.read() >= 0) {
            throw new LoadException("Found data after after zero-length header.");
        }
    }

    public static void store(byte[][] certDatas, OutputStream out)
        throws IOException
    {
        DataOutputStream dout = new DataOutputStream(out);
        for (byte[] certData : certDatas) {
            if (certData.length <= 0) throw new AssertionError("zero-length certData entry");
            if (certData.length > Utils.MaxCertLength) throw new AssertionError("certData entry too long");
            dout.writeShort(certData.length);
            dout.write(certData);
        }
        dout.writeShort(0);
        dout.flush();
    }
}
