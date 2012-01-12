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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;

import org.codehaus.plexus.util.FileUtils;

/**
 * Start Play! Server ("play start" equivalent).
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal start
 * @requiresDependencyResolution test
 */
public class PlayStartMojo
    extends AbstractAntJavaBasedPlayMojo
{
    /**
     * Play! id (profile) used for testing.
     * 
     * @parameter expression="${play.id}" default-value=""
     * @since 1.0.0
     */
    protected String playId;

//    /**
//     * Enable logging mode.
//     *
//     * @parameter expression="${play.serverLogOutput}" default-value="true"
//     * @since 1.0.0
//     */
//    private boolean serverLogOutput;

    /**
     * Arbitrary JVM options to set on the command line.
     * 
     * @parameter expression="${play.serverProcessArgLine}"
     * @since 1.0.0
     */
    private String serverProcessArgLine;

    @Override
    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        File playHome = getPlayHome();
        File baseDir = project.getBasedir();

        File confDir = new File( baseDir, "conf" );
        File configurationFile = new File( confDir, "application.conf" );

        ConfigurationParser configParser = new ConfigurationParser( configurationFile, playId );
        configParser.parse();

        int serverPort = 9000;
        String serverPortStr = configParser.getProperty( "http.port" );
        if ( serverPortStr != null )
        {
            serverPort = Integer.parseInt( serverPortStr );
        }

        File buildDirectory = new File( project.getBuild().getDirectory() );
        File logDirectory = new File( buildDirectory, "play" );
        // ant.mkdir(dir: logDirectory)
        if ( !logDirectory.exists() && !logDirectory.mkdirs() )
        {
            // ???
        }

        File userExtensionsJsFile =
            new File( playHome, "modules/testrunner/public/test-runner/selenium/scripts/user-extensions.js" );
        if ( userExtensionsJsFile.isFile() )
        {
            File seleniumDirectory = new File( buildDirectory, "selenium" );
            // ant.mkdir(dir: seleniumDirectory)
            if ( !seleniumDirectory.exists() && !seleniumDirectory.mkdirs() )
            {
                // ???
            }
            FileUtils.copyFileToDirectoryIfModified( userExtensionsJsFile, seleniumDirectory );
        }
        // else??

        Project antProject = createProject();
        Path classPath = getProjectClassPath(antProject, playId);

        Java java = new Java();
        java.setProject( antProject );
        java.setClassname( "com.google.code.play.PlayServerBooter" );
        java.setFork( true );
        java.setDir( baseDir );
        java.setFailonerror( true );
        java.setClasspath( classPath );
        addSystemProperty( java, "play.home", playHome.getAbsolutePath() );
        addSystemProperty( java, "play.id", ( playId != null ? playId : "" ) );
        addSystemProperty( java, "application.path", baseDir.getAbsolutePath() );

        // if (serverLogOutput) {
        File logFile = new File( logDirectory, "server.log" );
        getLog().info( String.format( "Redirecting output to: %s", logFile.getAbsoluteFile() ) );
        java.setOutput( logFile );
        // }

        if ( serverProcessArgLine != null )
        {
            String argLine = serverProcessArgLine.trim();
            if ( !"".equals( argLine ) )
            {
                String[] args = argLine.split( " " );
                for ( String arg : args )
                {
                    Commandline.Argument jvmArg = java.createJvmarg();
                    jvmArg.setValue( arg );
                    // jvmarg(value: arg);
                    getLog().debug( "  Adding jvmarg '" + arg + "'" );
                }
            }
        }

        JavaRunnable runner = new JavaRunnable( java );
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
        
        getLog().info( "Waiting for Play! server...");

        URL connectUrl = new URL(String.format( "http://localhost:%d", serverPort));
        /*private*/ int verifyWaitDelay = 1000;
        while (!started) {
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
