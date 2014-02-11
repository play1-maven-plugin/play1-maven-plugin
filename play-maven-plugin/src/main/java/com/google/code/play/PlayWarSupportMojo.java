/*
 * Copyright 2010-2014 Grzegorz Slowikowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.code.play;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.FileUtils;

/**
 * Prepare project for war packaging.
 * For now only prepares "web.xml" file (replaces %APPLICATION_NAME% and %PLAY_ID% with actual values).
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @since 1.0.0
 */
@Mojo( name = "war-support", defaultPhase = LifecyclePhase.PREPARE_PACKAGE )
public class PlayWarSupportMojo
    extends AbstractPlayMojo
{
    /**
     * Play! id (profile) used for WAR packaging.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.warId", defaultValue = "war" )
    private String playWarId;

    /**
     * Single directory for extra files to include in the WAR.
     *
     * @since 1.0.0
     */
    @Parameter( property = "play.warWebappDirectory", defaultValue = "${basedir}/war", required = true )
    private File warWebappDirectory;

    /**
     * Replace %APPLICATION_NAME% and %PLAY_ID% in "WEB-INF/web.xml" file
     * with application name ("application.name" property in "conf/application.conf" file)
     * and Play! war profile name ("playWarId" plugin configuration property value).
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.warFilterWebXml", defaultValue = "true" )
    private boolean warFilterWebXml;

    @Override
    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        ConfigurationParser configParser = getConfiguration( playWarId );

        File buildDirectory = new File( project.getBuild().getDirectory() );
        File tmpDirectory = new File( buildDirectory, "play/tmp" );

        File webXmlFile = new File( warWebappDirectory, "WEB-INF/web.xml" );
        if ( !webXmlFile.isFile() )
        {
            File playHome = getPlayHome();
            webXmlFile = new File( playHome, "resources/war/web.xml" );
        }
        if ( warFilterWebXml )
        {
            filterWebXml( webXmlFile, tmpDirectory, configParser.getApplicationName(), playWarId );
        }
        else
        {
            File filteredWebXmlFile = new File( tmpDirectory, "filtered-web.xml" );
            FileUtils.copyFile( webXmlFile, filteredWebXmlFile );
        }
    }

}
