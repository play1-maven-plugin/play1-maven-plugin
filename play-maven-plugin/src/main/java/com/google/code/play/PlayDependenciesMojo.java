/*
 * Copyright 2010-2015 Grzegorz Slowikowski
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.PathTool;

import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Extract project dependencies to "lib" and "modules" directories.
 * 
 * It's like Play! framework's "dependencies" command, but uses Maven dependencies,
 * instead of "conf/dependencies.yml" file.
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @since 1.0.0
 */
@Mojo( name = "dependencies", requiresDependencyResolution = ResolutionScope.TEST )
public class PlayDependenciesMojo
    extends AbstractDependencyProcessingPlayMojo
{

    /**
     * Skip dependencies extraction.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.dependenciesSkip", defaultValue = "false" )
    private boolean dependenciesSkip;

    /**
     * Should project's "lib" and "modules" subdirectories be cleaned before dependency resolution.
     * If true, dependenciesOverwrite is meaningless.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.dependenciesClean", defaultValue = "false" )
    private boolean dependenciesClean;

    /**
     * Should existing dependencies be overwritten.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.dependenciesOverwrite", defaultValue = "false" )
    private boolean dependenciesOverwrite;

    /**
     * Should jar dependencies be processed. They are necessary for Play! Framework,
     * but not needed for Maven build (Maven uses dependency mechanism).
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.dependenciesSkipJars", defaultValue = "false" )
    private boolean dependenciesSkipJars; // TODO-change default value to true

    /**
     * To look up Archiver/UnArchiver implementations.
     * 
     */
    @Component
    private ArchiverManager archiverManager;

    /**
     * All projects in the reactor.
     */
    @Parameter( defaultValue = "${reactorProjects}", required = true, readonly = true )
    protected List<MavenProject> reactorProjects;

    /**
     * For M2E integration.
     */
    @Component
    private BuildContext buildContext;

    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        if ( dependenciesSkip )
        {
            getLog().info( "Dependencies extraction skipped" );
            return;
        }

        File baseDir = project.getBasedir();

        try
        {
            if ( dependenciesClean )
            {
                if ( !dependenciesSkipJars )
                {
                    FileUtils.deleteDirectory( new File( baseDir, "lib" ) );
                }
                FileUtils.deleteDirectory( new File( baseDir, "modules" ) );
            }

            Set<?> projectArtifacts = project.getArtifacts();

            Set<Artifact> excludedArtifacts = new HashSet<Artifact>();
            Artifact playSeleniumJunit4Artifact =
                            getDependencyArtifact( projectArtifacts, "com.google.code.maven-play-plugin",
                                                    "play-selenium-junit4", "jar" );
            if ( playSeleniumJunit4Artifact != null )
            {
                excludedArtifacts.addAll( getDependencyArtifacts( projectArtifacts, playSeleniumJunit4Artifact ) );
            }

            Set<Artifact> filteredArtifacts = new HashSet<Artifact>(); // TODO-rename to filteredClassPathArtifacts
            for ( Iterator<?> iter = projectArtifacts.iterator(); iter.hasNext(); )
            {
                Artifact artifact = (Artifact) iter.next();
                if ( artifact.getArtifactHandler().isAddedToClasspath()
                    && !Artifact.SCOPE_PROVIDED.equals( artifact.getScope() ) && !excludedArtifacts.contains( artifact ) )
                {
                    // TODO-add checkPotentialReactorProblem( artifact );
                    filteredArtifacts.add( artifact );
                }
            }

            // modules/*/lib
            File modulesDir = new File( baseDir, "modules" );

            Map<String, Artifact> moduleArtifacts = findAllModuleArtifacts( true );
            for ( Map.Entry<String, Artifact> moduleArtifactEntry : moduleArtifacts.entrySet() )
            {
                String moduleName = moduleArtifactEntry.getKey();
                Artifact moduleZipArtifact = moduleArtifactEntry.getValue();

                if ( !Artifact.SCOPE_PROVIDED.equals( moduleZipArtifact.getScope() ) )
                {
                    boolean foundInReactor = false;
                    for ( MavenProject reactorProject : reactorProjects )
                    {
                        if ( reactorProject != project )
                        {
                            Artifact reactorProjectArtifact = reactorProject.getArtifact();

                            if ( reactorProjectArtifact.getGroupId().equals( moduleZipArtifact.getGroupId() )
                                && reactorProjectArtifact.getArtifactId().equals( moduleZipArtifact.getArtifactId() )
                                && reactorProjectArtifact.getVersion().equals( moduleZipArtifact.getVersion() ) )
                            {
                                File reactorProjectBasedir = reactorProject.getBasedir();
                                String relativePath =
                                    PathTool.getRelativeFilePath( baseDir.getAbsolutePath(),
                                                                  reactorProjectBasedir.getAbsolutePath() );
                                File moduleLinkFile =
                                    new File( modulesDir, String.format( "%s-%s",
                                                                      reactorProject.getArtifact().getArtifactId(),
                                                                      reactorProject.getArtifact().getVersion() ) );
                                if ( moduleLinkFile.isDirectory() )
                                {
                                    getLog().info( String.format( "Deleting \"%s\" directory", moduleLinkFile ) ); // TODO-more descriptive message
                                    FileUtils.deleteDirectory( moduleLinkFile );
                                }
                                else if ( !moduleLinkFile.getParentFile().exists() )
                                {
                                    if ( !moduleLinkFile.getParentFile().mkdirs() )
                                    {
                                        throw new IOException( String.format( "Cannot create \"%s\" directory",
                                                                              moduleLinkFile.getParentFile().getCanonicalPath() ) );
                                    }
                                }

                                writeToFile( moduleLinkFile, relativePath );
                                buildContext.refresh( moduleLinkFile );
                                foundInReactor = true;
                                getLog().info( String.format( "Play! module dependency found in reactor, relative path is \"%s\"", relativePath ) );
                                break;
                            }
                        }
                    }
                    
                    if ( foundInReactor )
                    {
                        break; // TODO-change it
                    }
                    //already not needed checkPotentialReactorProblem( moduleZipArtifact );

                    File moduleZipFile = moduleZipArtifact.getFile();
                    String moduleSubDir =
                                    String.format( "%s-%s", moduleName, moduleZipArtifact.getBaseVersion() );
                    File moduleDirectory = new File( modulesDir, moduleSubDir );
                    createModuleDirectory( moduleDirectory, dependenciesOverwrite
                                           || moduleDirectory.lastModified() < moduleZipFile.lastModified() );
                    if ( moduleDirectory.list().length == 0 )
                    {
                        UnArchiver zipUnArchiver = archiverManager.getUnArchiver( "zip" );
                        zipUnArchiver.setSourceFile( moduleZipFile );
                        zipUnArchiver.setDestDirectory( moduleDirectory );
                        zipUnArchiver.setOverwrite( false/* ??true */ );
                        zipUnArchiver.extract();
                        moduleDirectory.setLastModified( System.currentTimeMillis() );
                        buildContext.refresh( moduleDirectory );
                        // Scala module hack
                        if ( "scala".equals( moduleName ) )
                        {
                            scalaHack( moduleDirectory, filteredArtifacts );
                        }
                        if ( !dependenciesSkipJars )
                        {
                            Set<Artifact> dependencySubtree = getModuleDependencyArtifacts( filteredArtifacts, moduleZipArtifact );

                            if ( !dependencySubtree.isEmpty() )
                            {
                                File moduleLibDir = new File( moduleDirectory, "lib" );
                                createLibDirectory( moduleLibDir );

                                for ( Artifact classPathArtifact : dependencySubtree )
                                {
                                    File jarFile = classPathArtifact.getFile();
                                    if ( dependenciesOverwrite )
                                    {
                                        FileUtils.copyFileToDirectory( jarFile, moduleLibDir );
                                    }
                                    else
                                    {
                                        if ( jarFile == null )
                                        {
                                            getLog().info( "null file" ); // TODO-???
                                        }
                                        FileUtils.copyFileToDirectoryIfModified( jarFile, moduleLibDir );
                                    }
                                    filteredArtifacts.remove( classPathArtifact );
                                }
                            }
                        }
                    }
                    else // just remove dependency tree from "filteredArtifacts" collection 
                    {
                        if ( !dependenciesSkipJars )
                        {
                            Set<Artifact> dependencySubtree = getModuleDependencyArtifacts( filteredArtifacts, moduleZipArtifact );
                            for ( Artifact classPathArtifact : dependencySubtree )
                            {
                                filteredArtifacts.remove( classPathArtifact );
                            }
                        }
                    }
                }
            }

            // lib
            if ( !dependenciesSkipJars && !filteredArtifacts.isEmpty() )
            {
                File libDir = new File( baseDir, "lib" );
                createLibDirectory( libDir );
                for ( Iterator<?> iter = filteredArtifacts.iterator(); iter.hasNext(); )
                {
                    Artifact classPathArtifact = (Artifact) iter.next();
                    File jarFile = classPathArtifact.getFile();
                    // In a reactor (multi-module) build if "play" module depends on "jar" module,
                    // "jar" module artifact's file can be a directory instead of a file.
                    // This happens when "compile" lifecycle phase or any phase between "compile"
                    // and "package" has ben executed before "play:dependencies" mojo
                    // (for example "mvn compile play:dependencies").
                    // How to solve this problem?
                    // Dependency "jar" artifact has to be installed first ("mvn install" for "jar"
                    // module only) or at least "package" phase has to be executed for the whole reactor
                    // before "play:dependencies" ("mvn package play:dependencies").
                    checkPotentialReactorProblem( classPathArtifact );
                    if ( dependenciesOverwrite )
                    {
                        FileUtils.copyFileToDirectory( jarFile, libDir );
                        buildContext.refresh( new File(libDir, jarFile.getName()) );
                    }
                    else
                    {
                        if ( FileUtils.copyFileIfModified( jarFile, new File( libDir, jarFile.getName() ) ) )
                        {
                            buildContext.refresh( new File(libDir, jarFile.getName()) );
                        }
                    }
                }
            }
        }
        catch ( ArchiverException e )
        {
            // throw new MojoExecutionException( "Error unpacking file [" + file.getAbsolutePath() + "]" + "to ["
            // + unpackDirectory.getAbsolutePath() + "]", e );
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

    private void createLibDirectory( File libDirectory )
        throws IOException
    {
        if ( libDirectory.exists() )
        {
            if ( !libDirectory.isDirectory() )
            {
                throw new IOException( String.format( "\"%s\" is not a directory", libDirectory.getCanonicalPath() ) );
            }
        }
        else
        {
            if ( !libDirectory.mkdirs() )
            {
                throw new IOException(
                                       String.format( "Cannot create \"%s\" directory", libDirectory.getCanonicalPath() ) );
            }
        }
    }

    private void checkPotentialReactorProblem( Artifact artifact )
    {
        File artifactFile = artifact.getFile();
        if ( artifactFile.isDirectory() )
        {
            throw new ArchiverException(
                                         String.format( "\"%s:%s:%s:%s\" dependent artifact's file is a directory, not a file. This is probably Maven reactor build problem.",
                                                        artifact.getGroupId(), artifact.getArtifactId(),
                                                        artifact.getType(), artifact.getBaseVersion() ) );
        }
    }

    private void scalaHack( File scalaModuleDirectory, Set<Artifact> filteredArtifacts ) throws IOException
    {
        Set<?> projectArtifacts = project.getArtifacts();
        for ( Iterator<?> iter = projectArtifacts.iterator(); iter.hasNext(); )
        {
            Artifact artifact = (Artifact) iter.next();
            if ( "org.scala-lang".equals( artifact.getGroupId() )
                && ( "scala-compiler".equals( artifact.getArtifactId() ) || "scala-library".equals( artifact.getArtifactId() ) )
                && "jar".equals( artifact.getType() ) )
            {
                File jarFile = artifact.getFile();
                FileUtils.copyFileIfModified( jarFile,
                                              new File( scalaModuleDirectory, "lib/" + artifact.getArtifactId()
                                                  + ".jar" ) );

                filteredArtifacts.remove( artifact );
            }
        }
    }

}

// TODO
// 1. Add name conflict detection for modules and jars
