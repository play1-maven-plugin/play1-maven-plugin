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
import java.net.URL;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.apache.tools.ant.taskdefs.Java;

/**
 * Start Play! server before integration testing.
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal start-server
 * @requiresDependencyResolution test
 */
public class PlayStartServerMojo
    extends AbstractPlayServerMojo
{
    /**
     * Play! id (profile) used for testing.
     * 
     * @parameter expression="${play.testId}" default-value="test"
     * @since 1.0.0
     */
    protected String playTestId;

//    /**
//     * Enable logging mode.
//     *
//     * @parameter expression="${play.serverLogOutput}" default-value="true"
//     * @since 1.0.0
//     */
//    private boolean serverLogOutput;

    /**
     * Skip goal execution
     * 
     * @parameter expression="${play.seleniumSkip}" default-value="false"
     * @since 1.0.0
     */
    private boolean seleniumSkip;

    @Override
    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        if ( seleniumSkip )
        {
            getLog().info( "Skipping execution" );
            return;
        }

        File baseDir = project.getBasedir();

        File pidFile = new File( baseDir, "server.pid" );
        if ( pidFile.exists() )
        {
            throw new MojoExecutionException( String.format( "Play! Server already started (\"%s\" file found)",
                                                             pidFile.getName() ) );
        }

        File confDir = new File( baseDir, "conf" );
        File configurationFile = new File( confDir, "application.conf" );
        ConfigurationParser configParser = new ConfigurationParser( configurationFile, playTestId );
        configParser.parse();

        File buildDirectory = new File( project.getBuild().getDirectory() );
        File logDirectory = new File( buildDirectory, "play" );
        if ( !logDirectory.exists() && !logDirectory.mkdirs() )
        {
            throw new MojoExecutionException( String.format( "Cannot create %s directory",
                                                             logDirectory.getAbsolutePath() ) );
        }

        int serverPort = 9000;
        if ( httpPort != null && !httpPort.isEmpty() ) // TODO-handle "https.port" parameter(?)
        {
            serverPort = Integer.parseInt( httpPort );
        }
        else
        {
            String serverPortStr = configParser.getProperty( "http.port" );
            if ( serverPortStr != null )
            {
                serverPort = Integer.parseInt( serverPortStr );
            }
        }

        Java javaTask = prepareAntJavaTask( configParser, playTestId, true );

        String applicationMode = configParser.getProperty( "application.mode", "dev" );
        if ( "prod".equalsIgnoreCase( applicationMode ) )
        {
            addSystemProperty( javaTask, "pidFile", pidFile.getAbsolutePath() );
        }

        File logFile = new File( logDirectory, "server.log" );
        getLog().info( String.format( "Redirecting output to: %s", logFile.getAbsoluteFile() ) );
        javaTask.setOutput( logFile );

        JavaRunnable runner = new JavaRunnable( javaTask );
        Thread t = new Thread( runner, "Play! Server runner" );
        getLog().info( "Launching Play! Server" );
        t.start();

        // boolean timedOut = false;
        
        /*TimerTask timeoutTask = null;
        if (timeout > 0) {
            TimerTask task = new TimerTask() {
                public void run() {
                    timedOut = true;
                }
            };
            timer.schedule( task, timeout * 1000 );
            //timeoutTask = timer.runAfter(timeout * 1000, {
            //    timedOut = true;
            //})
        }*/
        
        boolean started = false;
        
        getLog().info( "Waiting for Play! Server..." );

        URL connectUrl = new URL( String.format( "http://localhost:%d", serverPort ) );
        int verifyWaitDelay = 1000;
        while ( !started )
        {
            //if (timedOut) {
            //    throw new MojoExecutionException("Unable to verify if Play! Server was started in the given time ($timeout seconds)");
            //}
            
            Exception runnerException = runner.getException();
            if ( runnerException != null )
            {
                throw new MojoExecutionException( "Failed to start Play! Server", runnerException );
            }

            try
            {
                connectUrl.openConnection().getContent();
                started = true;
            }
            catch ( Exception e )
            {
                // return false;
            }

            if ( !started )
            {
                try
                {
                    Thread.sleep( verifyWaitDelay );
                }
                catch ( InterruptedException e )
                {
                    throw new MojoExecutionException( "?", e );
                }
            }
        }
        
        /*if (timeoutTask != null) {
            timeoutTask.cancel();
        }*/

        getLog().info( "Play! Server started" );
        
        Exception startServerException = runner.getException();
        if ( startServerException != null )
        {
            throw new MojoExecutionException( "?", startServerException );
        }
    }

}
