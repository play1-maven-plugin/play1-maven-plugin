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
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.war.WarArchiver;

/**
 * Package Play! framework and Play! application as a WAR achive.
 * WARNING: NOT READY YET! DON'T USE IT!
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal war
 * @phase package
 */
public class PlayWarMojo
    extends AbstractPlayMojo
{

    private final static String[] libIncludes = new String[] { "*.jar" };

    private final static String[] libExcludes = new String[] { "provided-*.jar" };

    private final static String[] confIncludes =
        new String[] { "application.conf", "messages", "messages.*", "routes" };

    // private final static String[] confIncludes = new String[]{"messages", "messages.*", "routes"};

    private final static String[] moduleExcludes = new String[] { "dist/**", "documentation/**", "lib/**",
        "nbproject/**", "samples-and-tests/**", "src/**", "build.xml", "commands.py" };

    /**
     * The directory with Play! distribution.
     * 
     * @parameter expression="${play.home}"
     * @since 1.0.0
     */
    protected File playHome;

    /**
     * Play! id (profile) used for WAR packaging.
     * 
     * @parameter expression="${play.warId}" default-value="war"
     * @since 1.0.0
     */
    protected String playWarId;

    /**
     * The directory for the generated WAR file.
     * 
     * @parameter expression="${play.warOutputDirectory}" default-value="${project.build.directory}"
     * @required
     * @since 1.0.0
     */
    private String warOutputDirectory;

    /**
     * The name of the generated WAR file.
     * 
     * @parameter expression="${play.warArchiveName}" default-value="${project.build.finalName}"
     * @required
     * @since 1.0.0
     */
    private String warArchiveName;

    /**
     * Classifier to add to the generated WAR file.
     * 
     * @parameter expression="${play.warClassifier}" default-value=""
     * @since 1.0.0
     */
    private String warClassifier;

    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        try
        {
            File baseDir = project.getBasedir();
            File destFile = new File( warOutputDirectory, getDestinationFileName() );

            ConfigurationParser configParser = new ConfigurationParser( new File( baseDir, "conf" ), playWarId );
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


            // getLog().debug("1" );

            WarArchiver warArchiver = new WarArchiver();
            warArchiver.setDuplicateBehavior( Archiver.DUPLICATES_ADD );
            warArchiver.setDestFile( destFile );

            // app
            warArchiver.addDirectory( new File( baseDir, "app" ), "WEB-INF/application/app/", null, null );
            // public
            warArchiver.addDirectory( new File( baseDir, "public" ), "WEB-INF/application/public/", null, null );
            // conf
            //File filteredApplicationConf =
            //    filterApplicationConf( new File( baseDir, "conf/application.conf" ), modules );
            //warArchiver.addFile( filteredApplicationConf, "WEB-INF/application/conf/application.conf" );
            warArchiver.addFile( new File( baseDir, "conf/application.conf" ), "WEB-INF/application/conf/application.conf" );
            warArchiver.addDirectory( new File( baseDir, "conf" ), "WEB-INF/application/conf/",
                                      subtract( confIncludes, new String[] { "application.conf" } ), null );
            // warArchiver.addClasses(new File(baseDir, "conf"), null, confIncludes);
            warArchiver.addClasses( new File( baseDir, "conf" ), null, confIncludes );
            // framework
            warArchiver.addDirectory( new File( playHome, "framework/templates" ), "WEB-INF/framework/templates/",
                                      null, null );
            // lib
            warArchiver.addLib( new File( playHome, "framework/play.jar" ) );
            warArchiver.addLibs( new File( playHome, "framework/lib" ), libIncludes, libExcludes );
            warArchiver.addLibs( new File( baseDir, "lib" ), libIncludes, libExcludes );
            // modules
            for ( String moduleName : modules.keySet() )
            {
                String modulePath = modules.get( moduleName );
                modulePath = modulePath.replace( "${play.path}", playHome.getPath() );
                File moduleDir = new File( modulePath );

                warArchiver.addDirectory( moduleDir, "WEB-INF/modules/" + moduleDir.getName() + "/", null,
                                          moduleExcludes );
                if ( new File( modulePath, "lib" ).isDirectory() )
                {
                    warArchiver.addLibs( new File( modulePath, "lib" ), libIncludes, libExcludes );
                }
            }
            // resources
            File filteredWebXml = filterWebXml( new File( playHome, "resources/war/web.xml" ), configParser );
            warArchiver.setWebxml( filteredWebXml );
            warArchiver.addFile( new File( playHome, "resources/messages" ), "WEB-INF/resources/messages" );
            if ( new File( baseDir, "war" ).isDirectory() )
            {
                warArchiver.addDirectory( new File( baseDir, "war" ), "", null, null );
            }

            warArchiver.createArchive();
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "?", e );
        }
    }

    protected void/* String */resolvePlayId()
    {
        // String result = super.resolvePlayId();

        if ( playWarId == null || "".equals( playWarId ) )
        {
            playWarId = "war";
        }
        // return result;
    }

    private String getDestinationFileName()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( warArchiveName );
        if ( warClassifier != null && !"".equals( warClassifier ) )
        {
            if ( !warClassifier.startsWith( "-" ) )
            {
                buf.append( '-' );
            }
            buf.append( warClassifier );
        }
        buf.append( ".war" );
        return buf.toString();
    }

    private File filterWebXml( File webXml, ConfigurationParser configParser )
        throws java.io.IOException
    {
        File resultDir = new File( project.getBuild().getDirectory(), "play/tmp" );
        if ( !resultDir.exists() )
        {
            resultDir.mkdirs();
        }
        File result = new File( resultDir, "filtered-web.xml" );
        BufferedReader reader = createBufferedFileReader( webXml, "UTF-8" );
        try
        {
            BufferedWriter writer = createBufferedFileWriter( result, "UTF-8" );
            try
            {
                String line = reader.readLine();
                while ( line != null )
                {
                    if ( line.indexOf( "%APPLICATION_NAME%" ) >= 0 )
                    {
                        line = line.replace( "%APPLICATION_NAME%", configParser.getApplicationName() );
                    }
                    if ( line.indexOf( "%PLAY_ID%" ) >= 0 )
                    {
                        line = line.replace( "%PLAY_ID%", playWarId );
                    }
                    writer.write( line );
                    writer.newLine();
                    line = reader.readLine();
                }
            }
            finally
            {
                writer.close();
            }
        }
        finally
        {
            reader.close();
        }
        return result;
    }

