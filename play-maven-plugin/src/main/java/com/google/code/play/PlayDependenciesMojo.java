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
            if ( dependenciesClean )
            {
                File baseDir = project.getBasedir();
                FileUtils.deleteDirectory( new File( baseDir, "lib" ) );
                FileUtils.deleteDirectory( new File( baseDir, "modules" ) );
            }

            Map<Artifact, File> moduleTypeArtifacts = processModuleDependencies();
            processJarDependencies( moduleTypeArtifacts );
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

            //if ( !Artifact.SCOPE_PROVIDED.equals( moduleArtifact.getScope() ) )
            //{
                File zipFile = moduleArtifact.getFile();
                String moduleSubDir = String.format( "%s-%s", moduleName, moduleArtifact.getVersion() );
                File toDirectory = new File( modulesDirectory, moduleSubDir );
                createDir( toDirectory );
                UnArchiver zipUnArchiver = archiverManager.getUnArchiver( "zip" );
                zipUnArchiver.setSourceFile( zipFile );
                zipUnArchiver.setDestDirectory( toDirectory );
                zipUnArchiver.setOverwrite( false/* ??true */);
                zipUnArchiver.extract();

                result.put( moduleArtifact, toDirectory );
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

}

// TODO
// 1. Add name conflict detection for modules and jars
