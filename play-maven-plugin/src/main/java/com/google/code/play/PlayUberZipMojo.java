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

/**
 * Packages Play! framework and Play! application as one ZIP achive (standalone distribution).
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal uberzip
 * @phase package
 * @requiresDependencyResolution test
 */
public class PlayUberZipMojo
    extends AbstractDependencyProcessingPlayMojo
{

    // private final static String[] libIncludes = new String[]{"*.jar"};

    // private final static String[] libExcludes = new String[]{"provided-*.jar"};

    // private final static String[] confIncludes = new String[]{"application.conf", "messages", "messages.*",
    // "routes"};

    // private final static String[] moduleExcludes = new String[]{"dist/**", "documentation/**", "lib/**",
    // "nbproject/**", "samples-and-tests/**", "src/**", "build.xml", "commands.py"};

    /**
     * Default Play! id (profile).
     * 
     * @parameter expression="${play.id}" default-value=""
     * @since 1.0.0
     */
    protected String playId;

    /**
     * Skip UberZip generation.
     * 
     * @parameter expression="${play.uberzipSkip}" default-value="false"
     * @required
     * @since 1.0.0
     */
    private boolean uberzipSkip = false;

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
        if ( uberzipSkip )
        {
            getLog().info( "UberZip generation skipped" );
            return;
        }

        try
        {
            File baseDir = project.getBasedir();
            File destFile = new File( uberzipOutputDirectory, getDestinationFileName() );

            File confDir = new File( baseDir, "conf" );
            File configurationFile = new File( confDir, "application.conf" );
            ConfigurationParser configParser = new ConfigurationParser( configurationFile, playId );
            configParser.parse();
            Set<String> providedModuleNames = getProvidedModuleNames(configParser, playId, false);

            Archiver zipArchiver = archiverManager.getArchiver( "zip" );
            zipArchiver.setDuplicateBehavior( Archiver.DUPLICATES_FAIL ); // Just in case
            zipArchiver.setDestFile( destFile );

            // APPLICATION
            getLog().debug( "UberZip includes: " + uberzipIncludes );
            getLog().debug( "UberZip excludes: " + uberzipExcludes );
            String[] includes = ( uberzipIncludes != null ? uberzipIncludes.split( "," ) : null );
            String[] excludes = ( uberzipExcludes != null ? uberzipExcludes.split( "," ) : null );
            zipArchiver.addDirectory( baseDir, "application/", includes, excludes );

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

            Set<Artifact> filteredArtifacts = new HashSet<Artifact>();//TODO-rename to filteredClassPathArtifacts
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
            Artifact frameworkZipArtifact = findFrameworkArtifact( false );
            File frameworkZipFile = frameworkZipArtifact.getFile();
            zipArchiver.addArchivedFileSet( frameworkZipFile );
            Artifact frameworkJarArtifact =
                            getDependencyArtifact( filteredArtifacts/* ?? */, frameworkZipArtifact.getGroupId(),
                                                   frameworkZipArtifact.getArtifactId(), "jar" );
            //TODO-validate not null
            File frameworkJarFile = frameworkJarArtifact.getFile();
            String frameworkDestinationFileName = "framework/" + frameworkJarFile.getName();
            String playVersion = frameworkJarArtifact.getVersion();
            if ( "1.2".compareTo( playVersion ) > 0 )
            {
                // Play 1.1.x
                frameworkDestinationFileName = "framework/play.jar";
            }
            zipArchiver.addFile( frameworkJarFile, frameworkDestinationFileName );
            filteredArtifacts.remove( frameworkJarArtifact );
            Set<Artifact> dependencySubtree = getDependencyArtifacts( filteredArtifacts/* ?? */, frameworkJarArtifact );
            for (Artifact classPathArtifact: dependencySubtree)
            {
                File jarFile = classPathArtifact.getFile();
                String destinationFileName = "framework/lib/" + jarFile.getName();
                zipArchiver.addFile( jarFile, destinationFileName );
                filteredArtifacts.remove( classPathArtifact );
            }

            // modules/*/lib and application/modules/*/lib
            Set<Artifact> notActiveProvidedModules = new HashSet<Artifact>();
            Map<String, Artifact> moduleArtifacts = findAllModuleArtifacts( false );
            for ( Map.Entry<String, Artifact> moduleArtifactEntry : moduleArtifacts.entrySet() )
            {
                String moduleName = moduleArtifactEntry.getKey();
                Artifact moduleZipArtifact = moduleArtifactEntry.getValue();

                File moduleZipFile = moduleZipArtifact.getFile();
                if ( Artifact.SCOPE_PROVIDED.equals( moduleZipArtifact.getScope() ) )
                {
                    if ( providedModuleNames.contains( moduleName ) )
                    {
                        String moduleSubDir =
                            String.format( "modules/%s/", moduleName/* , moduleArtifact.getVersion() */);
                        zipArchiver.addArchivedFileSet( moduleZipFile, moduleSubDir );
                        dependencySubtree = getModuleDependencyArtifacts( filteredArtifacts, moduleZipArtifact );
                        for ( Artifact classPathArtifact : dependencySubtree )
                        {
                            File jarFile = classPathArtifact.getFile();
                            String destinationFileName =
                                String.format( "modules/%s/lib/%s", moduleName, jarFile.getName() );
                            zipArchiver.addFile( jarFile, destinationFileName );
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
                    String moduleSubDir =
                        String.format( "application/modules/%s-%s/", moduleName, moduleZipArtifact.getVersion() );
                    zipArchiver.addArchivedFileSet( moduleZipFile, moduleSubDir );
                    dependencySubtree = getModuleDependencyArtifacts( filteredArtifacts, moduleZipArtifact );
                    for ( Artifact classPathArtifact : dependencySubtree )
                    {
                        File jarFile = classPathArtifact.getFile();
                        String destinationFileName =
                            String.format( "application/modules/%s-%s/lib/%s", moduleName,
                                           moduleZipArtifact.getVersion(), jarFile.getName() );
                        zipArchiver.addFile( jarFile, destinationFileName );
                        filteredArtifacts.remove( classPathArtifact );
                    }
                }
            }

            for ( Artifact moduleZipArtifact : notActiveProvidedModules )
            {
                dependencySubtree = getModuleDependencyArtifacts( filteredArtifacts, moduleZipArtifact );
                filteredArtifacts.removeAll( dependencySubtree );
            }

            // application/lib
            for ( Iterator<?> iter = filteredArtifacts.iterator(); iter.hasNext(); )
            {
                Artifact artifact = (Artifact) iter.next();
                File jarFile = artifact.getFile();
                String destinationFileName = "application/lib/" + jarFile.getName();
                zipArchiver.addFile( jarFile, destinationFileName );
            }

            zipArchiver.createArchive();
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
