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
 * Stop Play! server. Based on <a
 * href="http://mojo.codehaus.org/selenium-maven-plugin/stop-server-mojo.html">selenium:stop-server mojo</a>
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal stop-server
 */
public class PlayStopServerMojo
    extends AbstractPlayMojo
{
    /**
     * Play! id (profile) used for testing.
     * 
     * @parameter expression="${play.testId}" default-value="test"
     * @since 1.0.0
     */
    protected String playTestId;

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
        File baseDir = project.getBasedir();
        File confDir = new File( baseDir, "conf" );
        File configurationFile = new File( confDir, "application.conf" );

        ConfigurationParser configParser = new ConfigurationParser( configurationFile, playTestId );
        configParser.parse();

        int serverPort = 9000;
        String serverPortStr = configParser.getProperty( "http.port" );
        if ( serverPortStr != null )
        {
            serverPort = Integer.parseInt( serverPortStr );
        }

        if ( seleniumSkip )
        {
            getLog().info( "Skipping execution" );
            return;
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
