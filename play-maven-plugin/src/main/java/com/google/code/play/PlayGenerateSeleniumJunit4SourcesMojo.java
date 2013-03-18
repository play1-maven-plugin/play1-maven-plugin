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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Generates JUnit test wrapper sources for Selenium tests. Maven Surefire plugin operates on Java test classes (JUnit
 * or TestNG). Play! application Selenium tests are defined in "*.test.html" files so there is a need for a JUnit (or
 * TestNG) wrapper for every Selenium test.
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @since 1.0.0
 */
@Mojo( name = "generate-selenium-junit4-sources", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES )
public class PlayGenerateSeleniumJunit4SourcesMojo
    extends AbstractPlayMojo
{

    /**
     * Skip generating JUnit test wrapper sources.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.seleniumSkip", defaultValue = "false" )
    private boolean seleniumSkip;

    protected void internalExecute()
        throws MojoExecutionException, MojoFailureException, IOException
    {
        if ( !seleniumSkip )
        {
            File baseDir = project.getBasedir();
            File playTests = new File( baseDir, "test" );
            File destDir = new File( project.getBuild().getDirectory(), "selenium/generated" ); // TODO - maybe parametrize

            int classesGenerated = processTestsInDirectory( playTests, destDir, null );
            if ( classesGenerated == 0 )
            {
                getLog().info( "Nothing to generate - all Selenium JUnit4 test sources are up to date" );
            }
            project.addTestCompileSourceRoot( destDir.getAbsolutePath() );
        }
    }

    protected int processTestsInDirectory( File srcDir, File destDir, String javaPackageName )
        throws MojoExecutionException, MojoFailureException, IOException
    {
        File[] srcFiles = srcDir.listFiles();

        if ( srcFiles == null || srcFiles.length == 0 )
        {
            return 0;
        }

        int classesGenerated = 0;
        for ( File srcFile : srcFiles )
        {
            if ( srcFile.isDirectory() )
            {
                String javaSubPackageName = srcFile.getName();
                if ( javaPackageName != null )
                {
                    javaSubPackageName = javaPackageName + "." + javaSubPackageName;
                }
                classesGenerated +=
                    processTestsInDirectory( srcFile, new File( destDir, srcFile.getName() ), javaSubPackageName );
            }
            else
            {
                String srcFileName = srcFile.getName();
                if ( srcFileName.endsWith( ".test.html" ) )
                {
                    String oryginalTestClassName = srcFileName.substring( 0, srcFileName.indexOf( ".test.html" ) );
                    String javaTestClassName = oryginalTestClassName;
                    javaTestClassName = javaTestClassName.replace( ".", "_" );
                    javaTestClassName = javaTestClassName.replace( "-", "_" );
                    if ( Character.isDigit( javaTestClassName.charAt( 0 ) ) )
                    {
                        javaTestClassName = "_" + javaTestClassName;
                    }
                    File javaTestFile = new File( destDir, javaTestClassName + "SeleniumTest.java" );
                    if ( !javaTestFile.exists() )
                    {
                        if ( !javaTestFile.getParentFile().exists() )
                        {
                            if ( !javaTestFile.getParentFile().mkdirs() )
                            {
                                throw new IOException( String.format( "Cannot create \"%s\" directory",
                                                                      javaTestFile.getParentFile().getCanonicalPath() ) );
                            }
                        }
                        PrintWriter w =
                            new PrintWriter(
                                             new BufferedWriter(
                                                                 new OutputStreamWriter(
                                                                                         new FileOutputStream(
                                                                                                               javaTestFile ),
                                                                                         "UTF-8" ) ) );
                        try
                        {
                            generateTestSource( oryginalTestClassName, javaPackageName, javaTestClassName, w );
                            classesGenerated++;
                        }
                        finally
                        {
                            w.flush(); // ??
                            w.close();
                        }
                    }
                }
            }
        }

        return classesGenerated;
    }

    private void generateTestSource( String oryginalTestClassName, String javaTestPackage, String javaTestClassName,
                                     PrintWriter w )
        throws IOException, MojoExecutionException
    {
        String playTestPath = oryginalTestClassName + ".test.html";
        if ( javaTestPackage != null )
        {
            playTestPath = javaTestPackage.replace( '.', '/' ) + "/" + playTestPath;
        }
        if ( javaTestPackage != null )
        {
            w.println( "package " + javaTestPackage + ";" );
            w.println();
        }
        w.println( "import org.junit.Test;" );
        w.println();
        w.println( "import com.google.code.play.selenium.PlaySeleniumTest;" );
        w.println();
        w.println( "public class " + javaTestClassName + "SeleniumTest extends PlaySeleniumTest {" );
        w.println();
        w.println( "\t@Test" );
        w.println( "\tpublic void test" + javaTestClassName + "() throws Exception {" );
        w.println( "\t\tseleniumTest(\"" + playTestPath + "\");" );
        w.println( "\t}" );
        w.println();
        w.println( "}" );
    }
}

// TODO
// - option to force test sources generation (not generating incrementally)
// - use includes/excludes?
