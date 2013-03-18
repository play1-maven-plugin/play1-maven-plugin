/*
 * Copyright 2010-2013 Grzegorz Slowikowski
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.shared.artifact.filter.PatternExcludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.PatternIncludesArtifactFilter;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.zip.ZipArchiver;

/**
 * Package Play! application as a ZIP achive.
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @since 1.0.0
 */
@Mojo( name = "zip", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME )
public class PlayZipMojo
    extends AbstractArchivingMojo
{
    /**
     * Application resources include filter
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.zipApplicationIncludes", defaultValue = "app/**,conf/**,public/**,tags/**" )
    private String zipApplicationIncludes;

    /**
     * Application resources exclude filter.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.zipApplicationExcludes", defaultValue = "" )
    private String zipApplicationExcludes;

    /**
     * Should project dependencies ("lib" and "modules" directories) be packaged. No include/exclude filters.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.zipDependencies", defaultValue = "false" )
    private boolean zipDependencies;

    /**
     * Dependency include filter.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.zipDependencyIncludes", defaultValue = "" )
    private String zipDependencyIncludes;

    /**
     * Dependency exclude filter.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.zipDependencyExcludes", defaultValue = "" )
    private String zipDependencyExcludes;

    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        try
        {
            File baseDir = project.getBasedir();
            File zipOutputDirectory = new File( project.getBuild().getDirectory() );
            String zipName = project.getBuild().getFinalName();
            File destFile = new File( zipOutputDirectory, zipName + ".zip" );

            ZipArchiver zipArchiver = getZipArchiver();
            zipArchiver.setDestFile( destFile );

            getLog().debug( "Zip includes: " + zipApplicationIncludes );
            getLog().debug( "Zip excludes: " + zipApplicationExcludes );
            String[] includes = ( zipApplicationIncludes != null ? zipApplicationIncludes.split( "," ) : null );
            String[] excludes = ( zipApplicationExcludes != null ? zipApplicationExcludes.split( "," ) : null );
            zipArchiver.addDirectory( baseDir, includes, excludes );

            if ( zipDependencies )
            {
                processDependencies( zipArchiver );
            }
            checkArchiverForProblems( zipArchiver );
            zipArchiver.createArchive();

            project.getArtifact().setFile( destFile );
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

    private void processDependencies( ZipArchiver zipArchiver )
        throws DependencyTreeBuilderException, IOException
    {
        // preparation
        Set<?> projectArtifacts = project.getArtifacts();

        Set<Artifact> excludedArtifacts = new HashSet<Artifact>();
        /*Artifact playSeleniumJunit4Artifact =
            getDependencyArtifact( projectArtifacts, "com.google.code.maven-play-plugin", "play-selenium-junit4",
                                   "jar" );
        if ( playSeleniumJunit4Artifact != null )
        {
            excludedArtifacts.addAll( getDependencyArtifacts( projectArtifacts, playSeleniumJunit4Artifact ) );
        }*/

        AndArtifactFilter dependencyFilter = new AndArtifactFilter();
        if ( zipDependencyIncludes != null && zipDependencyIncludes.length() > 0 )
        {
            List<String> incl = Arrays.asList( zipDependencyIncludes.split( "," ) ); 
            PatternIncludesArtifactFilter includeFilter =
                new PatternIncludesArtifactFilter( incl, true/* actTransitively */ );

            dependencyFilter.add( includeFilter );
        }
        if ( zipDependencyExcludes != null && zipDependencyExcludes.length() > 0 )
        {
            List<String> excl = Arrays.asList( zipDependencyExcludes.split( "," ) ); 
            PatternExcludesArtifactFilter excludeFilter =
                new PatternExcludesArtifactFilter( excl, true/* actTransitively */ );

            dependencyFilter.add( excludeFilter );
        }

        Set<Artifact> filteredArtifacts = new HashSet<Artifact>(); // TODO-rename to filteredClassPathArtifacts
        for ( Iterator<?> iter = projectArtifacts.iterator(); iter.hasNext(); )
        {
            Artifact artifact = (Artifact) iter.next();
            if ( artifact.getArtifactHandler().isAddedToClasspath() && !excludedArtifacts.contains( artifact ) )
            {
                // TODO-add checkPotentialReactorProblem( artifact );
                if ( dependencyFilter.include( artifact ) )
                {
                    filteredArtifacts.add( artifact );
                }
                else
                {
                    getLog().debug( artifact.toString() + " excluded" );
                }
            }
        }

        // modules/*/lib
        Map<String, Artifact> moduleArtifacts = findAllModuleArtifacts( false );
        for ( Map.Entry<String, Artifact> moduleArtifactEntry : moduleArtifacts.entrySet() )
        {
            String moduleName = moduleArtifactEntry.getKey();
            Artifact moduleZipArtifact = moduleArtifactEntry.getValue();

            File moduleZipFile = moduleZipArtifact.getFile();
            String moduleSubDir = String.format( "modules/%s-%s/", moduleName, moduleZipArtifact.getBaseVersion() );
            zipArchiver.addArchivedFileSet( moduleZipFile, moduleSubDir );
            Set<Artifact> dependencySubtree = getModuleDependencyArtifacts( filteredArtifacts, moduleZipArtifact );
            for ( Artifact classPathArtifact : dependencySubtree )
            {
                File jarFile = classPathArtifact.getFile();
                String destinationFileName = jarFile.getName();
                // Scala module hack
                if ( "scala".equals( moduleName ) )
                {
                    destinationFileName = scalaHack( classPathArtifact );
                }
                String destinationPath =
                                String.format( "modules/%s-%s/lib/%s", moduleName,
                                               moduleZipArtifact.getBaseVersion(), destinationFileName );
                zipArchiver.addFile( jarFile, destinationPath );
                filteredArtifacts.remove( classPathArtifact );
            }

        }

        // lib
        for ( Iterator<?> iter = filteredArtifacts.iterator(); iter.hasNext(); )
        {
            Artifact artifact = (Artifact) iter.next();
            File jarFile = artifact.getFile();
            String destinationFileName = "lib/" + jarFile.getName();
            zipArchiver.addFile( jarFile, destinationFileName );
        }
    }

    private String scalaHack( Artifact dependencyArtifact ) throws IOException
    {
        String destinationFileName = dependencyArtifact.getFile().getName();
        if ( "org.scala-lang".equals( dependencyArtifact.getGroupId() )
            && ( "scala-compiler".equals( dependencyArtifact.getArtifactId() ) || "scala-library".equals( dependencyArtifact.getArtifactId() ) )
            && "jar".equals( dependencyArtifact.getType() ) )
        {
            destinationFileName = dependencyArtifact.getArtifactId() + ".jar";
        }
        return destinationFileName;
    }

}

// TODO - add name conflicts detection for modules and jars
