/*
 * Copyright 2010-2013 Grzegorz Slowikowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.code.play;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

/**
 * Packages Play! framework and Play! application as one ZIP achive (standalone distribution).
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal dist
 * @phase package
 * @requiresDependencyResolution test
 */
public class PlayDistMojo
    extends AbstractPlayDistMojo
{

    /**
     * Skip distribution file generation.
     * 
     * @parameter expression="${play.distSkip}" default-value="false"
     * @required
     * @since 1.0.0
     */
    private boolean distSkip;

    /**
     * The directory for the generated distribution file.
     * 
     * @parameter expression="${play.distOutputDirectory}" default-value="${project.build.directory}"
     * @required
     * @since 1.0.0
     */
    private String distOutputDirectory;

    /**
     * The name of the generated distribution file.
     * 
     * @parameter expression="${play.distArchiveName}" default-value="${project.build.finalName}"
     * @required
     * @since 1.0.0
     */
    private String distArchiveName;

    /**
     * Classifier to add to the generated distribution file.
     * 
     * @parameter expression="${play.distClassifier}" default-value="dist"
     * @since 1.0.0
     */
    private String distClassifier;

    /**
     * Attach generated distribution file to project artifacts.
     * 
     * @parameter expression="${play.distAttach}" default-value="false"
     * @since 1.0.0
     */
    private boolean distAttach;

    /**
     * Maven ProjectHelper.
     * 
     * @component
     */
    private MavenProjectHelper projectHelper;

    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        if ( distSkip )
        {
            getLog().info( "UberZip generation skipped" );
            return;
        }

        try
        {
            File destFile = new File( distOutputDirectory, getDestinationFileName() );

            ConfigurationParser configParser = getConfiguration();

            ZipArchiver zipArchiver = prepareArchiver( configParser );
            zipArchiver.setDestFile( destFile );

            zipArchiver.createArchive();
            
            if ( distAttach )
            {
                projectHelper.attachArtifact( project, "zip", distClassifier, destFile );
            }
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "?", e );
        }
        catch ( DependencyTreeBuilderException e )
        {
            throw new MojoExecutionException( "?", e );
        }
        catch ( NoSuchArchiverException e )
        {
            throw new MojoExecutionException( "?", e );
        }
    }

    private String getDestinationFileName()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( distArchiveName );
        if ( distClassifier != null && !"".equals( distClassifier ) )
        {
            if ( !distClassifier.startsWith( "-" ) )
            {
                buf.append( '-' );
            }
            buf.append( distClassifier );
        }
        buf.append( ".zip" );
        return buf.toString();
    }

}
