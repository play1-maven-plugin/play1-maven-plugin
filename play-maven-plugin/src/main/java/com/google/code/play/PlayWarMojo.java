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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;

import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.war.WarArchiver;

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
     * @parameter expression="${play.warIncludes}" default-value="app/**,conf/**,precompiled/**,public/**,tags/**,test/**"
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
            Set<String> providedModuleNames = getProvidedModuleNames(configParser, playWarId, true);

            WarArchiver warArchiver = (WarArchiver) archiverManager.getArchiver( "war" );
            warArchiver.setDuplicateBehavior( Archiver.DUPLICATES_FAIL ); // Just in case
            warArchiver.setDestFile( destFile );

            // APPLICATION
            getLog().debug( "War includes: " + warIncludes );
            getLog().debug( "War excludes: " + warExcludes );
            String[] includes = ( warIncludes != null ? warIncludes.split( "," ) : null );
            //TODO-don't add "test/**" if profile is not test profile 
            String[] excludes = ( warExcludes != null ? warExcludes.split( "," ) : null );
            warArchiver.addDirectory( baseDir, "WEB-INF/application/", includes, excludes );

            warArchiver.addClasses( new File( baseDir, "conf" ), confIncludes, null );

            File tmpDirectory = new File( buildDirectory, "play/tmp" );
            /* File filteredWebXml = */filterWebXml( new File( playHome, "resources/war/web.xml" ), tmpDirectory,
                                                     configParser.getApplicationName(), playWarId );
            File filteredWebXmlFile = new File( tmpDirectory, "filtered-web.xml" );
            warArchiver.setWebxml( filteredWebXmlFile );

            // preparation
            Set<?> projectArtifacts = project.getArtifacts();

            Set<Artifact> excludedArtifacts = new HashSet<Artifact>();
            Artifact playSeleniumJunit4Artifact =
                            getDependencyArtifact( projectArtifacts, "com.google.code.maven-play-plugin",
                                                    "play-selenium-junit4", "jar" );
            if (playSeleniumJunit4Artifact != null)
            {
                excludedArtifacts.addAll( getDependencyArtifacts( projectArtifacts, playSeleniumJunit4Artifact ) );
            }

            Set<Artifact> filteredArtifacts = new HashSet<Artifact>();
            for ( Iterator<?> iter = projectArtifacts.iterator(); iter.hasNext(); )
            {
                Artifact artifact = (Artifact) iter.next();
                if ( artifact.getArtifactHandler().isAddedToClasspath() && !excludedArtifacts.contains( artifact ) )
                {
                    //TODO-add checkPotentialReactorProblem( artifact );
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
            //TODO-validate not null
            Set<Artifact> dependencySubtree = getDependencyArtifacts( filteredArtifacts/* ?? */, frameworkJarArtifact );
            for (Artifact classPathArtifact: dependencySubtree)
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
                            String.format( "WEB-INF/modules/%s/", moduleName/* , moduleArtifact.getVersion() */);
                        warArchiver.addArchivedFileSet( moduleZipFile, moduleSubDir );
                        dependencySubtree = getModuleDependencyArtifacts( filteredArtifacts, moduleZipArtifact );
                        for ( Artifact classPathArtifact : dependencySubtree )
                        {
                            File jarFile = classPathArtifact.getFile();
                            warArchiver.addLib( jarFile );
                            filteredArtifacts.remove( classPathArtifact );
                        }
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
                warArchiver.addDirectory( warDir );
            }

            warArchiver.createArchive();
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
