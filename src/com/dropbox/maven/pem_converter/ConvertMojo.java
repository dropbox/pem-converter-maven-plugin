package com.dropbox.maven.pem_converter;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

@Mojo(name = "convert", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class ConvertMojo extends AbstractMojo
{
    @Parameter(required = true)
    private File input;

    @Parameter(required = true)
    private File output;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        getLog().debug("Converting PEM file \"" + input.getPath() + "\" to \"" + output.getPath() + "\".");

        byte[][] certDatas;
        try {
            FileInputStream in = new FileInputStream(this.input);
            try {
                certDatas = PemLoader.load(new InputStreamReader(in, "UTF-8"));
            }
            finally {
                in.close();
            }
        }
        catch (PemLoader.LoadException ex) {
            throw new MojoExecutionException("Format error in PEM file: " + ex.getMessage(), ex);
        }
        catch (IOException ex) {
            throw new MojoExecutionException("Error reading from PEM file", ex);
        }

        // Write KeyStore to file.
        File parent = this.output.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream(this.output));
            try {
                RawLoader.store(certDatas, out);
            } finally {
                out.close();
            }
        }
        catch (IOException ex) {
            throw new MojoExecutionException("Error writing to output file: " + ex.getMessage());
        }
    }
}
