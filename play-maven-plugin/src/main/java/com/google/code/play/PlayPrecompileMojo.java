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

//import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;

import org.codehaus.plexus.util.FileUtils;

/**
 * Invoke Play! precompilation.
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal precompile
 * @requiresDependencyResolution test
 */
public class PlayPrecompileMojo
    extends AbstractPlayServerMojo
{
    /**
     * Play! id (profile) used when not precompiling tests.
     * 
     * @parameter expression="${play.id}" default-value=""
     * @since 1.0.0
     */
    private String playId;

    /**
     * Play! id (profile) used when precompiling tests.
     * 
     * @parameter expression="${play.testId}" default-value="test"
     * @since 1.0.0
     */
    private String playTestId;

    /**
     * Should tests be precompiled.
     * 
     * @parameter expression="${play.precompileTests}" default-value="false"
     * @since 1.0.0
     */
    private boolean precompileTests;

    /**
     * Precompile in forked Java process.
     * 
     * @parameter expression="${play.precompileFork}" default-value="false"
     * @since 1.0.0
     */
    private boolean precompileFork;

    /**
     * Allows precompilation to be skipped.
     * 
     * @parameter expression="${play.precompileSkip}" default-value="false"
     * @since 1.0.0
     */
    private boolean precompileSkip;

    @Override
    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        if ( precompileSkip )
        {
            getLog().info( "Skipping precompilation" );
            return;
        }

        String precompilePlayId = ( precompileTests ? playTestId : playId );
        
        File playHome = getPlayHome();
        File baseDir = project.getBasedir();

        FileUtils.deleteDirectory( new File( baseDir, "precompiled" ) );
        FileUtils.deleteDirectory( new File( baseDir, "tmp" ) );

        ConfigurationParser configParser =  getConfiguration( precompilePlayId );

        Project antProject = createProject();
        Path classPath = getProjectClassPath( antProject, precompilePlayId );

        Java javaTask = new Java();
        javaTask.setTaskName( "play" );
        javaTask.setProject( antProject );
        javaTask.setClassname( "com.google.code.play.PlayServerBooter" );
        javaTask.setClasspath( classPath );
        javaTask.setFailonerror( true );
        javaTask.setFork( precompileFork );
        if ( precompileFork )
        {
            javaTask.setDir( baseDir );

            boolean memoryInArgs = false;
            String jvmArgs = getServerJvmArgs();
            if ( jvmArgs != null )
            {
                jvmArgs = jvmArgs.trim();
                if ( !jvmArgs.isEmpty() )
                {
                    String[] args = jvmArgs.split( " " );
                    for ( String arg : args )
                    {
                        javaTask.createJvmarg().setValue( arg );
                        getLog().debug( "  Adding jvmarg '" + arg + "'" );
                        if ( arg.startsWith( "-Xm" ) )
                        {
                            memoryInArgs = true;
                        }
                    }
                }
            }
            
            if ( !memoryInArgs )
            {
                String jvmMemory = configParser.getProperty( "jvm.memory" );
                if ( jvmMemory != null )
                {
                    jvmMemory = jvmMemory.trim();
                    if ( !jvmMemory.isEmpty() )
                    {
                        String[] args = jvmMemory.split( " " );
                        for ( String arg : args )
                        {
                            javaTask.createJvmarg().setValue( arg );
                            getLog().debug( "  Adding jvmarg '" + arg + "'" );
                        }
                    }
                }
            }

            // JDK 7 compat
            javaTask.createJvmarg().setValue( "-XX:-UseSplitVerifier" );
        }
        else
        {
            //find and add all system properties in "serverJvmArgs"
            String jvmArgs = getServerJvmArgs();
            if ( jvmArgs != null )
            {
                jvmArgs = jvmArgs.trim();
                if ( !jvmArgs.isEmpty() )
                {
                    String[] args = jvmArgs.split( " " );
                    for ( String arg : args )
                    {
                        if ( arg.startsWith( "-D" ) )
                        {
                            arg = arg.substring( 2 );
                            int p = arg.indexOf( '=' );
                            if ( p >= 0 )
                            {
                                String key = arg.substring( 0, p );
                                String value = arg.substring( p + 1 );
                                getLog().debug( "  Adding system property '" + arg + "'" );
                                addSystemProperty( javaTask, key, value );
                            }
                            else
                            {
                                // TODO - throw an exception
                            }
                        }
                    }
                }
            }
        }
        addSystemProperty( javaTask, "play.home", playHome.getAbsolutePath() );
        addSystemProperty( javaTask, "play.id", ( precompilePlayId != null ? precompilePlayId : "" ) );
        addSystemProperty( javaTask, "application.path", baseDir.getAbsolutePath() );
        addSystemProperty( javaTask, "precompile", "yes"/*Boolean.toString( true )*/ ); // any (not null) value

        JavaRunnable runner = new JavaRunnable( javaTask );
        Thread t = new Thread( runner, "Play! precompilation runner" );
        t.start();
        try
        {
            t.join();
        }
        catch ( InterruptedException e )
        {
            throw new MojoExecutionException( "?", e );
        }
        Exception precompileException = runner.getException();
        if ( precompileException != null )
        {
            throw new MojoExecutionException( "?", precompileException );
        }
    }

}
