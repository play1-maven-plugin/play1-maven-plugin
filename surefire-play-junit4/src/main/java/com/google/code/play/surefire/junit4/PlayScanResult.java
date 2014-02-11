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

package com.google.code.play.surefire.junit4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.maven.surefire.report.ConsoleLogger;
import org.apache.maven.surefire.util.NestedRuntimeException;
import org.apache.maven.surefire.util.ScanResult;
import org.apache.maven.surefire.util.ScannerFilter;
import org.apache.maven.surefire.util.TestsToRun;

public class PlayScanResult
    implements ScanResult
{
    private final List<String> files;

    private static final String scanResultNo = "tc.";

    private final ConsoleLogger consoleLogger;

    public PlayScanResult( List<String> files, ConsoleLogger consoleLogger )
    {
        this.files = Collections.unmodifiableList( files );
        this.consoleLogger = consoleLogger;
    }

    public int size()
    {
        return files.size();
    }

    public String getClassName( int index )
    {
        return files.get( index );
    }

    public void writeTo( Properties properties )
    {
        int size = files.size();
        for ( int i = 0; i < size; i++ )
        {
            properties.setProperty( scanResultNo + i, files.get( i ) );
        }
    }

    public static PlayScanResult from( Properties properties, ConsoleLogger consoleLogger )
    {
        List<String> result = new ArrayList<String>();
        int i = 0;
        while ( true )
        {
            String item = properties.getProperty( scanResultNo + ( i++ ) );
            if ( item == null )
            {
                return new PlayScanResult( result, consoleLogger );
            }
            result.add( item );
        }
    }

    public boolean isEmpty()
    {
        return files.isEmpty();
    }

    public List<String> getFiles()
    {
        return files;
    }

    public TestsToRun applyFilter( ScannerFilter scannerFilter, ClassLoader testClassLoader )
    {
        List<Class> result = new ArrayList<Class>();

        int size = size();
        for ( int i = 0; i < size; i++ )
        {
            String className = getClassName( i );

            Class<?> testClass = loadClass( testClassLoader, className );

            if ( scannerFilter == null || scannerFilter.accept( testClass ) )
            {
                if ( testClass.getClassLoader() != testClassLoader )
                {
                    consoleLogger.info( String.format( "WARNING: Test class %s not loaded by Play.classloader. This may cause unexpected problems.%n",
                                                       testClass.getName() ) );
                }
                result.add( testClass );
            }
        }

        return new TestsToRun( result );
    }

    public List<Class<?>> getClassesSkippedByValidation( ScannerFilter scannerFilter, ClassLoader testClassLoader )
    {
        List<Class<?>> result = new ArrayList<Class<?>>();

        int size = size();
        for ( int i = 0; i < size; i++ )
        {
            String className = getClassName( i );

            Class<?> testClass = loadClass( testClassLoader, className );

            if ( scannerFilter != null && !scannerFilter.accept( testClass ) )
            {
                result.add( testClass );
            }
        }

        return result;
    }

    private static Class<?> loadClass( ClassLoader classLoader, String className )
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

    public PlayScanResult append( PlayScanResult other )
    {
        if ( other != null )
        {
            List<String> src = new ArrayList<String>( files );
            src.addAll( other.files );
            return new PlayScanResult( src, consoleLogger );
        }
        else
        {
            return this;
        }
    }


}
