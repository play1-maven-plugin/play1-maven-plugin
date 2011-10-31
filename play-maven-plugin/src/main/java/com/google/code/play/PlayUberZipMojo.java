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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

/**
 * Packages Play! framework and Play! application as one ZIP achive (standalone distribution).
 * WARNING: NOT READY YET! DON'T USE IT!
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal uberzip
 * @phase package
 * @requiresDependencyResolution test
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

    /**
     * Application resources include filter
     * 
     * @parameter expression="${play.uberzipIncludes}" default-value="app/**,conf/**,public/**,tags/**,test/**"
     * @since 1.0.0
     */
    private String uberzipIncludes;

    /**
     * Application resources exclude filter.
     * 
     * @parameter expression="${play.uberzipExcludes}" default-value=""
     * @since 1.0.0
     */
    private String uberzipExcludes;

    /**
     * To look up Archiver/UnArchiver implementations.
     * 
     * @component role="org.codehaus.plexus.archiver.manager.ArchiverManager"
     * @required
     */
    private ArchiverManager archiverManager;

    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        try
        {
            File baseDir = project.getBasedir();
            File destFile = new File( uberzipOutputDirectory, getDestinationFileName() );

            File confDir = new File( baseDir, "conf" );
            File configurationFile = new File( confDir, "application.conf" );
            ConfigurationParser configParser = new ConfigurationParser( configurationFile, playId );
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

            Archiver zipArchiver = archiverManager.getArchiver( "zip" );
            zipArchiver.setDuplicateBehavior( Archiver.DUPLICATES_FAIL );// Just in case
            zipArchiver.setDestFile( destFile );

            // APPLICATION
            getLog().debug( "UberZip includes: " + uberzipIncludes );
            getLog().debug( "UberZip excludes: " + uberzipExcludes );
            String[] includes = ( uberzipIncludes != null ? uberzipIncludes.split( "," ) : null );
            String[] excludes = ( uberzipExcludes != null ? uberzipExcludes.split( "," ) : null );
            zipArchiver.addDirectory( baseDir, "application/", includes, excludes );

            //framework zip dependency
            Artifact frameworkArtifact = findFrameworkArtifact( false ); // TODO if ${play.path} defined use it instead of this dependency
            File frameworkZipFile = frameworkArtifact.getFile();
            zipArchiver.addArchivedFileSet( frameworkZipFile );

            //module zip dependencies
            Map<String, Artifact> moduleArtifacts = findAllModuleArtifacts( false );
            for ( Map.Entry<String, Artifact> moduleArtifactEntry : moduleArtifacts.entrySet() )
            {
                String moduleName = moduleArtifactEntry.getKey();
                Artifact moduleArtifact = moduleArtifactEntry.getValue();

                File moduleZipFile = moduleArtifact.getFile();
                String moduleSubDir = String.format( "application/modules/%s-%s/", moduleName, moduleArtifact.getVersion() );
                if ( Artifact.SCOPE_PROVIDED.equals( moduleArtifact.getScope() ) )
                {
                    moduleSubDir = String.format( "modules/%s/", moduleName/*, moduleArtifact.getVersion()*/ );
                }
                zipArchiver.addArchivedFileSet( moduleZipFile, moduleSubDir );
            }

            Set<?> artifacts = project.getArtifacts();
            for ( Iterator<?> iter = artifacts.iterator(); iter.hasNext(); )
            {
                Artifact artifact = (Artifact) iter.next();
                if ( "jar".equals( artifact.getType() ) )
                {
                    File jarFile = artifact.getFile();
                    String destinationFileName = "application/lib/" + jarFile.getName();
                    // Play! Framework's library
                    if ( Artifact.SCOPE_PROVIDED.equals( artifact.getScope() ) )
                    {
                        // check if there is module zip dependency
                        String moduleName = artifact.getArtifactId();
                        if ( artifact.getArtifactId().startsWith( "play-" ) )
                        {
                            moduleName = moduleName.substring( "play-".length() );
                        }
                        if ( moduleArtifacts.containsKey( moduleName ))
                        {
                            destinationFileName =
                                            String.format( "modules/%s/lib/%s", moduleName, jarFile.getName() );
                        }
                        else
                        {
                            destinationFileName = "framework/lib/" + jarFile.getName();
                            if ( "play".equals( artifact.getArtifactId() ) ) // ???
                            {
                                destinationFileName = "framework/" + jarFile.getName();
                                String playVersion = artifact.getVersion();
                                if ( "1.2".compareTo( playVersion ) > 0 )
                                {
                                    // Play 1.1.x
                                    destinationFileName = "framework/play.jar";
                                }
                            }
                        }
                    }
                    zipArchiver.addFile( jarFile, destinationFileName );
                }
            }

            zipArchiver.createArchive();
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "?", e );
        }
        catch ( NoSuchArchiverException e )
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
        buf.append( ".zip" );
        return buf.toString();
    }

}
