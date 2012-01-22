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

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;

/**
 * Run Play! Server in test mode ("play test" equivalent).
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal test
 * @requiresDependencyResolution test
 */
public class PlayTestMojo
    extends AbstractPlayServerMojo
{
    /**
     * Play! test id (profile) used.
     * 
     * @parameter expression="${play.testId}" default-value="test"
     * @since 1.0.0
     */
    private String playTestId;

    @Override
    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        File playHome = getPlayHome();
        File baseDir = project.getBasedir();

        Project antProject = createProject();
        Path classPath = getProjectClassPath(antProject, playTestId);

        Java java = new Java();
        java.setProject( antProject );
        java.setClassname( "com.google.code.play.PlayServerBooter" );
        java.setFailonerror( true );
        java.setClasspath( classPath );
        addSystemProperty( java, "play.home", playHome.getAbsolutePath() );
        addSystemProperty( java, "play.id", playTestId );
        addSystemProperty( java, "application.path", baseDir.getAbsolutePath() );

        JavaRunnable runner = new JavaRunnable( java );
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
        Exception precompileException = runner.getException();
        if ( precompileException != null )
        {
            throw new MojoExecutionException( "?", precompileException );
        }

        while ( true ) // wait for Ctrl+C
        {
            try
            {
                Thread.sleep( 10000 );
            }
            catch ( InterruptedException e )
            {
                throw new MojoExecutionException( "?", e );
            }
        }
    }

}
