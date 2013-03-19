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
import org.apache.maven.plugins.annotations.Parameter;

import org.apache.maven.shared.artifact.filter.PatternExcludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.PatternIncludesArtifactFilter;

import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;

import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.war.WarArchiver;

/**
 * Base class for Play&#33; war packaging mojos.
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 */
public abstract class AbstractPlayWarMojo
    extends AbstractArchivingMojo
{

    /**
     * Play! id (profile) used for WAR packaging.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.warId", defaultValue = "war" )
    private String playWarId;

    /**
     * Application resources include filter
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.warApplicationIncludes", defaultValue = "app/**,conf/**,precompiled/**,public/**,tags/**,test/**" )
    private String warApplicationIncludes;

    /**
     * Application resources exclude filter.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.warApplicationExcludes", defaultValue = "war/**" )
    private String warApplicationExcludes;

    /**
     * Single directory for extra files to include in the WAR.
     *
     * @since 1.0.0
     */
    @Parameter( property = "play.warWebappDirectory", defaultValue = "${basedir}/war", required = true )
    private File warWebappDirectory;

    /**
     * Dependency include filter.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.warDependencyIncludes", defaultValue = "" )
    private String warDependencyIncludes;

    /**
     * Dependency exclude filter.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.warDependencyExcludes", defaultValue = "" )
    private String warDependencyExcludes;

    /**
     * Conf classpath resources include filter
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.warConfResourcesIncludes", defaultValue = "application.conf,messages,messages.*,routes" )
    private String warConfResourcesIncludes;

    /**
     * Conf classpath resources exclude filter.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.warConfResourcesExcludes", defaultValue = "" )
    private String warConfResourcesExcludes;

    protected void checkIfPrecompiled() throws IOException, MojoExecutionException
    {
        File baseDir = project.getBasedir();

        File precompiledDir = new File( baseDir, "precompiled" );
        if ( !precompiledDir.exists() )
        {
            throw new MojoExecutionException(
                                              String.format( "\"precompiled\" directory %s does not exist. Run \"mvn play:precompile\" first.",
                                                             precompiledDir.getCanonicalPath() ) );
        }
        if ( !precompiledDir.isDirectory() )
        {
            throw new MojoExecutionException( String.format( "\"precompiled\" directory %s is not a directory",
                                                             precompiledDir.getCanonicalPath() ) );
        }
    }
    
    protected WarArchiver prepareArchiver( ConfigurationParser configParser, boolean addWarDirectory )
        throws DependencyTreeBuilderException, IOException, MojoExecutionException, NoSuchArchiverException
    {
        WarArchiver warArchiver = getWarArchiver();

        File playHome = getPlayHome();

        File baseDir = project.getBasedir();
        File buildDirectory = new File( project.getBuild().getDirectory() );

        Set<String> providedModuleNames = getProvidedModuleNames( configParser, playWarId, true );

        // APPLICATION
        getLog().debug( "War application includes: " + warApplicationIncludes );
        getLog().debug( "War application excludes: " + warApplicationExcludes );
        String[] applicationIncludes = null;
        if ( warApplicationIncludes != null )
        {
            applicationIncludes = warApplicationIncludes.split( "," );
        }
        // TODO-don't add "test/**" if profile is not test profile
        String[] applicationExcludes = null;
        if ( warApplicationExcludes != null )
        {
            applicationExcludes = warApplicationExcludes.split( "," );
        }
        warArchiver.addDirectory( baseDir, "WEB-INF/application/", applicationIncludes, applicationExcludes );

        getLog().debug( "War conf classpath resources includes: " + warConfResourcesIncludes );
        getLog().debug( "War conf classpath resources excludes: " + warConfResourcesExcludes );
        String[] confResourcesIncludes = null;
        if ( warConfResourcesIncludes != null )
        {
            confResourcesIncludes = warConfResourcesIncludes.split( "," );
        }
        String[] confResourcesExcludes = null;
        if ( warConfResourcesExcludes != null )
        {
            confResourcesExcludes = warConfResourcesExcludes.split( "," );
        }
        warArchiver.addClasses( new File( baseDir, "conf" ), confResourcesIncludes, confResourcesExcludes );

        File webXmlFile = new File( warWebappDirectory, "WEB-INF/web.xml" );
        if ( !webXmlFile.isFile() )
        {
            File tmpDirectory = new File( buildDirectory, "play/tmp" );
            filterWebXml( new File( playHome, "resources/war/web.xml" ), tmpDirectory,
                          configParser.getApplicationName(), playWarId );
            webXmlFile = new File( tmpDirectory, "filtered-web.xml" );
        }
        warArchiver.setWebxml( webXmlFile );

        // preparation
        Set<?> projectArtifacts = project.getArtifacts();

        Set<Artifact> excludedArtifacts = new HashSet<Artifact>();
        Artifact playSeleniumJunit4Artifact =
            getDependencyArtifact( projectArtifacts, "com.google.code.maven-play-plugin", "play-selenium-junit4",
                                   "jar" );
        if ( playSeleniumJunit4Artifact != null )
        {
            excludedArtifacts.addAll( getDependencyArtifacts( projectArtifacts, playSeleniumJunit4Artifact ) );
        }

        AndArtifactFilter dependencyFilter = new AndArtifactFilter();
        if ( warDependencyIncludes != null && warDependencyIncludes.length() > 0 )
        {
            List<String> incl = Arrays.asList( warDependencyIncludes.split( "," ) ); 
            PatternIncludesArtifactFilter includeFilter =
                new PatternIncludesArtifactFilter( incl, true/* actTransitively */ );

            dependencyFilter.add( includeFilter );
        }
        if ( warDependencyExcludes != null && warDependencyExcludes.length() > 0 )
        {
            List<String> excl = Arrays.asList( warDependencyExcludes.split( "," ) ); 
            PatternExcludesArtifactFilter excludeFilter =
                new PatternExcludesArtifactFilter( excl, true/* actTransitively */ );

            dependencyFilter.add( excludeFilter );
        }

        Set<Artifact> filteredArtifacts = new HashSet<Artifact>();
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

        // framework
        Artifact frameworkZipArtifact = findFrameworkArtifact( true );
        // TODO-validate not null
        File frameworkZipFile = frameworkZipArtifact.getFile();
        warArchiver.addArchivedFileSet( frameworkZipFile, "WEB-INF/",
                                        "framework/templates/**,resources/messages".split( "," ), null );
        Artifact frameworkJarArtifact =
            getDependencyArtifact( filteredArtifacts, frameworkZipArtifact.getGroupId(),
                                   frameworkZipArtifact.getArtifactId(), "jar" );
        // TODO-validate not null
        Set<Artifact> dependencySubtree = getFrameworkDependencyArtifacts( filteredArtifacts, frameworkJarArtifact );
        for ( Artifact classPathArtifact : dependencySubtree )
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
                String.format( "WEB-INF/application/modules/%s-%s/", moduleName, moduleZipArtifact.getBaseVersion() );
            if ( Artifact.SCOPE_PROVIDED.equals( moduleZipArtifact.getScope() ) )
            {
                if ( providedModuleNames.contains( moduleName ) )
                {
                    moduleSubDir =
                        String.format( "WEB-INF/modules/%s-%s/", moduleName, moduleZipArtifact.getBaseVersion() );
                    if ( isFrameworkEmbeddedModule( moduleName ) )
                    {
                        moduleSubDir = String.format( "WEB-INF/modules/%s/", moduleName );
                    }
                    warArchiver.addArchivedFileSet( moduleZipFile, moduleSubDir );
                    dependencySubtree = getModuleDependencyArtifacts( filteredArtifacts, moduleZipArtifact );
                    for ( Artifact classPathArtifact : dependencySubtree )
                    {
                        File jarFile = classPathArtifact.getFile();
                        warArchiver.addLib( jarFile );
                        filteredArtifacts.remove( classPathArtifact );
                    }
                    // Scala hack - NOT NEEDED, war works without it (maybe bacause precompiled == true)
                    //if ( "scala".equals( moduleName ) )
                    //{
                    //    ...
                    //}
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

        if ( addWarDirectory )
        {
            if ( warWebappDirectory.isDirectory() )
            {
                String[] webappIncludes = null;
                if ( getWebappIncludes() != null )
                {
                    webappIncludes = getWebappIncludes().split( "," );
                }
                String[] webappExcludes = null;
                if ( getWebappExcludes() != null )
                {
                    webappExcludes = getWebappExcludes().split( "," );
                }
                warArchiver.addDirectory( warWebappDirectory, webappIncludes, webappExcludes );
            }
        }

        checkArchiverForProblems( warArchiver );
        
        return warArchiver;
    }

    protected String getWebappIncludes()
    {
        return null;
    }

    protected String getWebappExcludes()
    {
        return null;
    }

    protected ConfigurationParser getConfiguration() throws IOException
    {
        return getConfiguration( playWarId );
    }

    protected File getWebappDirectory()
    {
        return warWebappDirectory;
    }
    
}
