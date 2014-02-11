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

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;

/**
 * Base class for Play&#33; dependency processing mojos.
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 */
public abstract class AbstractDependencyProcessingPlayMojo
    extends AbstractPlayMojo
{

    /**
     * The artifact repository to use.
     *
     */
    @Parameter( property = "localRepository", required = true, readonly = true )
    private ArtifactRepository localRepository;

    /**
     * The artifact factory to use.
     *
     */
    @Component
    private ArtifactFactory artifactFactory;

    /**
     * The artifact metadata source to use.
     *
     */
    @Component
    private ArtifactMetadataSource artifactMetadataSource;

    /**
     * The artifact collector to use.
     *
     */
    @Component
    private ArtifactCollector artifactCollector;

    /**
     * The dependency tree builder to use.
     *
     */
    @Component
    private DependencyTreeBuilder dependencyTreeBuilder;

    /**
     * The computed dependency tree root node of the Maven project.
     */
    private DependencyNode rootNode;

    protected Set<Artifact> getFrameworkDependencyArtifacts( Set<?> classPathArtifacts, Artifact frameworkJarArtifact )
        throws DependencyTreeBuilderException
    {
        Set<Artifact> result = getDependencyArtifactsExt( classPathArtifacts, frameworkJarArtifact );
        return result;
    }
                
    protected Set<Artifact> getModuleDependencyArtifacts( Set<?> classPathArtifacts, Artifact moduleZipArtifact )
        throws DependencyTreeBuilderException
    {
        Set<Artifact> result =
            getDependencyArtifactsExt( classPathArtifacts, moduleZipArtifact );
        return result;
    }
    
    protected Artifact getDependencyArtifact( Set<?> classPathArtifacts, String groupId, String artifactId, String type )
    {
        Artifact result = null;
        for ( Iterator<?> iter = classPathArtifacts.iterator(); iter.hasNext(); )
        {
            Artifact artifact = (Artifact) iter.next();
            if ( groupId.equals( artifact.getGroupId() ) && artifactId.equals( artifact.getArtifactId() )
                && type.equals( artifact.getType() ) )
            {
                result = artifact;
                break;
            }
        }
        return result;
    }
    
    protected Set<Artifact> getDependencyArtifacts( Set<?> classPathArtifacts, Artifact rootArtifact )
        throws DependencyTreeBuilderException
    {
        Set<Artifact> result = null;

        buildDependencyTree();
        DependencyNode artifactNode = findArtifactNode( rootArtifact, rootNode );
        if ( artifactNode != null )
        {
            result = new HashSet<Artifact>();
            addDependencyArtifacts( result, classPathArtifacts, artifactNode );
        }
        else
        {
            result = Collections.emptySet();
        }

        return result;
    }
                
    private Set<Artifact> getDependencyArtifactsExt( Set<?> classPathArtifacts, Artifact rootArtifact )
        throws DependencyTreeBuilderException
    {
        Set<Artifact> result = null;

        buildDependencyTree();
        DependencyNode artifactNode = findArtifactNode( rootArtifact, rootNode );
        if ( artifactNode != null )
        {
            result = new HashSet<Artifact>();
            addDependencyArtifacts( result, classPathArtifacts, artifactNode );
            if ( result.isEmpty() )
            {
                Set<?> projectArtifacts = project.getArtifacts();
                for ( Iterator<?> iter = projectArtifacts.iterator(); iter.hasNext(); )
                {
                    Artifact artifact = (Artifact) iter.next();
                    if ( artifact != rootArtifact && artifact.getGroupId().equals( rootArtifact.getGroupId() )
                        && artifact.getArtifactId().equals( rootArtifact.getArtifactId() ) )
                    {
                        Set<Artifact> tmp = getDependencyArtifacts( classPathArtifacts, artifact );
                        result.addAll( tmp );
                    }
                }
            }
        }
        else
        {
            result = Collections.emptySet();
        }

        return result;
    }

    private void addDependencyArtifacts( Set<Artifact> collection, Set<?> classPathArtifacts, DependencyNode artifactNode )
    {
        if ( artifactNode.getState() == DependencyNode.INCLUDED )
        {
            Artifact artifact = artifactNode.getArtifact();
            // don't use this artifact, because it can be unresolved
            // find corresponding artifact in "classPathArtifacts" set
            // (only if exists, if does not exist - we don't need it)
            for ( Iterator<?> iter = classPathArtifacts.iterator(); iter.hasNext(); )
            {
                Artifact a = (Artifact) iter.next();
                if ( areArtifactsEqual( artifact, a ) )
                {
                    collection.add( a );
                    break;
                }
            }
            List<?> childDependencyNodes = artifactNode.getChildren();
            for ( Iterator<?> iter = childDependencyNodes.iterator(); iter.hasNext(); )
            {
                DependencyNode childNode = (DependencyNode) iter.next();
                addDependencyArtifacts( collection, classPathArtifacts, childNode );
            }
        }
    }

    private DependencyNode findArtifactNode( Artifact artifact, DependencyNode findRootNode )
    {
        DependencyNode result = null;
        if ( findRootNode.getState() == DependencyNode.INCLUDED )
        {
            if ( findRootNode.getArtifact().equals( artifact ) )
            {
                result = findRootNode;
            }
            else
            {
                List<?> childDependencyNodes = findRootNode.getChildren();
                for ( Iterator<?> iter = childDependencyNodes.iterator(); iter.hasNext(); )
                {
                    DependencyNode childNode = (DependencyNode) iter.next();
                    DependencyNode tmp = findArtifactNode( artifact, childNode );
                    if ( tmp != null )
                    {
                        result = tmp;
                        break;
                    }
                }
            }
        }
        return result;
    }

    private boolean areArtifactsEqual( Artifact a1, Artifact a2 )
    {
        boolean result =
            a1.getGroupId().equals( a2.getGroupId() )
                && a1.getArtifactId().equals( a2.getArtifactId() )
                && a1.getType().equals( a2.getType() )
                && ( a1.getClassifier() == null ? a2.getClassifier() == null
                                : a1.getClassifier().equals( a2.getClassifier() ) );
        return result;
    }

    // copied from dependency:tree mojo (v2.4)
    private void buildDependencyTree()
        throws DependencyTreeBuilderException
    {
        if ( rootNode == null )
        {
            rootNode =
                dependencyTreeBuilder.buildDependencyTree( project, localRepository, artifactFactory,
                                                           artifactMetadataSource, null, artifactCollector );
        }
    }
    
    protected Set<String> getProvidedModuleNames( String playId )
        throws IOException
    {
        ConfigurationParser configParser =  getConfiguration( playId );
        return getProvidedModuleNames( configParser, playId, false );
    }

    protected Set<String> getProvidedModuleNames( ConfigurationParser configParser, String playId, boolean forceProdMode )
        throws IOException
    {
        Map<String, String> modulePaths = configParser.getModules();

        Set<String> moduleNames = new HashSet<String>();
        for ( String moduleName : modulePaths.keySet() )
        {
            moduleNames.add( moduleName );
            // getLog().debug( String.format( "Module: %s", moduleName ) );
        }
        //TODO-LATER
        //String modeStr = configParser.getProperty( "application.mode" );
        ////TODO-validate
        //if (!forceProdMode && !modeStr.equalsIgnoreCase( "prod" ))
        //{
        //    moduleNames.add( "docviewer" );//TODO-add only in dev mode
        //}
        if ( !forceProdMode )
        {
            moduleNames.add( "docviewer" );
        }
        if ( isTestProfile( playId ) )
        {
            moduleNames.add( "testrunner" );
        }

        return moduleNames;
    }
    
    private boolean isTestProfile( String playId )
    {
        boolean result = false;
        if ( playId != null )
        {
            result = "test".equals( playId ) || ( playId.startsWith( "test-" ) && playId.length() > 5 );
        }
        return result;
    }
    
}
