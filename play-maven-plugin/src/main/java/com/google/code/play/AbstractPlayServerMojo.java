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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

/**
 * Base class for Play! Server starting mojos.
 */
public abstract class AbstractPlayServerMojo
    extends AbstractAntJavaBasedPlayMojo
{
    protected Collection<Artifact> getExcludedArtifacts( Set<?> classPathArtifacts, String playId )
        throws IOException
    {
        List<Artifact> result = new ArrayList<Artifact>();

        // Get "application.conf" modules active in "playId" profile
        Collection<String> providedModuleNames = getProvidedModuleNames( playId );

        Map<String, Artifact> moduleArtifacts = findAllModuleArtifacts( true );

        for ( Iterator<?> iter = classPathArtifacts.iterator(); iter.hasNext(); )
        {
            Artifact artifact = (Artifact) iter.next();
            if ( Artifact.SCOPE_PROVIDED.equals( artifact.getScope() ) )
            {
                for ( Map.Entry<String, Artifact> moduleArtifactEntry : moduleArtifacts.entrySet() )
                {
                    Artifact moduleArtifact = moduleArtifactEntry.getValue();
                    if ( Artifact.SCOPE_PROVIDED.equals( moduleArtifact.getScope() ) )
                    {
                        if ( artifact.getGroupId().equals( moduleArtifact.getGroupId() )
                            && artifact.getArtifactId().equals( moduleArtifact.getArtifactId() ) )
                        {
                            String moduleName = moduleArtifactEntry.getKey();
                            if ( !providedModuleNames.contains( moduleName ) )
                            {
                                result.add( artifact );
                            }
                            break;
                        }
                    }
                }
            }
        }

        return result;
    }
    
    protected Path getProjectClassPath( Project antProject, String playId )
        throws MojoExecutionException, IOException
    {
        Path classPath = new Path( antProject );

        Set<?> classPathArtifacts = project.getArtifacts();
        Collection<Artifact> excludedArtifacts = getExcludedArtifacts( classPathArtifacts, playId );
        for ( Iterator<?> iter = classPathArtifacts.iterator(); iter.hasNext(); )
        {
            Artifact artifact = (Artifact) iter.next();
            if ( !excludedArtifacts.contains( artifact ) )
            {
                getLog().debug( String.format( "CP: %s:%s:%s (%s)", artifact.getGroupId(),
                                               artifact.getArtifactId(), artifact.getType(), artifact.getScope() ) );
                classPath.createPathElement().setLocation( artifact.getFile() );
            }
        }
        classPath.createPathElement().setLocation( getPluginArtifact( "com.google.code.maven-play-plugin",
                                                                      "play-server-booter", "jar" ).getFile() );
        return classPath;
    }    

}
