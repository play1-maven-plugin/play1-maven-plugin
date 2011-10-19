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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
//import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Prepares project for WAR packaging.
 * For now only prepares "web.xml" file (replaces %APPLICATION_NAME% and %PLAY_ID% with actual values).
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal war-support
 * @phase prepare-package
 */
public class PlayWarSupportMojo
    extends AbstractPlayMojo
{
    /**
     * The directory with Play! distribution.
     * 
     * @parameter expression="${play.home}"
     * @since 1.0.0
     */
    protected File playHome;

    /**
     * Play! id (profile) used for WAR packaging.
     * 
     * @parameter expression="${play.warId}" default-value="war"
     * @since 1.0.0
     */
    protected String playWarId;

    @Override
    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        checkPlayHome( playHome );

        File baseDir = project.getBasedir();
        File confDir = new File( baseDir, "conf" );
        File configurationFile = new File( confDir, "application.conf" );

        ConfigurationParser configParser = new ConfigurationParser( configurationFile, playWarId );
        configParser.parse();
        // Map<String, File> modules = configParser.getModules();

        /*
         * File filteredApplicationConf =
         */// filterApplicationConf( new File( baseDir, "conf/application.conf" ), modules );

        File buildDirectory = new File( project.getBuild().getDirectory() );
        File outputDirectory = new File( buildDirectory, "play/tmp" );
        /* File filteredWebXml = */filterWebXml( new File( playHome, "resources/war/web.xml" ), outputDirectory,
                                                 configParser.getApplicationName() );
    }

    private File filterWebXml( File webXml, File outputDirectory, String applicationName )
        throws IOException
    {
        if ( !outputDirectory.exists() )
        {
            if ( !outputDirectory.mkdirs() )
            {
                throw new IOException( String.format( "Cannot create \"%s\" directory",
                                                      outputDirectory.getCanonicalPath() ) );
            }
        }
        File result = new File( outputDirectory, "filtered-web.xml" );
        BufferedReader reader = createBufferedFileReader( webXml, "UTF-8" );
        try
        {
            BufferedWriter writer = createBufferedFileWriter( result, "UTF-8" );
            try
            {
                getLog().debug( "web.xml file:" );
                String line = reader.readLine();
                while ( line != null )
                {
                    getLog().debug( "  " + line );
                    if ( line.indexOf( "%APPLICATION_NAME%" ) >= 0 )
                    {
                        line =
                            line.replace( "%APPLICATION_NAME%", applicationName/* configParser.getApplicationName() */);
                    }
                    if ( line.indexOf( "%PLAY_ID%" ) >= 0 )
                    {
                        line = line.replace( "%PLAY_ID%", playWarId );
                    }
                    writer.write( line );
                    writer.newLine();
                    line = reader.readLine();
                }
            }
            finally
            {
                writer.close();
            }
        }
        finally
        {
            reader.close();
        }
        return result;
    }

/* not needed
    private File filterApplicationConf( File applicationConf, Map<String, File> modules )
        throws IOException
    {
        if ( !outputDirectory.exists() )
        {
            if ( !outputDirectory.mkdirs() )
            {
                throw new IOException( String.format( "Cannot create \"%s\" directory",
                                                      outputDirectory.getCanonicalPath() ) );
            }
        }
        File result = new File( outputDirectory, "filtered-application.conf" );
        BufferedReader reader = createBufferedFileReader( applicationConf, "UTF-8" );
        try
        {
            BufferedWriter writer = createBufferedFileWriter( result, "UTF-8" );
            try
            {
                String line = reader.readLine();
                while ( line != null )
                {
                    if ( !line.trim().startsWith( "#" ) && line.contains( "${play.path}" ) )
                    {
                        line = line.replace( "${play.path}", ".." );
                    }
                    writer.write( line );
                    writer.newLine();
                    line = reader.readLine();
                }
            }
            finally
            {
                writer.close();
            }
        }
        finally
        {
            reader.close();
        }
        return result;
    }
*/

}
