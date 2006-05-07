package org.codehaus.plexus.maven.plugin.service;

/*
 * Copyright (c) 2004, Codehaus.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Properties;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import org.codehaus.plexus.builder.service.ServiceBuilder;
import org.codehaus.plexus.builder.service.ServiceBuilderException;

/**
 * @goal assemble-service
 *
 * @requiresDependencyResolution
 * @requiresProject
 *
 * @description Assembled and bundles a Plexus service.
 *
 * @phase package
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class AssembleService
    extends AbstractMojo
{
    // ----------------------------------------------------------------------
    // Configuration
    // ----------------------------------------------------------------------

    /**
     * @parameter expression="${serviceName}"
     * @required
     */
    private String serviceName;

    /**
     * @parameter expression="${serviceConfiguration}"
     * @required
     */
    private File serviceConfiguration;

    /**
     * @parameter expression="${configurationsDirectory}"
     */
    private File configurationsDirectory;

    /**
     * @parameter expression="${configurationProperties}"
     */
    private File configurationProperties;

    // ----------------------------------------------------------------------
    // Read only configurator
    // ----------------------------------------------------------------------

    /**
     * @parameter expression="${project.build.finalName}"
     * @required
     */
    private String finalName;

    /**
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File target;

    /**
     * @parameter expression="${project.artifacts}"
     * @required
     */
    private Set serviceArtifacts;

    /**
     * @parameter expression="${project.build.directory}/plexus-service"
     * @required
     */
    private File serviceAssemblyDirectory;

    // ----------------------------------------------------------------------
    // Components
    // ----------------------------------------------------------------------

    /**
     * @parameter expression="${localRepository}"
     * @required
     */
    private ArtifactRepository localRepository;

    /**
     * @parameter expression="${project.remoteArtifactRepositories}"
     * @required
     */
    private List remoteRepositories;

    /**
     * @parameter expression="${component.org.codehaus.plexus.builder.service.ServiceBuilder}"
     * @required
     */
    private ServiceBuilder builder;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @component
     */
    private MavenProjectHelper projectHelper;

    public void execute()
        throws MojoExecutionException
    {
        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        File outputFile = new File( target, finalName + ".sar" );

        File serviceJar = new File( target, finalName + ".jar" );

        // ----------------------------------------------------------------------
        // Build the service
        // ----------------------------------------------------------------------

        Properties interpolationProperties = new Properties();

        if ( configurationProperties != null )
        {
            try
            {
                interpolationProperties.load( new FileInputStream( configurationProperties ) );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Cannot load configuration properties file.", e );
            }
        }

        try
        {
            builder.build( serviceName,
                           serviceAssemblyDirectory,
                           serviceJar,
                           remoteRepositories,
                           localRepository,
                           serviceArtifacts,
                           serviceConfiguration,
                           configurationsDirectory,
                           interpolationProperties );

            // ----------------------------------------------------------------------
            // Bundle the service
            // ----------------------------------------------------------------------

            builder.bundle( outputFile, serviceAssemblyDirectory );
        }
        catch ( ServiceBuilderException e )
        {
            throw new MojoExecutionException( "Error while making service.", e );
        }

        project.getArtifact().setFile( outputFile );
    }
}
