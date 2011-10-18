/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License") you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package com.google.code.play

import org.apache.maven.project.MavenProject

import org.codehaus.gmaven.mojo.GroovyMojo
import org.codehaus.gmaven.mojo.support.ProcessLauncher

/**
 * Invoke Play! precompilation.
 *
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal precompile
 * @requiresDependencyResolution test
 */
class PrecompileMojo
    extends GroovyMojo
{
    /**
     * ...
     *
     * @parameter expression="${play.home}"
     * @required
     * @since 1.0.0
     */
    String playHome

    /**
     * ...
     *
     * @parameter expression="${play.id}" default-value=""
     * @since 1.0.0
     */
    String playId

    /**
     * Allows the server startup to be skipped.
     *
     * @parameter expression="${play.precompileSkip}" default-value="false"
     * @since 1.0.0
     */
    boolean precompileSkip
    
    //
    // Components
    //
    
    /**
     * The enclosing project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    MavenProject project;

    /**
     * List of artifacts this plugin depends on. Used for resolving the Findbugs coreplugin.
     *
     * @parameter expression="${plugin.artifacts}"
     * @required
     * @readonly
     */
    ArrayList pluginArtifacts

     /**
     * @parameter expression="${plugin.artifactMap}"
     * @required
     * @readonly
     */
    Map pluginArtifactMap

    //
    // Mojo
    //

    void execute() {
        if ( !"play".equals( project.packaging ) )
        {
            return;
        }

        if (precompileSkip) {
            log.info('Skipping precompilation')
            return
        }
        
        def applicationPath = project.basedir;

        ant.delete(dir: new File(applicationPath, "precompiled"))
        ant.delete(dir: new File(applicationPath, "tmp"))
        
        def pluginArifact = { id ->
            def artifact = pluginArtifactMap[id]
            if (!artifact) {
                fail("Unable to locate '$id' in the list of plugin artifacts")
            }
            
            log.debug("Using plugin artifact: ${artifact.file}")
            
            return artifact.file
        }
        
        def launcher = new ProcessLauncher(name: 'Play! precompilation', background: false)
        
        launcher.process = {
            ant.java(classname: 'com.google.code.play.PlayServerBooter',
                     fork: false,
                     failonerror: true)
            {
                classpath() {
                    def projectClasspathElements = project.testClasspathElements
                    def projectClasspathList = projectClasspathElements.findAll{project.build.outputDirectory != it.toString()}

                    projectClasspathList.each() {projectClasspathElement ->
                        log.debug("  Adding to projectArtifact ->" + projectClasspathElement.toString())
                        pathelement(location: projectClasspathElement.toString())
                    }
                    pathelement(location: pluginArifact('com.google.code.maven-play-plugin:play-server-booter'))
                }
                
                sysproperty(key: 'play.home', value: playHome)
                sysproperty(key: 'play.id', value: playId)
                sysproperty(key: 'application.path', value: applicationPath)
                sysproperty(key: 'precompile', value: true)
            }
        }
        
        launcher.launch()
    }

}
