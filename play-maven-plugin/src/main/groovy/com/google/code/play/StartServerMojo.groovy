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
 * Start Play! server.
 * Based on <a href="http://mojo.codehaus.org/selenium-maven-plugin/start-server-mojo.html">selenium:start-server mojo</a>
 *
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal start-server
 * @requiresDependencyResolution test
 */
class StartServerMojo
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
     * @parameter expression="${play.testId}" default-value="test"
     * @since 1.0.0
     */
    String playTestId

    /**
     * The port number of the server to connect to.
     *
     * @parameter expression="${play.serverPort}" default-value="9000"
     * @required
     * @since 1.0.0
     */
    int serverPort
    
    /**
     * Enable logging mode.
     *
     * @parameter expression="${play.serverLogOutput}" default-value="true"
     * @since 1.0.0
     */
    boolean serverLogOutput

    /**
     * Allows the server startup to be skipped.
     *
     * @parameter expression="${play.seleniumSkip}" default-value="false"
     * @since 1.0.0
     */
    boolean seleniumSkip
    
    /**
     * Arbitrary JVM options to set on the command line.
     *
     * @parameter expression="${play.serverProcessArgLine}"
     * @since 1.0.0
     */
    private String serverProcessArgLine;
    
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

        if (seleniumSkip) {
            log.info('Skipping startup')
            return
        }
        
        def applicationPath = project.basedir;
        
        File buildDirectory = new File( project.build.directory );
        File workingDirectory = new File( buildDirectory, "play" );
        ant.mkdir(dir: workingDirectory)
        
        //if (serverLogOutput) {
        //    ant.mkdir(dir: logFile.parentFile)
        //}
        
        def pluginArifact = { id ->
            def artifact = pluginArtifactMap[id]
            if (!artifact) {
                fail("Unable to locate '$id' in the list of plugin artifacts")
            }
            
            log.debug("Using plugin artifact: ${artifact.file}")
            
            return artifact.file
        }

//System.out.println("playHome:"+playHome);
//System.out.println("applicationPath:"+applicationPath);
//System.out.println("playId:"+playTestId);
        
        def launcher = new ProcessLauncher(name: 'Play! Server', background: true)
        
        launcher.process = {
            ant.java(classname: 'com.google.code.play.PlayServerBooter',
                     fork: true,
                     dir: workingDirectory,
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
                /*classpath() {
                    // Add our plugin artifact to pick up log4j configuration
                    //GS pathelement(location: getClass().protectionDomain.codeSource.location.file)
                    //GS pathelement(location: pluginArifact('log4j:log4j'))
                }*/
                
                if (serverLogOutput) {
                    File logFile = new File( workingDirectory, "server.log" );
                    log.info("Redirecting output to: $logFile")
                    redirector(output: logFile)
                }
                
                sysproperty(key: 'play.home', value: playHome)
                sysproperty(key: 'play.id', value: playTestId)
                sysproperty(key: 'application.path', value: applicationPath)
                
                if (serverProcessArgLine != null) {
                    String argLine = serverProcessArgLine.trim();
                    if (!"".equals(argLine)) {
                        String[] args = argLine.split( " " );
                        for (String arg: args) {
                            jvmarg(value: arg);
                            //System.out.println("jvmarg:'"+arg+"'");
                        }
                    }
                }
            }
        }
        
        URL url = new URL("http://localhost:$serverPort")
        
        launcher.verifier = {
            log.debug("Trying connection to: $url")
            
            try {
                url.openConnection().content
                return true
            }
            catch (Exception e) {
                return false
            }
        }
        
        launcher.launch()
    }

}