/*    private File filterApplicationConf( File applicationConf, Map<String, File> modules )
        throws IOException
    {
        File resultDir = new File( project.getBuild().getDirectory(), "play/tmp" );
        if ( !resultDir.exists() )
        {
            resultDir.mkdirs();
        }
        File result = new File( resultDir, "filtered-application.conf" );
        BufferedReader reader = createBufferedFileReader( applicationConf, "UTF-8" );
        try
        {
            BufferedWriter writer = createBufferedFileWriter( result, "UTF-8" );
            try
            {
                String line = reader.readLine();
                while ( line != null )
                {
                    if ( !line.trim().startsWith( "#" ) && line.contains( "${play.path}" ) )
                    {
                        line = line.replace( "${play.path}", ".." );
                    }
                    writer.write( line );
                    writer.newLine();
                    line = reader.readLine();
                }
            }
            finally
            {
                writer.close();
            }
        }
        finally
        {
            reader.close();
        }
        return result;
    }*/

    protected String[] concatenate( String[] array1, String[] array2 )
    {
        // System.arraycopy(src, srcPos, dest, destPos, length);
        java.util.List<String> list1 = Arrays.asList( array1 );
        java.util.List<String> list2 = Arrays.asList( array2 );
        java.util.ArrayList<String> list = new java.util.ArrayList<String>( array1.length + array2.length );
        list.addAll( list1 );
        list.addAll( list2 );
        return list.toArray( new String[list.size()] );
    }

    protected String[] subtract( String[] array1, String[] array2 )
    {
        // System.arraycopy(src, srcPos, dest, destPos, length);
        java.util.List<String> list1 = Arrays.asList( array1 );
        java.util.List<String> list2 = Arrays.asList( array2 );
        java.util.ArrayList<String> list = new java.util.ArrayList<String>( array1.length );
        list.addAll( list1 );
        list.removeAll( list2 );
        return list.toArray( new String[list.size()] );
    }

}

// TODO
// add "warExclude" option (deleteFrom(war_path, app.readConf('war.exclude').split("|")) where is it from? I don't remember and cannot find ;)
