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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;

import org.codehaus.plexus.util.FileUtils;

/**
 * Extracts project dependencies to "lib" and "modules" directories.
 * It's like Play! framework's "dependencies" command, but uses Maven dependencies,
 * instead of "conf/dependencies.yml" file.
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal dependencies
 * @requiresDependencyResolution runtime
 */
public class PlayDependenciesMojo
    extends AbstractPlayMojo
{

    /**
     * Skip dependencies extraction.
     * 
     * @parameter expression="${play.dependenciesSkip}" default-value="false"
     * @required
     * @since 1.0.0
     */
    private boolean dependenciesSkip = false;

    /**
     * Should project's "lib" and "modules" subdirectories be cleaned before dependency resolution.
     * If true, dependenciesOverwrite is meaningless.
     * 
     * @parameter expression="${play.dependenciesClean}" default-value="false"
     * @since 1.0.0
     */
    private boolean dependenciesClean = false;

    /**
     * Should existing dependencies be overwritten.
     * 
     * @parameter expression="${play.dependenciesOverwrite}" default-value="false"
     * @since 1.0.0
     */
    private boolean dependenciesOverwrite = false;

    /**
     * Should jar dependencies be processed. They are necessary for Play! Framework,
     * but not needed for Maven build (Maven uses dependency mechanism).
     * 
     * @parameter expression="${play.dependenciesSkipJars}" default-value="false"
     * @since 1.0.0
     */
    private boolean dependenciesSkipJars = false;

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
        if (dependenciesSkip)
        {
            getLog().info( "Dependencies extraction skipped" );
            return;
        }
        
        try
        {
            if ( dependenciesClean )
            {
                File baseDir = project.getBasedir();
                if ( !dependenciesSkipJars )
                {
                    FileUtils.deleteDirectory( new File( baseDir, "lib" ) );
                }
                FileUtils.deleteDirectory( new File( baseDir, "modules" ) );
            }

            Map<Artifact, File> moduleTypeArtifacts = processModuleDependencies();
            if ( !dependenciesSkipJars )
            {
                processJarDependencies( moduleTypeArtifacts );
            }
        }
        catch ( ArchiverException e )
        {
            // throw new MojoExecutionException( "Error unpacking file [" + file.getAbsolutePath() + "]" + "to ["
            // + unpackDirectory.getAbsolutePath() + "]", e );
            throw new MojoExecutionException( "?", e );
        }
        catch ( NoSuchArchiverException e )
        {
            throw new MojoExecutionException( "?", e );
        }
    }

    private Map<Artifact, File> processModuleDependencies()
        throws ArchiverException, NoSuchArchiverException, IOException
    {
        Map<String, Artifact> moduleArtifacts = findAllModuleArtifacts( true );
        Map<Artifact, File> moduleTypeArtifacts = decompressModuleDependencies( moduleArtifacts );
        return moduleTypeArtifacts;
    }
    
    private Map<Artifact, File> decompressModuleDependencies( Map<String, Artifact> moduleArtifacts )
        throws ArchiverException, NoSuchArchiverException, IOException
    {
        Map<Artifact, File> result = new HashMap<Artifact, File>();

        File baseDir = project.getBasedir();
        File modulesDirectory = new File( baseDir, "modules" );

        for ( Map.Entry<String, Artifact> moduleArtifactEntry : moduleArtifacts.entrySet() )
        {
            String moduleName = moduleArtifactEntry.getKey();
            Artifact moduleArtifact = moduleArtifactEntry.getValue();
            checkPotentialReactorProblem(moduleArtifact);

            //if ( !Artifact.SCOPE_PROVIDED.equals( moduleArtifact.getScope() ) )
            //{
                File zipFile = moduleArtifact.getFile();
                String moduleSubDir = String.format( "%s-%s", moduleName, moduleArtifact.getVersion() );
                File moduleDirectory = new File( modulesDirectory, moduleSubDir );
                createDir( moduleDirectory );
                if ( moduleDirectory.list().length == 0 || moduleDirectory.lastModified() < zipFile.lastModified() )
                {
                    UnArchiver zipUnArchiver = archiverManager.getUnArchiver( "zip" );
                    zipUnArchiver.setSourceFile( zipFile );
                    zipUnArchiver.setDestDirectory( moduleDirectory );
                    zipUnArchiver.setOverwrite( false/* ??true */);
                    zipUnArchiver.extract();
                    moduleDirectory.setLastModified( System.currentTimeMillis() );
                }

                result.put( moduleArtifact, moduleDirectory );
            //}
        }
        return result;
    }

    private void processJarDependencies( Map<Artifact, File> moduleTypeArtifacts )
        throws ArchiverException, NoSuchArchiverException, IOException
    {
        File baseDir = project.getBasedir();
        Set<?> artifacts = project.getArtifacts();

        for ( Iterator<?> iter = artifacts.iterator(); iter.hasNext(); )
        {
            Artifact artifact = (Artifact) iter.next();
            if ( "jar".equals( artifact.getType() ) )
            {
                checkPotentialReactorProblem(artifact);
                //if ( !Artifact.SCOPE_PROVIDED.equals( artifact.getScope() ) )
                //{
                    File libDir = new File( baseDir, "lib" );
                    // System.out.println("jar: " + artifact.getGroupId() + ":" + artifact.getArtifactId());
                    File jarFile = artifact.getFile();
                    for ( Map.Entry<Artifact, File> moduleTypeArtifactEntry : moduleTypeArtifacts.entrySet() )
                    {
                        Artifact moduleArtifact = moduleTypeArtifactEntry.getKey();
                        // System.out.println("checking module: " + moduleArtifact.getGroupId() + ":" +
                        // moduleArtifact.getArtifactId());
                        if ( artifact.getGroupId().equals( moduleArtifact.getGroupId() )
                            && artifact.getArtifactId().equals( moduleArtifact.getArtifactId() ) )
                        {
                            File modulePath = moduleTypeArtifactEntry.getValue();
                            libDir = new File( modulePath, "lib" );
                            // System.out.println("checked ok - lib is " + libDir.getCanonicalPath());
                            break;
                        }
                    }
                    // System.out.println("jar: " + artifact.getGroupId() + ":" + artifact.getArtifactId() +
                    // " added to " +
                    // libDir);
                    createDir( libDir );
                    if ( dependenciesOverwrite )
                    {
                        FileUtils.copyFileToDirectory( jarFile, libDir );
                    }
                    else
                    {
                        FileUtils.copyFileToDirectoryIfModified( jarFile, libDir );
                    }
                //}
            }
        }
    }

    private void createDir( File directory )
        throws IOException
    {
        if ( dependenciesOverwrite && directory.exists() )
        {
            if (directory.isDirectory())
            {
                FileUtils.cleanDirectory( directory );
            }
            else // is file
            {
                if ( !directory.delete() )
                {
                    throw new IOException( String.format( "Cannot delete \"%s\" file", directory.getCanonicalPath() ) );
                }
            }
        }

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

    private void checkPotentialReactorProblem(Artifact artifact) throws ArchiverException
    {
        File artifactFile = artifact.getFile();
        if ( artifactFile.isDirectory() )
        {
            throw new ArchiverException(
                                         String.format( "\"%s:%s:%s:%s\" dependent artifact's file is a directory, not a file. This is probably Maven reactor build problem.",
                                                        artifact.getGroupId(), artifact.getArtifactId(),
                                                        artifact.getType(), artifact.getVersion() ) );
        }
    }
}

// TODO
// 1. Add name conflict detection for modules and jars
