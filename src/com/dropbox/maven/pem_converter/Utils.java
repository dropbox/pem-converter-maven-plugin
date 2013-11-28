package com.dropbox.maven.pem_converter;

import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class Utils
{
    public static final int MaxCertLength = 10 * 1024;

    static CertificateFactory createX509CertificateFactory()
    {
        CertificateFactory certFactory;
        try {
            certFactory = CertificateFactory.getInstance("X.509");
        }
        catch (CertificateException ex) {
            throw new AssertionError("Internal error: Unable to create X.509 certificate factory: " + ex.getMessage());
        }
        return certFactory;
    }
}
