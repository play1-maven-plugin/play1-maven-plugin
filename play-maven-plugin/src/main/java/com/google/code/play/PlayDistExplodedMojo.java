/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.google.code.play;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

/**
 * Create an exploded Play! framework and Play! application (standalone distribution).
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal dist-exploded
 * @phase package
 * @requiresDependencyResolution test
 */
public class PlayDistExplodedMojo
    extends AbstractPlayDistMojo
{

    /**
     * Skip dist exploded generation.
     * 
     * @parameter expression="${play.distExplodedSkip}" default-value="false"
     * @required
     * @since 1.0.0
     */
    private boolean distExplodedSkip;

    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        if ( distExplodedSkip )
        {
            getLog().info( "Exploded dist generation skipped" );
            return;
        }

        try
        {
            ConfigurationParser configParser = getConfiguration();

            ZipArchiver zipArchiver = prepareArchiver( configParser );

            File distOutputDirectory = new File( project.getBuild().getDirectory(), "dist" );
            getLog().info( "Building dist directory: " + distOutputDirectory.getAbsolutePath() );
            expandArchive( zipArchiver, distOutputDirectory );
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

}
