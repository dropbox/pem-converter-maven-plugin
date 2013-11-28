package com.dropbox.maven.pem_converter;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

public class CommandLineTool
{
    public static void main(String[] args)
    {
        try {
            main_(args);
        }
        catch (Exit exit) {
            System.exit(exit.code);
        }
    }

    public static void main_(String[] args)
        throws Exit
    {
        if (args.length != 3) {
            throw error(
                "Usage:",
                "    COMMAND pem-to-raw <input-file> <output-file>",
                "    COMMAND raw-to-keystore <input-file> <output-file>");
        }

        String conversion = args[0];
        String argInFile = args[1];
        String argOutFile = args[2];

        if (conversion.equals("pem-to-raw")) {
            pemToRaw(argInFile, argOutFile);
        }
        else if (conversion.equals("raw-to-keystore")) {
            pemToKeyStore(argInFile, argOutFile);
        }
        else {
            throw error("Invalid sub-command: \"" + conversion + "\".");
        }
    }

    private static void pemToKeyStore(String argInFile, String argOutFile)
        throws Exit
    {
        KeyStore keyStore;
        try {
            InputStream in = new FileInputStream(argInFile);
            try {
                keyStore = RawLoader.load(in);
            }
            finally {
                in.close();
            }
        }
        catch (IOException ex) {
            throw error("Error reading from <input-file>: \"" + argInFile + "\": " + ex.getMessage());
        }
        catch (RawLoader.LoadException ex) {
            throw error("Error in <input-file>: \"" + argInFile + ": " + ex.getMessage());
        }
        catch (KeyStoreException ex) {
            throw error("Error manipulating KeyStore: " + ex.getMessage());
        }

        try {
            OutputStream out = new FileOutputStream(argOutFile);
            try {
                keyStore.store(out, new char[0]);
            }
            finally {
                out.close();
            }
        }
        catch (CertificateException ex) {
            throw error("Error storing KeyStore to file: " + ex.getMessage());
        }
        catch (NoSuchAlgorithmException ex) {
            throw error("Error storing KeyStore to file: " + ex.getMessage());
        }
        catch (KeyStoreException ex) {
            throw error("Error storing KeyStore to file: " + ex.getMessage());
        }
        catch (IOException ex) {
            throw error("Error writing to <output-file>: \"" + argOutFile + "\": " + ex.getMessage());
        }
    }

    private static void pemToRaw(String argInFile, String argOutFile)
        throws Exit
    {
        byte[][] certDatas;
        try {
            InputStream in = new FileInputStream(argInFile);
            try {
                certDatas = PemLoader.load(new InputStreamReader(in, "UTF-8"));
            }
            finally {
                in.close();
            }
        }
        catch (IOException ex) {
            throw error("Error reading from <input-file>: \"" + argInFile + "\": " + ex.getMessage());
        }
        catch (PemLoader.LoadException ex) {
            throw error("Error in <input-file>: \"" + argInFile + ": " + ex.getMessage());
        }

        // Write KeyStore to file.
        try {
            OutputStream out = new FileOutputStream(argOutFile);
            try {
                RawLoader.store(certDatas, out);
            } finally {
                out.close();
            }
        }
        catch (IOException ex) {
            throw error("Error writing to <output-file>: \"" + argOutFile + "\": " + ex.getMessage());
        }
    }

    static Exit error(String... messages)
    {
        for (String message : messages) {
            System.err.println(message);
        }
        return new Exit(1);
    }

    static final class Exit extends Exception
    {
        public final int code;
        public Exit(int code) { this.code = code; }
    }
}
