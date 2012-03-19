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
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;

import org.codehaus.plexus.archiver.ArchiveEntry;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.ResourceIterator;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.war.WarArchiver;
import org.codehaus.plexus.components.io.resources.PlexusIoResource;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.io.RawInputStreamFacade;

/**
 * Package Play! framework and Play! application as a WAR achive.
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal war
 * @phase package
 * @requiresDependencyResolution test
 */
public class PlayWarMojo
    extends AbstractDependencyProcessingPlayMojo
{

    // private final static String[] libIncludes = new String[] { "*.jar" };

    // private final static String[] libExcludes = new String[] { "provided-*.jar" };

    private static final String[] confIncludes =
        new String[] { "application.conf", "messages", "messages.*", "routes" };

    // private final static String[] confIncludes = new String[]{"messages", "messages.*", "routes"};

    // private final static String[] moduleExcludes = new String[] { "dist/**", "documentation/**", "lib/**",
    // "nbproject/**", "samples-and-tests/**", "src/**", "build.xml", "commands.py" };

    /**
     * Play! id (profile) used for WAR packaging.
     * 
     * @parameter expression="${play.warId}" default-value="war"
     * @since 1.0.0
     */
    protected String playWarId;

    /**
     * Skip War generation.
     * 
     * @parameter expression="${play.warSkip}" default-value="false"
     * @required
     * @since 1.0.0
     */
    private boolean warSkip = false;

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

    /**
     * Application resources include filter
     * 
     * @parameter expression="${play.warIncludes}"
     *            default-value="app/**,conf/**,precompiled/**,public/**,tags/**,test/**"
     * @since 1.0.0
     */
    private String warIncludes;

    /**
     * Application resources exclude filter.
     * 
     * @parameter expression="${play.warExcludes}" default-value=""
     * @since 1.0.0
     */
    private String warExcludes;

    /**
     * ...
     * 
     * @parameter expression="${play.warZip}" default-value="true"
     * @required
     * @since 1.0.0
     */
    private boolean warZip;

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
        if ( warSkip )
        {
            getLog().info( "War generation skipped" );
            return;
        }

        try
        {
            File playHome = getPlayHome();

            File baseDir = project.getBasedir();

            File precompiledDir = new File( baseDir, "precompiled" );
            if ( !precompiledDir.exists() )
            {
                throw new MojoExecutionException( String.format( "\"precompiled\" directory %s does not exist",
                                                                 precompiledDir.getCanonicalPath() ) );
            }
            if ( !precompiledDir.isDirectory() )
            {
                throw new MojoExecutionException( String.format( "\"precompiled\" directory %s is not a directory",
                                                                 precompiledDir.getCanonicalPath() ) );
            }

            File buildDirectory = new File( project.getBuild().getDirectory() );

            File destFile = new File( warOutputDirectory, getDestinationFileName() );

            File confDir = new File( baseDir, "conf" );
            File configurationFile = new File( confDir, "application.conf" );
            ConfigurationParser configParser = new ConfigurationParser( configurationFile, playWarId );
            configParser.parse();
            Set<String> providedModuleNames = getProvidedModuleNames( configParser, playWarId, true );

            WarArchiver warArchiver = (WarArchiver) archiverManager.getArchiver( "war" );
            warArchiver.setDuplicateBehavior( Archiver.DUPLICATES_FAIL ); // Just in case
            warArchiver.setDestFile( destFile );

            // APPLICATION
            getLog().debug( "War includes: " + warIncludes );
            getLog().debug( "War excludes: " + warExcludes );
            String[] includes = ( warIncludes != null ? warIncludes.split( "," ) : null );
            // TODO-don't add "test/**" if profile is not test profile
            String[] excludes = ( warExcludes != null ? warExcludes.split( "," ) : null );
            warArchiver.addDirectory( baseDir, "WEB-INF/application/", includes, excludes );

            warArchiver.addClasses( new File( baseDir, "conf" ), confIncludes, null );

            File webXmlFile = new File( baseDir, "war/WEB-INF/web.xml" );
            if ( !webXmlFile.isFile() )
            {
                File tmpDirectory = new File( buildDirectory, "play/tmp" );
                filterWebXml( new File( playHome, "resources/war/web.xml" ), tmpDirectory,
                              configParser.getApplicationName(), playWarId );
                webXmlFile = new File( tmpDirectory, "filtered-web.xml" );
            }
            warArchiver.setWebxml( webXmlFile );

            // preparation
            Set<?> projectArtifacts = project.getArtifacts();

            Set<Artifact> excludedArtifacts = new HashSet<Artifact>();
            Artifact playSeleniumJunit4Artifact =
                getDependencyArtifact( projectArtifacts, "com.google.code.maven-play-plugin", "play-selenium-junit4",
                                       "jar" );
            if ( playSeleniumJunit4Artifact != null )
            {
                excludedArtifacts.addAll( getDependencyArtifacts( projectArtifacts, playSeleniumJunit4Artifact ) );
            }

            Set<Artifact> filteredArtifacts = new HashSet<Artifact>();
            for ( Iterator<?> iter = projectArtifacts.iterator(); iter.hasNext(); )
            {
                Artifact artifact = (Artifact) iter.next();
                if ( artifact.getArtifactHandler().isAddedToClasspath() && !excludedArtifacts.contains( artifact ) )
                {
                    // TODO-add checkPotentialReactorProblem( artifact );
                    filteredArtifacts.add( artifact );
                }
            }

            // framework
            Artifact frameworkZipArtifact = findFrameworkArtifact( true );
            File frameworkZipFile = frameworkZipArtifact.getFile();
            warArchiver.addArchivedFileSet( frameworkZipFile, "WEB-INF/",
                                            "framework/templates/**,resources/messages".split( "," ), null );
            Artifact frameworkJarArtifact =
                getDependencyArtifact( filteredArtifacts, frameworkZipArtifact.getGroupId(),
                                       frameworkZipArtifact.getArtifactId(), "jar" );
            // TODO-validate not null
            Set<Artifact> dependencySubtree = getDependencyArtifacts( filteredArtifacts/* ?? */, frameworkJarArtifact );
            for ( Artifact classPathArtifact : dependencySubtree )
            {
                File jarFile = classPathArtifact.getFile();
                warArchiver.addLib( jarFile );
                filteredArtifacts.remove( classPathArtifact );
            }

            // modules
            Set<Artifact> notActiveProvidedModules = new HashSet<Artifact>();
            Map<String, Artifact> moduleArtifacts = findAllModuleArtifacts( false );
            for ( Map.Entry<String, Artifact> moduleArtifactEntry : moduleArtifacts.entrySet() )
            {
                String moduleName = moduleArtifactEntry.getKey();
                Artifact moduleZipArtifact = moduleArtifactEntry.getValue();

                File moduleZipFile = moduleZipArtifact.getFile();
                String moduleSubDir =
                    String.format( "WEB-INF/application/modules/%s-%s/", moduleName, moduleZipArtifact.getVersion() );
                if ( Artifact.SCOPE_PROVIDED.equals( moduleZipArtifact.getScope() ) )
                {
                    if ( providedModuleNames.contains( moduleName ) )
                    {
                        moduleSubDir =
                            String.format( "WEB-INF/modules/%s/", moduleName/* , moduleArtifact.getVersion() */ );
                        warArchiver.addArchivedFileSet( moduleZipFile, moduleSubDir );
                        dependencySubtree = getModuleDependencyArtifacts( filteredArtifacts, moduleZipArtifact );
                        for ( Artifact classPathArtifact : dependencySubtree )
                        {
                            File jarFile = classPathArtifact.getFile();
                            warArchiver.addLib( jarFile );
                            filteredArtifacts.remove( classPathArtifact );
                        }
                        // Scala hack - NOT NEEDED, war works without it (maybe bacause precompiled == true)
                        //if ( "scala".equals( moduleName ) )
                        //{
                        //    ...
                        //}
                    }
                    else
                    {
                        notActiveProvidedModules.add( moduleZipArtifact );
                    }
                }
                else
                {
                    warArchiver.addArchivedFileSet( moduleZipFile, moduleSubDir );
                    dependencySubtree = getModuleDependencyArtifacts( filteredArtifacts, moduleZipArtifact );
                    for ( Artifact classPathArtifact : dependencySubtree )
                    {
                        File jarFile = classPathArtifact.getFile();
                        warArchiver.addLib( jarFile );
                        filteredArtifacts.remove( classPathArtifact );
                    }
                }
            }

            for ( Artifact moduleZipArtifact : notActiveProvidedModules )
            {
                dependencySubtree = getModuleDependencyArtifacts( filteredArtifacts, moduleZipArtifact );
                filteredArtifacts.removeAll( dependencySubtree );
            }

            // lib
            for ( Iterator<?> iter = filteredArtifacts.iterator(); iter.hasNext(); )
            {
                Artifact artifact = (Artifact) iter.next();
                // TODO-exclude test-scoped dependencies?
                File jarFile = artifact.getFile();
                warArchiver.addLib( jarFile );
            }

            File warDir = new File( baseDir, "war" );
            if ( warDir.isDirectory() )
            {
                if ( warZip )
                {
                    //warArchiver.addDirectory( warDir ); //TODO warOutputDirectory cannot be neither equal war directory nor be a child of war directory
                    /*tmp*/warArchiver.addDirectory( warDir, "", null, new String[] { "WEB-INF/web.xml", destFile.getName()} ); //TODO warOutputDirectory cannot be neither equal war directory nor be a child of war directory
                }
                else
                {
                    String warDirCanonicalPath = warDir.getCanonicalPath();
                    String outputDirCanonicalPath = new File( warOutputDirectory ).getCanonicalPath();
                    if ( !warDirCanonicalPath.equals( outputDirCanonicalPath ) )
                    {
                        warArchiver.addDirectory( warDir );
                    }
                }
            }

            if ( warZip )
            {
                warArchiver.createArchive();
            }
            else
            {
                getLog().info( "Building war: " + new File( warOutputDirectory ).getAbsolutePath() );
                expandArchive( warArchiver );
            }
        }
        catch ( ArchiverException e )
        {
            throw new MojoExecutionException( "?", e );
        }
        catch ( DependencyTreeBuilderException e )
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

    private void expandArchive( Archiver archiver )
        throws IOException
    {
        File destDir = archiver.getDestFile().getParentFile();
        for ( ResourceIterator iter = archiver.getResources(); iter.hasNext(); )
        {
            ArchiveEntry entry = iter.next();
            String name = entry.getName();
            name = name.replace( File.separatorChar, '/' );
            File destFile = new File( destDir, name );

            PlexusIoResource resource = entry.getResource();
            boolean skip = false;
            if ( destFile.exists() )
            {
                long resLastModified = resource.getLastModified();
                if ( resLastModified != PlexusIoResource.UNKNOWN_MODIFICATION_DATE )
                {
                    long destFileLastModified = destFile.lastModified(); // TODO-use this
                    if ( resLastModified <= destFileLastModified )
                    {
                        skip = true;
                    }
                }
            }

            if ( !skip )
            {
                switch ( entry.getType() )
                {
                    case ArchiveEntry.DIRECTORY:
                        destFile.mkdirs(); // change to PlexusUtils, check result
                        break;
                    case ArchiveEntry.FILE:
                        InputStream contents = resource.getContents();
                        RawInputStreamFacade facade = new RawInputStreamFacade( contents );
                        FileUtils.copyStreamToFile( facade, destFile );
                        break;
                    default:
                        throw new RuntimeException( "Unknown archive entry type: " + entry.getType() ); // TODO-polish, what exception class?
                }
                // System.out.println(entry.getName());
            }
        }
    }

}

// TODO
// add "warExclude" option (deleteFrom(war_path, app.readConf('war.exclude').split("|")) where is it from? I don't
// remember and cannot find ;)
