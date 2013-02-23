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
import org.codehaus.plexus.util.FileUtils;

/**
 * Clean Play! temporary directories:
 *  - "db"
 *  - "lib" and "modules"
 *  - "logs"
 *  - "precompiled"
 *  - "test-result"
 *  - "tmp"
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal clean
 * @phase clean
 */
public class PlayCleanMojo
    extends AbstractPlayMojo
{
    /**
     * Default Play! id (profile).
     * 
     * @parameter expression="${play.id}" default-value=""
     * @since 1.0.0
     */
    private String playId;

    /**
     * Should all "cleanable" directories be deleted. If "true", overrides all "cleanXXX" property values.
     * 
     * @parameter expression="${play.cleanAll}" default-value="false"
     * @since 1.0.0
     */
    private boolean cleanAll;

    /**
     * Should "db" directory be deleted.
     * 
     * @parameter expression="${play.cleanDb}" default-value="false"
     * @since 1.0.0
     */
    private boolean cleanDb;

    /**
     * Should "lib" and "modules" directories be deleted.
     * 
     * @parameter expression="${play.cleanDependencies}" default-value="false"
     * @since 1.0.0
     */
    private boolean cleanDependencies;

    /**
     * Should "logs" directory be deleted.
     * 
     * @parameter expression="${play.cleanLogs}" default-value="false"
     * @since 1.0.0
     */
    private boolean cleanLogs;

    /**
     * Should "precompiled" directory be deleted.
     * 
     * @parameter expression="${play.cleanPrecompiled}" default-value="false"
     * @since 1.0.0
     */
    private boolean cleanPrecompiled;

    /**
     * Should "test-result" directory be deleted.
     * 
     * @parameter expression="${play.cleanTestResult}" default-value="false"
     * @since 1.0.0
     */
    private boolean cleanTestResult;

    /**
     * Should "tmp" directory be deleted.
     * 
     * @parameter expression="${play.cleanTmp}" default-value="true"
     * @since 1.0.0
     */
    private boolean cleanTmp;

    /**
     * Skip cleaning.
     * 
     * @parameter expression="${play.cleanSkip}" default-value="false"
     * @required
     * @since 1.0.0
     */
    private boolean cleanSkip;

    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        if ( cleanSkip )
        {
            getLog().info( "Cleaning skipped" );
            return;
        }

        File baseDir = project.getBasedir();

        File pidFile = new File( baseDir, "server.pid" );
        if ( pidFile.exists() )
        {
            throw new MojoExecutionException(
                                              String.format( "Play! Server started (\"%s\" file found), cleaning not allowed",
                                                             pidFile.getName() ) );
        }

        if ( cleanAll || cleanTmp )
        {
            ConfigurationParser configParser =  getConfiguration( playId );
            String tmpDirName = configParser.getProperty( "play.tmp", "tmp" );

            if ( "none".equals( tmpDirName ) )
            {
                getLog().info( "No tmp folder will be used (play.tmp is set to \"none\")" );
            }
            else
            {
                File tmpDir = new File( tmpDirName );
                if ( !tmpDir.isAbsolute() )
                {
                    tmpDir = new File( baseDir, tmpDir.getPath() );
                }
                deleteDirectory( tmpDir );
            }
        }

        if ( cleanAll || cleanDb )
        {
            //TODO - this is temporary solution to avoid deleting "db/evolutions" subdirectory, improve it
            File dbDir = new File( baseDir, "db" );
            if ( dbDir.exists() )
            {
                deleteDirectory( new File( dbDir, "h2" ) );
                if ( dbDir.list().length == 0 ) // empty directory
                {
                    deleteDirectory( dbDir );
                }
            }
        }

        if ( cleanAll || cleanDependencies )
        {
            deleteDirectory( new File( baseDir, "lib" ) );
            deleteDirectory( new File( baseDir, "modules" ) );
        }

        if ( cleanAll || cleanLogs )
        {
            deleteDirectory( new File( baseDir, "logs" ) );
        }

        if ( cleanAll || cleanPrecompiled )
        {
            deleteDirectory( new File( baseDir, "precompiled" ) );
        }

        if ( cleanAll || cleanTestResult )
        {
            deleteDirectory( new File( baseDir, "test-result" ) );
        }
    }

    private void deleteDirectory( File directory )
        throws IOException
    {
        if ( directory.exists() )
        {
            getLog().info( String.format( "Deleting directory %s", directory ) );
            FileUtils.deleteDirectory( directory );
        }
    }

}
