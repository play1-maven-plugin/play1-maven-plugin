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

/**
 * Stop Play! Server ("play stop" equivalent).
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal stop
 */
public class PlayStopMojo
    extends AbstractPlayMojo
{
    /**
     * Play! id (profile) used.
     * 
     * @parameter expression="${play.id}" default-value=""
     * @since 1.0.0
     */
    protected String playId;

    @Override
    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        File baseDir = project.getBasedir();
        
        File pidFile = new File( baseDir, "server.pid" );
        if ( !pidFile.exists() )
        {
            getLog().warn( "\"server.pid\" file not found, trying to stop server anyway." );
        }

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

        getLog().info( "Stopping Play! server..." );

        URL url = new URL( String.format( "http://localhost:%d/@kill", Integer.valueOf( serverPort ) ) );

        getLog().debug( String.format( "Stop request URL: %s", url ) );

        try
        {
            url.openConnection().getContent();
        }
        catch ( java.net.SocketException e )
        {
            // ignore
        }

        getLog().info( "Stop request sent" );
    }

}
