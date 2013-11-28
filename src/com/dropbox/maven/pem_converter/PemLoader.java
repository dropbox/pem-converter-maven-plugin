package com.dropbox.maven.pem_converter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Loads a PEM X.509 certificate file.  Basically just parses it and base64-decodes the data.
 */
public class PemLoader
{
    public static byte[][] load(Reader in)
        throws IOException, LoadException
    {
        return load(Utils.createX509CertificateFactory(), in);
    }

    public static byte[][] load(CertificateFactory certFactory, Reader in)
        throws IOException, LoadException
    {
        HashSet<String> aliases = new HashSet<String>();
        ArrayList<byte[]> raw = new ArrayList<byte[]>();
        LineReader lin = new LineReader(in);
        try {
            while (true) {
                String line = lin.readLine();
                if (line == null) break;
                String trimmed = line.trim();
                if (trimmed.startsWith("#")) continue;  // Skip comment lines.
                if (trimmed.length() == 0) continue;    // Skip empty lines.
                if (!line.equals("-----BEGIN CERTIFICATE-----")) {
                    throw new LoadException("Expecting \"-----BEGIN CERTIFICATE-----\", blank line, or comment line starting with \"#\", got \"" + line + "\"");
                }
                int certStartLine = lin.getLastLineNumber();
                byte[] certData = loadCertData(lin, certStartLine);
                X509Certificate certificate;
                try {
                    certificate = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certData));
                }
                catch (CertificateException ex) {
                    throw new LoadException(certStartLine, "unable to load cert: " + ex.getMessage());
                }
                String alias = certificate.getSubjectX500Principal().getName();
                boolean added = aliases.add(alias);
                if (!added) {
                    throw new LoadException(certStartLine, "duplicate cert alias: \"" + alias + "\"");
                }
                if (certData.length > Utils.MaxCertLength) {
                    throw new LoadException(certStartLine, "cert data too long: " + certData.length + " bytes (max: " + Utils.MaxCertLength + " bytes)");
                }
                raw.add(certData);
            }
        }
        catch (LoadException ex) {
            // If there's no line number in the exception, this will add one in.
            ex.addLineNumber(lin.getLastLineNumber());
            throw ex;
        }
        return raw.toArray(new byte[raw.size()][]);
    }

    private static byte[] loadCertData(LineReader in, int certStartLine)
            throws IOException, LoadException
    {
        StringBuilder base64 = new StringBuilder();
        while (true) {
            String line = in.readLine();
            if (line == null) {
                throw new LoadException("No end found for cert beginning on line " + certStartLine);
            }
            if (line.startsWith("-")) {
                String endMarker = "-----END CERTIFICATE-----";
                if (!line.equals(endMarker)) {
                    throw new LoadException("Expecting \"" + endMarker + "\" or valid base-64 data, got \"" + line + "\"");
                }
                try {
                    return javax.xml.bind.DatatypeConverter.parseBase64Binary(base64.toString());
                }
                catch (IllegalArgumentException ex) {
                    throw new LoadException(certStartLine, "Invalid base-64 in cert data block: " + ex.getMessage());
                }
            }
            else {
                base64.append(line);
            }
        }
    }

    public static final class LoadException extends Exception
    {
        private final int lineNumber;

        public LoadException(String msg)
        {
            super(msg);
            this.lineNumber = -1;
        }

        public LoadException(int lineNumber, String msg)
        {
            super("line " + lineNumber + ": " + msg);
            this.lineNumber = lineNumber;
        }

        public LoadException addLineNumber(int lineNumber)
        {
            if (this.lineNumber >= 0) return this;
            return new LoadException(lineNumber, this.getMessage());
        }
    }

    public static final class LineReader
    {
        private final BufferedReader in;
        private int lastLineNumber;

        public LineReader(Reader in)
        {
            this.in = new BufferedReader(in);
            this.lastLineNumber = 0;
        }

        public String readLine()
            throws IOException
        {
            String line = in.readLine();
            if (line == null) return null;
            lastLineNumber++;
            return line;
        }

        private int getLastLineNumber()
        {
            return lastLineNumber;
        }
    }
}
