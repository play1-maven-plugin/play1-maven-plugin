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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

/**
 * Initializes Play! Maven project:
 * - Checks Play! home directory and creates temporary Play! home in "target" directory
 * if no Play! home directory defined and there is Play! framework zip dependency
 * in the project.
 * - Adds application and dependent modules sources to Maven project as compile source roots.
 * - Adds application and dependent modules resources to Maven project as resources.
 * - Adds application and dependent modules test sources to Maven project as test compile source roots.
 * - Adds application and dependent modules test resources to Maven project as test resources.
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal initialize
 * @phase initialize
 * @requiresDependencyResolution compile
 */
public class PlayInitializeMojo
    extends AbstractPlayMojo
{

    public static final String playFrameworkVersionFilePath = "framework/src/play/version";
    
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
     * Should application classes be compiled.
     * 
     * @parameter expression="${play.compileApp}" default-value="true"
     * @since 1.0.0
     */
    private boolean compileApp = true;

    /**
     * Should test classes be compiled.
     * 
     * @parameter expression="${play.compileTest}" default-value="true"
     * @since 1.0.0
     */
    private boolean compileTest = true;

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
        String playVersion = null;
        Set<?> artifacts = project.getArtifacts();
        for ( Iterator<?> iter = artifacts.iterator(); iter.hasNext(); )
        {
            Artifact artifact = (Artifact) iter.next();
            // System.out.println("artifact: " + artifact.getGroupId() + ":" + artifact.getArtifactId());
            if ( "play".equals( artifact.getArtifactId() ) )
            {
                // temporary solution, maybe use zip to unzip resource from a jar file
                playVersion = artifact.getVersion();
                // System.out.println("Play version: " + playVersion);
                // java.net.URL artifactUrl = artifact.getFile().toURI().toURL();
            }
        }

        getLog().debug( "Play! version: " + playVersion );
        checkPlayHomeExtended(playVersion);
        playId = resolvePlayId( playHome, playId );

        File baseDir = project.getBasedir();
        File confDir = new File( baseDir, "conf" );
        File configurationFile = new File( confDir, "application.conf" );

        ConfigurationParser configParser = new ConfigurationParser( configurationFile, playId );
        configParser.parse();
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
        if ( ( playVersion != null/* nie podoba mi sie to */) && "1.2".compareTo( /* Play.version */playVersion ) <= 0 )
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
                            getLog().debug( "Added module '" + moduleName + "': " + file.getAbsolutePath() );
                            modules.put( moduleName, file );
                        }
                    }
                }
            }
        }

        if ( compileApp )
        {
            File appPath = new File( baseDir, "app" );
            project.addCompileSourceRoot( appPath.getAbsolutePath() );
            getLog().debug( "Added source directory: " + appPath.getAbsolutePath() );

            Resource resource = new Resource();
            resource.setDirectory( appPath.getAbsolutePath() );
            resource.addExclude( "**/*.java" );
            project.addResource( resource );
            getLog().debug( "Added resource: " + resource.getDirectory() );

            File confPath = new File( baseDir, "conf" );
            resource = new Resource();
            resource.setDirectory( confPath.getAbsolutePath() );
            resource.addExclude( "application.conf" );
            resource.addExclude( "messages" );
            resource.addExclude( "messages.*" );
            resource.addExclude( "routes" );
            project.addResource( resource );
            getLog().debug( "Added resource: " + resource.getDirectory() );

            for ( File modulePath : modules.values() )
            {
                File moduleAppPath = new File( modulePath, "app" );
                if ( moduleAppPath.isDirectory() )
                {
                    project.addCompileSourceRoot( moduleAppPath.getAbsolutePath() );
                    getLog().debug( "Added source directory: " + moduleAppPath.getAbsolutePath() );

                    resource = new Resource();
                    resource.setDirectory( moduleAppPath.getAbsolutePath() );
                    resource.addExclude( "**/*.java" );
                    project.addResource( resource );
                    getLog().debug( "Added resource: " + resource.getDirectory() );
                }
            }
        }

        if ( compileTest )
        {
            File testPath = new File( baseDir, "test" );
            project.addTestCompileSourceRoot( testPath.getAbsolutePath() );
            getLog().debug( "Added test source directory: " + testPath.getAbsolutePath() );

            Resource resource = new Resource();
            resource.setDirectory( testPath.getAbsolutePath() );
            resource.addExclude( "**/*.java" );
            project.addTestResource( resource );
            getLog().debug( "Added test resource: " + resource.getDirectory() );

            // add test sources from dependent modules?
        }
    }

    protected void checkPlayHomeExtended(String playDependencyVersion)
        throws MojoExecutionException, IOException
    {
        if ( playHome == null )
        {
            Artifact frameworkArtifact = findFrameworkArtifact();
            Map<String, Artifact> providedModuleArtifacts = findAllProvidedModuleArtifacts();

            if ( frameworkArtifact != null )
            {
                try
                {
                    decompressFrameworkAndSetPlayHome( frameworkArtifact, providedModuleArtifacts, playDependencyVersion );
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
                String playHomePropertyValue = project.getProperties().getProperty( "play.home" );
                if (playHomePropertyValue != null)
                {
                    throw new MojoExecutionException(
                                                      "Play! home directory not set. There is \"play.home\" project property defined, but not used in play-maven-plugin. Add \"<playHome>${play.home}</playHome>\" to plugin's <configuration> section." );
                }
                else
                {
                    throw new MojoExecutionException(
                                                      "Play! home directory not set. Add \"play.home\" property to project <properties> section and \"<playHome>${play.home}</playHome>\" to play-maven-plugin's <configuration> section" );
                }
            }
        }
        else
        {
            if ( !playHome.exists() )
            {
                throw new MojoExecutionException(
                                                 String.format( "Play! home directory %s does not exist",
                                                                playHome.getCanonicalPath() ) );
            }
            if ( !playHome.isDirectory() )
            {
                throw new MojoExecutionException(
                                                  String.format( "Play! home directory %s is not a directory",
                                                                 playHome.getCanonicalPath() ) );
            }

            File playHomeVersionFile = new File( playHome, playFrameworkVersionFilePath );
            if ( playHomeVersionFile.exists() )
            {
                if ( playHomeVersionFile.isFile() )
                {
                    String playHomeVersion = readFileFirstLine( playHomeVersionFile );
                    if (playHomeVersion == null || !playHomeVersion.equals( playDependencyVersion ))
                    {
                        throw new MojoExecutionException(
                                                         String.format( "Version mismatch: Play! dependency version is %s, but Play! home directory version in %s file is %s.",
                                                                        playDependencyVersion, playHomeVersionFile.getCanonicalPath(), playHomeVersion ) );
                    }
                }
                else
                {
                    throw new MojoExecutionException(
                                                      String.format( "Play! home directory version file %s is not a file",
                                                                     playHomeVersionFile.getCanonicalPath() ) );
                }

            }
            else
            {
                throw new MojoExecutionException(
                                                  String.format( "Play! home directory version file %s does not exist",
                                                                 playHomeVersionFile.getCanonicalPath() ) );
            }
        }
    }

    private Artifact findFrameworkArtifact()
    {
        Artifact result = null;

        Set<?> artifacts = project.getArtifacts();
        for ( Iterator<?> iter = artifacts.iterator(); iter.hasNext(); )
        {
            Artifact artifact = (Artifact) iter.next();
            if ( "zip".equals( artifact.getType() ) )
            {
                if ( "framework".equals( artifact.getClassifier() ) )
                {
                    result = artifact;
                    // System.out.println( "added framework: " + artifact.getGroupId() + ":" + artifact.getArtifactId()
                    // );
                    // don't break, maybe there is "framework-min" artifact too
                }
                // "module-min" overrides "module" (if present)
                else if ( "framework-min".equals( artifact.getClassifier() ) )
                {
                    result = artifact;
                    // System.out.println( "added framework-min: " + artifact.getGroupId() + ":"
                    // + artifact.getArtifactId() );
                    break;
                }
            }
        }
        return result;
    }

    private Map<String, Artifact> findAllProvidedModuleArtifacts()
    {
        Map<String, Artifact> result = new HashMap<String, Artifact>();

        Set<?> artifacts = project.getArtifacts();
        for ( Iterator<?> iter = artifacts.iterator(); iter.hasNext(); )
        {
            Artifact artifact = (Artifact) iter.next();
            if ( Artifact.SCOPE_PROVIDED.equals( artifact.getScope() ) && "zip".equals( artifact.getType() ) )
            {
                if ( "module".equals( artifact.getClassifier() ) || "module-min".equals( artifact.getClassifier() ) )
                {
                    String moduleName = artifact.getArtifactId();
                    if ( moduleName.startsWith( "play-" ) )
                    {
                        moduleName = moduleName.substring( "play-".length() );
                    }

                    if ( "module".equals( artifact.getClassifier() ) )
                    {
                        if ( result.get( moduleName ) == null ) // if "module-min" already in map, don't use
                                                                // "module" artifact
                        {
                            result.put( moduleName, artifact );
                            // System.out.println("added module: " + artifact.getGroupId() + ":" +
                            // artifact.getArtifactId());
                        }
                    }
                    else
                    // "module-min" overrides "module" (if present)
                    {
                        result.put( moduleName, artifact );
                        // System.out.println("added module-min: " + artifact.getGroupId() + ":" +
                        // artifact.getArtifactId());
                    }
                }
            }
        }
        return result;
    }

    private void decompressFrameworkAndSetPlayHome( Artifact frameworkAtifact,
                                                    Map<String, Artifact> providedModuleArtifacts, String playDependencyVersion )
        throws ArchiverException, NoSuchArchiverException, IOException
    {
        File targetDir = new File( project.getBuild().getDirectory() );
        File playTmpDir = new File( targetDir, "play" );

        File playHomeDirectory = new File( playTmpDir, "home" );
        if ( !playHomeDirectory.isDirectory() )
        {
            // decompress framework
            createDir( playHomeDirectory );

            writeToFile( new File( playHomeDirectory, "WARNING.txt" ), "This directory is generated automatically. Don't change its content." );

            UnArchiver zipUnArchiver = archiverManager.getUnArchiver( "zip" );
            zipUnArchiver.setSourceFile( frameworkAtifact.getFile() );
            zipUnArchiver.setDestDirectory( playHomeDirectory );
            zipUnArchiver.setOverwrite( false/* ??true */);
            zipUnArchiver.extract();

            File playFrameworkVersionFile = new File( playHomeDirectory, playFrameworkVersionFilePath );
            createDir( playFrameworkVersionFile.getParentFile() );
            writeToFile( playFrameworkVersionFile, playDependencyVersion );
            
            // decompress provided-scoped modules
            File modulesDirectory = new File( playHomeDirectory, "modules" );
            for ( Map.Entry<String, Artifact> providedModuleArtifactEntry : providedModuleArtifacts.entrySet() )
            {
                String moduleName = providedModuleArtifactEntry.getKey();
                Artifact moduleArtifact = providedModuleArtifactEntry.getValue();

                File moduleDirectory = new File( modulesDirectory, moduleName );
                createDir( moduleDirectory );
                // can I reuse? UnArchiver zipUnArchiver = archiverManager.getUnArchiver( "zip" );
                zipUnArchiver.setSourceFile( moduleArtifact.getFile() );
                zipUnArchiver.setDestDirectory( moduleDirectory );
                zipUnArchiver.setOverwrite( false/* ??true */);
                zipUnArchiver.extract();
            }
        }

        playHome = playHomeDirectory;
        project.getProperties().setProperty( "play.home", playHome.getCanonicalPath() );
    }

    private void createDir( File directory )
        throws IOException
    {
        if ( directory.isFile() )
        {
            getLog().info( String.format( "Deleting \"%s\" file", directory ) );// TODO-more descriptive message
            if ( !directory.delete() )
            {
                throw new IOException( String.format( "Cannot delete \"%s\" file", directory.getCanonicalPath() ) );
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

}
