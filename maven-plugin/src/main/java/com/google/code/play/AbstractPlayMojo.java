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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * Abstract base class for Play! Mojos.
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 */
public abstract class AbstractPlayMojo
    extends AbstractMojo
{

    /**
     * <i>Maven Internal</i>: Project to interact with.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    protected abstract void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !"play".equals( project.getPackaging() ) )
        {
            return;
        }

        try
        {
            internalExecute();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "?", e );
        }
    }

    protected void checkPlayHome( File playHome )
        throws MojoExecutionException
    {
        if ( playHome == null )
        {
            throw new MojoExecutionException(
                                              "Play! home directory (\"playHome\" plugin configuration parameter) not set" );
        }
        if ( !playHome.exists() )
        {
            throw new MojoExecutionException( "Play! home directory " + playHome + " does not exist" );
        }
        if ( !playHome.isDirectory() )
        {
            throw new MojoExecutionException( "Play! home directory " + playHome + " is not a directory" );
        }
    }

    protected final BufferedReader createBufferedFileReader( File file, String encoding )
        throws FileNotFoundException, UnsupportedEncodingException
    {
        return new BufferedReader( new InputStreamReader( new FileInputStream( file ), encoding ) );
    }

    protected final BufferedWriter createBufferedFileWriter( File file, String encoding )
        throws FileNotFoundException, UnsupportedEncodingException
    {
        return new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file ), encoding ) );
    }

    protected String resolvePlayId( File playHome, String defaultPlayId )
        throws IOException
    {
        String result = defaultPlayId;

        if ( result == null || "".equals( result ) )
        {
            result = readFrameworkPlayId( playHome );
        }
        return result;
    }

    private String readFrameworkPlayId( File playHome )
        throws IOException
    {
        String result = null;

        if ( playHome != null )
        {
            File idFile = new File( playHome, "id" );
            if ( idFile.isFile() )
            {
                result = readFileFirstLine(idFile);
            }
        }
        return result;
    }

    protected String readFileFirstLine( File file )
        throws IOException
    {
        String result = null;

        BufferedReader is = new BufferedReader( new InputStreamReader( new FileInputStream( file ), "UTF-8" ) );
        try
        {
            result = is.readLine();
        }
        finally
        {
            is.close();
        }
        return result;
    }

    protected void writeToFile( File file, String line )
        throws IOException
    {
        BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file ), "UTF-8" ) );
        try
        {
            writer.write( line );
        }
        finally
        {
            writer.close();
        }
    }

}
