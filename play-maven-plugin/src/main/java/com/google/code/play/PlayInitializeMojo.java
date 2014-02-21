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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

import org.codehaus.plexus.util.FileUtils;

import org.sonatype.plexus.build.incremental.BuildContext;

/**
 * Initialize Play&#33; Maven project.
 * 
 * - Checks Play! home directory and creates temporary Play! home in "target" directory
 * if no Play! home directory defined and there is Play! framework zip dependency
 * in the project.
 * - Adds application and dependent modules sources to Maven project as compile source roots.
 * - Adds application resources to Maven project as resources.
 * - Adds application test sources to Maven project as test compile source roots.
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @since 1.0.0
 */
@Mojo( name = "initialize", defaultPhase = LifecyclePhase.INITIALIZE, requiresDependencyResolution = ResolutionScope.COMPILE )
public class PlayInitializeMojo
    extends AbstractPlayMojo
{

    public static final String playFrameworkVersionFilePath = "framework/src/play/version";

    /**
     * Default Play! id (profile).
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.id", defaultValue = "" )
    private String playId;

    /**
     * Should temporary Play! home directory be cleaned before it's reinitializing.
     * If true, homeOverwrite is meaningless.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.homeClean", defaultValue = "false" )
    private boolean homeClean;

    /**
     * Should existing temporary Play! home content be overwritten.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.homeOverwrite", defaultValue = "false" )
    private boolean homeOverwrite;

    /**
     * To look up Archiver/UnArchiver implementations.
     * 
     */
    @Component
    private ArchiverManager archiverManager;

    /**
     * For M2E integration.
     */
    @Component
    private BuildContext buildContext;

    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        String playVersion = null;
        Set<?> artifacts = project.getArtifacts();
        for ( Iterator<?> iter = artifacts.iterator(); iter.hasNext(); )
        {
            Artifact artifact = (Artifact) iter.next();
            // System.out.println("artifact: " + artifact.getGroupId() + ":" + artifact.getArtifactId());
            if ( "play".equals( artifact.getArtifactId() ) )
            {
                // temporary solution, maybe use zip to unzip resource from a jar file
                playVersion = artifact.getBaseVersion();
                // System.out.println("Play version: " + playVersion);
                // java.net.URL artifactUrl = artifact.getFile().toURI().toURL();
                break;
            }
        }

        getLog().debug( "Play! version: " + playVersion );
        File playHome = prepareAndGetPlayHome( playVersion );

        File baseDir = project.getBasedir();

        ConfigurationParser configParser = getConfiguration( playId );
        // Get modules
        Map<String, File> modules = new HashMap<String, File>();

        // Play 1.1.x
        Map<String, String> modulePaths = configParser.getModules();
        for ( Map.Entry<String, String> modulePathEntry : modulePaths.entrySet() )
        {
            String moduleName = modulePathEntry.getKey();
            String modulePath = modulePathEntry.getValue();
            modulePath = modulePath.replace( "${play.path}", playHome.getPath() );
            modules.put( moduleName, new File( modulePath ) );
        }
        // Play 1.2.x
        if ( ( playVersion != null/* I don't like it */ ) && "1.2".compareTo( /* Play.version */playVersion ) <= 0 )
        {
            File modulesDir = new File( baseDir, "modules" );
            if ( modulesDir.isDirectory() )
            {
                File[] files = modulesDir.listFiles();
                if ( files != null )
                {
                    for ( File file : files )
                    {
                        String moduleName = file.getName();
                        if ( file.isDirectory() )
                        {
                            // module itself
                            getLog().debug( "Added module '" + moduleName + "': " + file.getAbsolutePath() );
                            modules.put( moduleName, file );
                        }
                        else if ( file.isFile() )
                        {
                            // shortcut to module located in "modules" subdirectory of Play! framework location
                            String realModulePath = readFileFirstLine( file );
                            // String realModulePath = play.libs.IO.readContentAsString( file );
                            file = new File( realModulePath );
                            if ( !file.isAbsolute() )
                            {
                                file = new File( baseDir, realModulePath );
                            }
                            getLog().debug( "Added module '" + moduleName + "': " + file.getAbsolutePath() );
                            modules.put( moduleName, file );
                        }
                    }
                }
            }
        }

        File appPath = new File( baseDir, "app" );
        if ( !project.getCompileSourceRoots().contains( appPath.getAbsolutePath() ) )
        {
            project.addCompileSourceRoot( appPath.getAbsolutePath() );
            getLog().debug( "Added source directory: " + appPath.getAbsolutePath() );
        }

        File confPath = new File( baseDir, "conf" );
        boolean confResourceAlreadyAdded = false;
        List<Resource> projectResources = project.getResources();
        for ( Resource resource: projectResources )
        {
            if ( resource.getDirectory().equals( confPath.getAbsolutePath() ) )
            {
                confResourceAlreadyAdded = true;
                break;
            }
        }
        if ( !confResourceAlreadyAdded )
        {
            Resource resource = new Resource();
            resource.setDirectory( confPath.getAbsolutePath() );
            project.addResource( resource );
            getLog().debug( "Added resource: " + resource.getDirectory() );
        }

        for ( File modulePath : modules.values() )
        {
            File moduleAppPath = new File( modulePath, "app" );
            if ( moduleAppPath.isDirectory() )
            {
                if ( !project.getCompileSourceRoots().contains( moduleAppPath.getAbsolutePath() ) )
                {
                    project.addCompileSourceRoot( moduleAppPath.getAbsolutePath() );
                    getLog().debug( "Added source directory: " + moduleAppPath.getAbsolutePath() );
                }
            }
        }

        File testPath = new File( baseDir, "test" );
        if ( !project.getTestCompileSourceRoots().contains( testPath.getAbsolutePath() ) )
        {
            project.addTestCompileSourceRoot( testPath.getAbsolutePath() );
            getLog().debug( "Added test source directory: " + testPath.getAbsolutePath() );
        }
    }

    protected File prepareAndGetPlayHome( String playDependencyVersion )
        throws MojoExecutionException, IOException
    {
        File targetDir = new File( project.getBuild().getDirectory() );
        File playTmpDir = new File( targetDir, "play" );
        File playTmpHomeDir = new File( playTmpDir, "home" );
        Artifact frameworkArtifact = findFrameworkArtifact( true );
        Map<String, Artifact> moduleArtifacts = findAllModuleArtifacts( true );

        if ( frameworkArtifact != null )
        {
            try
            {
                decompressFrameworkAndSetPlayHome( frameworkArtifact, moduleArtifacts, playDependencyVersion,
                                                   playTmpHomeDir );
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
        else
        {
            throw new MojoExecutionException( "Missing Play! framework dependency." );
        }
        return playTmpHomeDir;
    }

    private void decompressFrameworkAndSetPlayHome( Artifact frameworkAtifact, Map<String, Artifact> moduleArtifacts,
                                                    String playDependencyVersion, File playTmpHomeDir )
        throws MojoExecutionException, NoSuchArchiverException, IOException
    {
        File warningFile = new File( playTmpHomeDir, "WARNING.txt" );

        if ( homeClean )
        {
            FileUtils.deleteDirectory( playTmpHomeDir );
        }

        if ( playTmpHomeDir.exists() )
        {
            if ( playTmpHomeDir.isDirectory() )
            {
                // Additional check whether the temporary Play! home directory is created by this plugin
                if ( warningFile.exists() )
                {
                    if ( !warningFile.isFile() )
                    {
                        throw new MojoExecutionException(
                                                          String.format( "Play! home directory warning file \"%s\" is not a file",
                                                                         warningFile.getCanonicalPath() ) );
                    }
                }
                else
                {
                    throw new MojoExecutionException(
                                                      String.format( "Play! home directory warning file \"%s\" does not exist",
                                                                     warningFile.getCanonicalPath() ) );
                }
            }
            else
            {
                throw new MojoExecutionException( String.format( "Play! home directory \"%s\" is not a directory",
                                                                 playTmpHomeDir.getCanonicalPath() ) );
            }
        }

        // decompress framework
        createDir( playTmpHomeDir );
        if ( !warningFile.exists() )
        {
            writeToFile( warningFile, "This directory is generated automatically. Don't change its content." );
        }
        File frameworkDir = new File( playTmpHomeDir, "framework" );
        if ( !frameworkDir.exists() || frameworkDir.lastModified() < frameworkAtifact.getFile().lastModified() )
        {
            UnArchiver zipUnArchiver = archiverManager.getUnArchiver( "zip" );
            zipUnArchiver.setSourceFile( frameworkAtifact.getFile() );
            zipUnArchiver.setDestDirectory( playTmpHomeDir );
            zipUnArchiver.setOverwrite( false/* ??true */ );
            zipUnArchiver.extract();

            File playFrameworkVersionFile = new File( playTmpHomeDir, playFrameworkVersionFilePath );
            createDir( playFrameworkVersionFile.getParentFile() );
            writeToFile( playFrameworkVersionFile, playDependencyVersion );
            frameworkDir.setLastModified( System.currentTimeMillis() );
            buildContext.refresh( frameworkDir );
        }

        // decompress provided-scoped modules
        File modulesDirectory = new File( playTmpHomeDir, "modules" );
        for ( Map.Entry<String, Artifact> moduleArtifactEntry : moduleArtifacts.entrySet() )
        {
            String moduleName = moduleArtifactEntry.getKey();
            Artifact moduleArtifact = moduleArtifactEntry.getValue();

            if ( Artifact.SCOPE_PROVIDED.equals( moduleArtifact.getScope() ) )
            {
                File zipFile = moduleArtifact.getFile();
                String moduleSubDir =
                                String.format( "%s-%s", moduleName, moduleArtifact.getBaseVersion() );
                if ( isFrameworkEmbeddedModule( moduleName ) )
                {
                    moduleSubDir = moduleName;
                }
                File moduleDirectory = new File( modulesDirectory, moduleSubDir );
                createModuleDirectory( moduleDirectory,
                                       homeOverwrite || moduleDirectory.lastModified() < zipFile.lastModified() );
                if ( moduleDirectory.list().length == 0 )
                {
                    UnArchiver zipUnArchiver = archiverManager.getUnArchiver( "zip" );
                    zipUnArchiver.setSourceFile( zipFile );
                    zipUnArchiver.setDestDirectory( moduleDirectory );
                    zipUnArchiver.setOverwrite( false/* ??true */ );
                    zipUnArchiver.extract();
                    moduleDirectory.setLastModified( System.currentTimeMillis() );
                    buildContext.refresh( moduleDirectory );
                    // Scala module hack
                    if ( "scala".equals( moduleName ) )
                    {
                        scalaHack( moduleDirectory );
                    }
                }
            }
        }
    }

    private void createDir( File directory )
        throws IOException
    {
        if ( directory.exists() )
        {
            if ( directory.isDirectory() )
            {
                if ( homeOverwrite )
                {
                    FileUtils.cleanDirectory( directory );
                }
            }
            else
            {
                throw new IOException( String.format( "\"%s\" is not a directory", directory.getCanonicalPath() ) );
            }
        }
        else
        {
            if ( !directory.mkdirs() )
            {
                throw new IOException( String.format( "Cannot create \"%s\" directory", directory.getCanonicalPath() ) );
            }
        }

        if ( !directory.exists() )
        {
            if ( !directory.mkdirs() )
            {
                throw new IOException( String.format( "Cannot create \"%s\" directory", directory.getCanonicalPath() ) );
            }
        }
    }

    private void scalaHack( File scalaModuleDirectory ) throws IOException
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
                //FileUtils.copyFileToDirectoryIfModified( jarFile, new File(scalaModuleDirectory, "lib" ) );
                FileUtils.copyFileIfModified( jarFile,
                                              new File( scalaModuleDirectory, "lib/" + artifact.getArtifactId()
                                                  + ".jar" ) );
            }
        }
    }

}
