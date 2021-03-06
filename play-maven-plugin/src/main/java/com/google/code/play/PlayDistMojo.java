/*
 * Copyright 2010-2016 Grzegorz Slowikowski
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
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

/**
 * Package Play&#33; framework and Play&#33; application as one zip achive (standalone distribution).
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @since 1.0.0
 */
@Mojo( name = "dist", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.TEST )
public class PlayDistMojo
    extends AbstractPlayDistMojo
{

    /**
     * Skip distribution file generation.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.distSkip", defaultValue = "false" )
    private boolean distSkip;

    /**
     * The directory for the generated distribution file.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.distOutputDirectory", defaultValue = "${project.build.directory}", required = true )
    private String distOutputDirectory;

    /**
     * The name of the generated distribution file.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.distArchiveName", defaultValue = "${project.build.finalName}", required = true )
    private String distArchiveName;

    /**
     * Classifier to add to the generated distribution file.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.distClassifier", defaultValue = "dist" )
    private String distClassifier;

    /**
     * Attach generated distribution file to project artifacts.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.distAttach", defaultValue = "false" )
    private boolean distAttach;

    /**
     * Maven ProjectHelper.
     * 
     */
    @Component
    private MavenProjectHelper projectHelper;

    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        if ( distSkip )
        {
            getLog().info( "Dist generation skipped" );
            return;
        }

        String checkMessage = playModuleNotApplicationCheck();
        if ( checkMessage != null )
        {
            getLog().info( checkMessage );
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
            throw new MojoExecutionException( "Failed to assemble distribution archive: " + e.getMessage(), e );
        }
        catch ( DependencyTreeBuilderException e )
        {
            throw new MojoExecutionException( "Failed to assemble distribution archive: " + e.getMessage(), e );
        }
        catch ( NoSuchArchiverException e )
        {
            throw new MojoExecutionException( "Failed to assemble distribution archive: " + e.getMessage(), e );
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
