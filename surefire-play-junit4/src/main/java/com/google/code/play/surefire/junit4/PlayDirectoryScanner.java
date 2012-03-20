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

package com.google.code.play.surefire.junit4;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.surefire.report.ConsoleLogger;
import org.apache.maven.surefire.util.DirectoryScanner;
import org.apache.maven.surefire.util.NestedRuntimeException;
import org.apache.maven.surefire.util.ScannerFilter;
import org.apache.maven.surefire.util.TestsToRun;

/**
 * ...Scans directories looking for tests.
 * Based on Apache Maven Surefire's class DefaultDirectoryScanner
 * (http://svn.apache.org/repos/asf/maven/surefire/tags/surefire-2.11/surefire-api/src/main/java/org/apache/maven/surefire/util/DefaultDirectoryScanner.java)
 */
public class PlayDirectoryScanner
    implements DirectoryScanner
{

    private static final String FS = System.getProperty( "file.separator" );

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    //private static final String JAVA_SOURCE_FILE_EXTENSION = ".java";

    //private static final String JAVA_CLASS_FILE_EXTENSION = ".class";

    private final File basedir;

    private final List<String> includes;

    private final List<String> excludes;

    private final List<Class<?>> classesSkippedByValidation = new ArrayList<Class<?>>();

    private final ConsoleLogger consoleLogger;


    public PlayDirectoryScanner( File basedir, List<String> includes, List<String> excludes, ConsoleLogger consoleLogger )
    {
        this.basedir = basedir;
        this.includes = includes;
        this.excludes = excludes;
        this.consoleLogger = consoleLogger;
    }

    public TestsToRun locateTestClasses( ClassLoader classLoader, ScannerFilter scannerFilter )
    {
        String[] testClassNames = collectTests();
        List<Class<?>> result = new ArrayList<Class<?>>();

        for ( int i = 0; i < testClassNames.length; i++ )
        {
            String className = testClassNames[i];
            Class<?> testClass = loadClass( classLoader, className );
            if ( scannerFilter == null || scannerFilter.accept( testClass ) )
            {
                if ( testClass.getClassLoader() != classLoader )
                {
                    consoleLogger.info( String.format( "WARNING: Test class %s not loaded by Play.classloader. This may cause unexpected problems.\n",
                                                       testClass.getName() ) );
                }
                result.add( testClass );
            }
            else
            {
                classesSkippedByValidation.add( testClass );
            }
        }
        return new TestsToRun( result );
    }

    private /*???static*/ Class<?> loadClass( ClassLoader classLoader, String className )
    {
        Class<?> testClass;
        try
        {
            testClass = classLoader.loadClass( className );
        }
        catch ( ClassNotFoundException e )
        {
            throw new NestedRuntimeException( "Unable to create test class '" + className + "'", e );
        }
        return testClass;
    }

    String[] collectTests()
    {
        String[] tests = EMPTY_STRING_ARRAY;
        if ( basedir.exists() )
        {
            org.codehaus.plexus.util.DirectoryScanner scanner = new org.codehaus.plexus.util.DirectoryScanner();

            scanner.setBasedir( basedir );

            if ( includes != null )
            {
                scanner.setIncludes( processIncludesExcludes( includes ) );
            }

            if ( excludes != null )
            {
                scanner.setExcludes( processIncludesExcludes( excludes ) );
            }

            scanner.scan();

            tests = scanner.getIncludedFiles();
            for ( int i = 0; i < tests.length; i++ )
            {
                String test = tests[i];
                test = test.substring( 0, test.indexOf( "." ) );
                tests[i] = test.replace( FS.charAt( 0 ), '.' );
            }
        }
        return tests;
    }

    private /*???static*/ String[] processIncludesExcludes( List<String> list )
    {
        List<String> newList = new ArrayList<String>();
        Iterator<String> iter = list.iterator();
        while (iter.hasNext())
        {
            String include = iter.next();
            String [] includes = include.split( "," );
            for ( int i = 0; i < includes.length; ++i )
            {
                newList.add( includes[i] );
            }
        }

        String[] incs = new String[newList.size()];

        for ( int i = 0; i < incs.length; i++ )
        {
            String inc = (String) newList.get( i );
            /*if ( inc.endsWith( JAVA_SOURCE_FILE_EXTENSION ) )
            {
                inc =
                    new StringBuffer( inc.length() - JAVA_SOURCE_FILE_EXTENSION.length()
                        + JAVA_CLASS_FILE_EXTENSION.length() ).append( inc.substring( 0,
                                                                                      inc.lastIndexOf( JAVA_SOURCE_FILE_EXTENSION ) ) ).append( JAVA_CLASS_FILE_EXTENSION ).toString();
            }*/
            incs[i] = inc;

        }
        return incs;
    }

    public List<Class<?>> getClassesSkippedByValidation()
    {
        return classesSkippedByValidation;
    }

}
