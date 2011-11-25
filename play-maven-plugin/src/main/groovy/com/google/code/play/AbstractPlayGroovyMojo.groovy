/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License") you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package com.google.code.play

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject

import org.codehaus.gmaven.mojo.GroovyMojo

/**
 * Base class for Groovy mojos
 *
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 */
abstract class AbstractPlayGroovyMojo
    extends GroovyMojo
{
    protected File checkPlayHome( File playHome )
        //throws IOException, MojoExecutionException
    {
        File result = playHome;
        
        if ( playHome == null )
        {
            File targetDir = new File( project.build.directory );
            File playTmpDir = new File( targetDir, "play" );
            File playTmpHomeDir = new File( playTmpDir, "home" );
            File warningFile = new File( playTmpHomeDir, "WARNING.txt" );

            if ( warningFile.isFile() )
            {
                result = playTmpHomeDir;
            }
            else
            {
                throw new MojoExecutionException(
                                                  "Play! home directory (\"playHome\" plugin configuration parameter) not set" );
            }
        }
        else
        {
            if ( !playHome.exists() )
            {
                throw new MojoExecutionException( String.format( "Play! home directory %s does not exist",
                                                                 playHome.getCanonicalPath() ) );
            }
            if ( !playHome.isDirectory() )
            {
                throw new MojoExecutionException( String.format( "Play! home directory %s is not a directory",
                                                                 playHome.getCanonicalPath() ) );
            }
        }
        return result;
    }

}
