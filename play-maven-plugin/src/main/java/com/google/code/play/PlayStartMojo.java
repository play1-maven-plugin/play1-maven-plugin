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

import org.apache.tools.ant.taskdefs.Java;

/**
 * Start Play! Server ("play start" equivalent).
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal start
 * @requiresDependencyResolution test
 */
public class PlayStartMojo
    extends AbstractPlayStartServerMojo
{
    /**
     * Play! id (profile) used when starting server without tests.
     * 
     * @parameter expression="${play.id}" default-value=""
     * @since 1.0.0
     */
    private String playId;

    /**
     * Play! id (profile) used when starting server with tests.
     * 
     * @parameter expression="${play.testId}" default-value="test"
     * @since 1.0.0
     */
    private String playTestId;

    /**
     * Allows the server startup to be skipped.
     * 
     * @parameter expression="${play.startSkip}" default-value="false"
     * @since 1.0.0
     */
    private boolean startSkip;

    /**
     * Start server with test profile.
     * 
     * @parameter expression="${play.startWithTests}" default-value="false"
     * @since 1.0.0
     */
    private boolean startWithTests;

    /**
     * Spawns started JVM process. See <a href="http://ant.apache.org/manual/Tasks/java.html">Ant Java task documentation</a> for details.
     * 
     * @parameter expression="${play.startSpawn}" default-value="true"
     * @since 1.0.0
     */
    private boolean startSpawn;

    /**
     * After starting server wait for "http://localhost:${httpPort}/" URL to be available.
     * 
     * @parameter expression="${play.startSynchro}" default-value="false"
     * @since 1.0.0
     */
    private boolean startSynchro;

    @Override
    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        if ( startSkip )
        {
            getLog().info( "Skipping execution" );
            return;
        }
        
        String startPlayId = ( startWithTests ? playTestId : playId );
        
        File baseDir = project.getBasedir();

        ConfigurationParser configParser =  getConfiguration( startPlayId );

        String sysOut = configParser.getProperty( "application.log.system.out" );
        boolean redirectSysOutToFile = !( "false".equals( sysOut ) || "off".equals( sysOut ) );

        File logFile = null;
        if ( redirectSysOutToFile )
        {
            File logDirectory = new File( baseDir, "logs" );
            logFile = new File( logDirectory, "system.out" );
        }

        if ( redirectSysOutToFile )
        {
            getLog().info( String.format( "Starting Play! Server, output is redirected to %s", logFile.getPath() ) );
        }
        else
        {
            getLog().info( "Starting Play! Server" );
        }

        Java javaTask = getStartServerTask( configParser, startPlayId, logFile, startSpawn );

        JavaRunnable runner = new JavaRunnable( javaTask );
        Thread t = new Thread( runner, "Play! Server runner" );
        t.start();
        try
        {
            t.join();
        }
        catch ( InterruptedException e )
        {
            throw new MojoExecutionException( "?", e );
        }
        Exception startServerException = runner.getException();
        if ( startServerException != null )
        {
            throw new MojoExecutionException( "?", startServerException );
        }
        
        if ( startSynchro )
        {
            String rootUrl = getRootUrl( configParser );

            getLog().info( String.format( "Waiting for %s", rootUrl ) );

            waitForServerStarted( rootUrl, runner );
        }
        
        getLog().info( "Play! Server started" );
    }

}
