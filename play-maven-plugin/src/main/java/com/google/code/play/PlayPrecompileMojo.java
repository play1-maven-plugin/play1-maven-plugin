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
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Environment;
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
    extends AbstractAntJavaBasedPlayMojo
{
    /**
     * Play! id (profile) used precompilation.
     * 
     * @parameter expression="${play.id}" default-value=""
     * @since 1.0.0
     */
    private String playId;

    /**
     * Allows the server startup to be skipped.
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

        File playHome = getPlayHome();
        File baseDir = project.getBasedir();

        FileUtils.deleteDirectory( new File( baseDir, "precompiled" ) );
        FileUtils.deleteDirectory( new File( baseDir, "tmp" ) );

        Project antProject = createProject();
        Path classPath = new Path( antProject );
        for (Artifact a: (List<Artifact>)project.getTestArtifacts()) {
            classPath.createPathElement().setLocation(a.getFile());
        }
        classPath.createPathElement().setLocation(getPluginArtifact( "com.google.code.maven-play-plugin", "play-server-booter" ).getFile());

        Java java = new Java();
        java.setProject( antProject );
        java.setClassname( "com.google.code.play.PlayServerBooter" );
        java.setFailonerror( true );
        java.setClasspath( classPath );
        
        Environment.Variable sysPropPlayHome = new Environment.Variable();
        sysPropPlayHome.setKey( "play.home" );
        sysPropPlayHome.setValue( playHome.getAbsolutePath() );
        java.addSysproperty( sysPropPlayHome );
        
        Environment.Variable sysPropPlayId = new Environment.Variable();
        sysPropPlayId.setKey( "play.id" );
        sysPropPlayId.setValue( (playId != null ? playId : "") );
        java.addSysproperty( sysPropPlayId );

        Environment.Variable sysPropAppPath = new Environment.Variable();
        sysPropAppPath.setKey( "application.path" );
        sysPropAppPath.setValue( baseDir.getAbsolutePath() );
        java.addSysproperty( sysPropAppPath );
        
        Environment.Variable sysPropPrecompile = new Environment.Variable();
        sysPropPrecompile.setKey( "precompile" );
        sysPropPrecompile.setValue( Boolean.toString(true) );
        java.addSysproperty( sysPropPrecompile );
        
        JavaRunnable runner = new JavaRunnable( java );
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
