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
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

/**
 * Packages Play! framework and Play! application as one ZIP achive (standalone distribution).
 * WARNING: NOT READY YET! DON'T USE IT!
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal uberzip
 * @phase package
 */
public class PlayUberZipMojo
    extends AbstractPlayMojo
{

    // private final static String[] libIncludes = new String[]{"*.jar"};

    // private final static String[] libExcludes = new String[]{"provided-*.jar"};

    // private final static String[] confIncludes = new String[]{"application.conf", "messages", "messages.*",
    // "routes"};

    // private final static String[] moduleExcludes = new String[]{"dist/**", "documentation/**", "lib/**",
    // "nbproject/**", "samples-and-tests/**", "src/**", "build.xml", "commands.py"};

    /**
     * The directory with Play! distribution.
     * 
     * @parameter expression="${play.home}"
     * @since 1.0.0
     */
    protected File playHome;

    /**
     * Default Play! id (profile).
     * 
     * @parameter expression="${play.id}" default-value=""
     * @since 1.0.0
     */
    protected String playId;

    /**
     * The directory for the generated ZIP file.
     * 
     * @parameter expression="${play.uberzipOutputDirectory}" default-value="${project.build.directory}"
     * @required
     * @since 1.0.0
     */
    private String uberzipOutputDirectory;

    /**
     * The name of the generated ZIP file.
     * 
     * @parameter expression="${play.uberzipArchiveName}" default-value="${project.build.finalName}"
     * @required
     * @since 1.0.0
     */
    private String uberzipArchiveName;

    /**
     * Classifier to add to the generated ZIP file.
     * 
     * @parameter expression="${play.uberzipClassifier}" default-value="with-framework"
     * @since 1.0.0
     */
    private String uberzipClassifier;

    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        try
        {
            File baseDir = project.getBasedir();
            File destFile = new File( uberzipOutputDirectory, getDestinationFileName() );

            ConfigurationParser configParser = new ConfigurationParser( new File( baseDir, "conf" ), playId );
            configParser.parse();
            Map<String, String> modules = configParser.getModules();
            //TODO-create method in a base class (create base class for uberzip i war mojos)?
            for (String modulePath: modules.values())
            {
                if (modulePath.contains( "${play.path}" ))
                {
                    checkPlayHome(playHome);
                    break;
                }
            }

            ZipArchiver zipArchiver = new ZipArchiver();
            zipArchiver.setDuplicateBehavior( Archiver.DUPLICATES_FAIL );// Just in case
            zipArchiver.setDestFile( destFile );

            // APPLICATION
            // app
            zipArchiver.addDirectory( new File( baseDir, "app" ), "app/", null, null );
            // conf
            zipArchiver.addDirectory( new File( baseDir, "conf" ), "conf/", null, null );
            // lib
            zipArchiver.addDirectory( new File( baseDir, "lib" ), "lib/", null, null/* or: libIncludes, libExcludes */);// TODO-without subdirectories
            // public
            zipArchiver.addDirectory( new File( baseDir, "public" ), "public/", null, null );
            // tags
            if ( new File( baseDir, "tags" ).isDirectory() )
            {
                zipArchiver.addDirectory( new File( baseDir, "tags" ), "tags/", null, null );
            }

            // PLAY! FRAMEWORK
            // framework
            zipArchiver.addFile( new File( playHome, "framework/play.jar" ), "framework/play.jar" );
            zipArchiver.addDirectory( new File( playHome, "framework/lib" ), "framework/lib/", null, null/*
                                                                                                          * or:
                                                                                                          * libIncludes,
                                                                                                          * libExcludes
                                                                                                          */);// TODO-without subdirectories
            zipArchiver.addDirectory( new File( playHome, "framework/pym" ), "framework/pym/", null, null );
            zipArchiver.addDirectory( new File( playHome, "framework/templates" ), "framework/templates/", null, null );
            // modules
            for ( String moduleName : modules.keySet() )
            {
                String modulePath = modules.get( moduleName );
                modulePath = modulePath.replace( "${play.path}", playHome.getPath() );
                File moduleDir = new File( modulePath );

                zipArchiver.addDirectory( moduleDir, "modules/" + moduleDir.getName() + "/", null, new String[] {
                    "documentation/**", "nbproject/**", "src/**", "build.xml", "commands.py" } );
            }
            // python
            zipArchiver.addDirectory( new File( playHome, "python" ), "python/", null, null );
            // resources
            zipArchiver.addFile( new File( playHome, "resources/messages" ), "resources/messages" );
            // other
            zipArchiver.addDirectory( playHome, "", new String[] { "COPYING", "play", "play.bat", "repositories" },
                                      null );

            zipArchiver.createArchive();
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "?", e );
        }
    }

    private String getDestinationFileName()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( uberzipArchiveName );
        if ( uberzipClassifier != null && !"".equals( uberzipClassifier ) )
        {
            if ( !uberzipClassifier.startsWith( "-" ) )
            {
                buf.append( '-' );
            }
            buf.append( uberzipClassifier );
        }
        buf.append( ".war" );
        return buf.toString();
    }

}
