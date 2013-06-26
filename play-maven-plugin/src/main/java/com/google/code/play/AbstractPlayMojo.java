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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;

import org.codehaus.plexus.util.FileUtils;

/**
 * Base class for all Play&#33; mojos.
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 */
public abstract class AbstractPlayMojo
    extends AbstractMojo
{

    /**
     * <i>Maven Internal</i>: Project to interact with.
     * 
     */
    @Component
    protected MavenProject project;

    protected abstract void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException;

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( !"play".equals( project.getPackaging() ) )
        {
            return;
        }

        try
        {
            internalExecute();
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "?", e );
        }
    }

    protected File getPlayHome()
        throws IOException, MojoExecutionException
    {
        File targetDir = new File( project.getBuild().getDirectory() );
        File playTmpDir = new File( targetDir, "play" );
        File playTmpHomeDir = new File( playTmpDir, "home" );
        if ( !playTmpHomeDir.exists() )
        {
            throw new MojoExecutionException(
                                              String.format( "Play! home directory \"%s\" does not exist. Run \"mvn play:initialize\" first.",
                                                             playTmpHomeDir.getCanonicalPath() ) );
        }
        if ( !playTmpHomeDir.isDirectory() )
        {
            throw new MojoExecutionException( String.format( "Play! home directory \"%s\" is not a directory",
                                                             playTmpHomeDir.getCanonicalPath() ) );
        }
        // Additional check whether the temporary Play! home directory is created by this plugin
        File warningFile = new File( playTmpHomeDir, "WARNING.txt" );
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
            throw new MojoExecutionException( String.format( "Play! home directory warning file \"%s\" does not exist",
                                                             warningFile.getCanonicalPath() ) );
        }
        return playTmpHomeDir;
    }

    protected final BufferedReader createBufferedFileReader( File file, String encoding )
        throws FileNotFoundException, UnsupportedEncodingException
    {
        return new BufferedReader( new InputStreamReader( new FileInputStream( file ), encoding ) );
    }

    protected final BufferedWriter createBufferedFileWriter( File file, String encoding )
        throws FileNotFoundException, UnsupportedEncodingException
    {
        return new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file ), encoding ) );
    }

    protected String readFileFirstLine( File file )
        throws IOException
    {
        String result = null;

        BufferedReader is = new BufferedReader( new InputStreamReader( new FileInputStream( file ), "UTF-8" ) );
        try
        {
            result = is.readLine();
        }
        finally
        {
            is.close();
        }
        return result;
    }

    protected void writeToFile( File file, String line )
        throws IOException
    {
        BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file ), "UTF-8" ) );
        try
        {
            writer.write( line );
        }
        finally
        {
            writer.close();
        }
    }

    // used by "initialize", "dist" and "war" mojos
    protected Artifact findFrameworkArtifact( boolean minVersionWins )
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
                    if ( !minVersionWins )
                    {
                        break;
                    }
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
                    if ( minVersionWins )
                    {
                        break;
                    }
                }
            }
        }
        return result;
    }

    protected Map<String, Artifact> findAllModuleArtifacts( boolean minVersionWins )
    {
        Map<String, Artifact> result = new HashMap<String, Artifact>();

        Set<?> artifacts = project.getArtifacts();
        for ( Iterator<?> iter = artifacts.iterator(); iter.hasNext(); )
        {
            Artifact artifact = (Artifact) iter.next();
            if ( "zip".equals( artifact.getType() ) )
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
                        if ( !minVersionWins || result.get( moduleName ) == null )
                        {
                            result.put( moduleName, artifact );
                            // System.out.println("added module: " + artifact.getGroupId() + ":" +
                            // artifact.getArtifactId());
                        }
                    }
                    else
                    // "module-min"
                    {
                        if ( minVersionWins || result.get( moduleName ) == null )
                        {
                            result.put( moduleName, artifact );
                            // System.out.println("added module-min: " + artifact.getGroupId() + ":" +
                            // artifact.getArtifactId());
                        }
                    }
                }
            }
            else if ( "play".equals( artifact.getType() ) )
            {
                String moduleName = artifact.getArtifactId();
                result.put( moduleName, artifact );
            }
        }
        return result;
    }

    // used by "war" and "war-support" mojos
    protected File filterWebXml( File webXml, File outputDirectory, String applicationName, String playWarId )
        throws IOException
    {
        if ( !outputDirectory.exists() )
        {
            if ( !outputDirectory.mkdirs() )
            {
                throw new IOException( String.format( "Cannot create \"%s\" directory",
                                                      outputDirectory.getCanonicalPath() ) );
            }
        }
        File result = new File( outputDirectory, "filtered-web.xml" );
        BufferedReader reader = createBufferedFileReader( webXml, "UTF-8" );
        try
        {
            BufferedWriter writer = createBufferedFileWriter( result, "UTF-8" );
            try
            {
                getLog().debug( "web.xml file:" );
                String line = reader.readLine();
                while ( line != null )
                {
                    getLog().debug( "  " + line );
                    if ( line.indexOf( "%APPLICATION_NAME%" ) >= 0 )
                    {
                        line =
                            line.replace( "%APPLICATION_NAME%", applicationName/* configParser.getApplicationName() */ );
                    }
                    if ( line.indexOf( "%PLAY_ID%" ) >= 0 )
                    {
                        line = line.replace( "%PLAY_ID%", playWarId );
                    }
                    writer.write( line );
                    writer.newLine();
                    line = reader.readLine();
                }
            }
            finally
            {
                writer.close();
            }
        }
        finally
        {
            reader.close();
        }
        return result;
    }

    protected void createModuleDirectory( File moduleDirectory, boolean overwrite )
        throws IOException
    {
        if ( moduleDirectory.exists() )
        {
            if ( moduleDirectory.isDirectory() )
            {
                if ( overwrite )
                {
                    FileUtils.cleanDirectory( moduleDirectory );
                }
            }
            else
            // file if ( moduleDirectory.isFile() )
            {
                getLog().info( String.format( "Deleting \"%s\" file", moduleDirectory ) ); // TODO-more descriptive message
                if ( !moduleDirectory.delete() )
                {
                    throw new IOException( String.format( "Cannot delete \"%s\" file",
                                                          moduleDirectory.getCanonicalPath() ) );
                }
            }
        }

        if ( !moduleDirectory.exists() )
        {
            if ( !moduleDirectory.mkdirs() )
            {
                throw new IOException( String.format( "Cannot create \"%s\" directory",
                                                      moduleDirectory.getCanonicalPath() ) );
            }
        }
    }

    protected ConfigurationParser getConfiguration( String playId ) throws IOException
    {
        File baseDir = project.getBasedir();
        File confDir = new File( baseDir, "conf" );
        File configurationFile = new File( confDir, "application.conf" );
        ConfigurationParser configParser = new ConfigurationParser( configurationFile, playId );
        configParser.parse();
        
        return configParser;
    }

    protected boolean isFrameworkEmbeddedModule( String moduleName )
    {
        boolean result =
            "crud".equals( moduleName ) || "docviewer".equals( moduleName ) || "grizzly".equals( moduleName )
                || "secure".equals( moduleName ) || "testrunner".equals( moduleName );
        return result;
    }
    
}
