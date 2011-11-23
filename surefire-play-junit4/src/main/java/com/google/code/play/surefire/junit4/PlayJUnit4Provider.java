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

import org.apache.maven.surefire.common.junit4.JUnit4RunListener;
import org.apache.maven.surefire.common.junit4.JUnit4RunListenerFactory;
import org.apache.maven.surefire.common.junit4.JUnit4TestChecker;
import org.apache.maven.surefire.providerapi.AbstractProvider;
import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ConsoleLogger;
import org.apache.maven.surefire.report.ConsoleOutputCapture;
import org.apache.maven.surefire.report.ConsoleOutputReceiver;
import org.apache.maven.surefire.report.PojoStackTraceWriter;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.ReporterException;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.report.SimpleReportEntry;
import org.apache.maven.surefire.suite.RunResult;
import org.apache.maven.surefire.testset.DirectoryScannerParameters;
import org.apache.maven.surefire.testset.TestSetFailedException;
//import org.apache.maven.surefire.util.DirectoryScanner;
//import org.apache.maven.surefire.util.DefaultDirectoryScanner;
import org.apache.maven.surefire.util.TestsToRun;

import org.junit.runner.Result;
import org.junit.runner.notification.RunNotifier;

import play.Play;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class PlayJUnit4Provider
    extends AbstractProvider
{
    private final ClassLoader testClassLoader;

    private final PlayDirectoryScanner directoryScanner;
    
    private final List<org.junit.runner.notification.RunListener> customRunListeners;

    private final JUnit4TestChecker jUnit4TestChecker;

    private final String requestedTestMethod;

    private TestsToRun testsToRun;
    
    private final String playId;

    private final String playHome;
    
    private final String applicationPath;

    private final ProviderParameters providerParameters;

    private final ConsoleLogger consoleLogger;

    public PlayJUnit4Provider( ProviderParameters booterParameters )
    {
        this.providerParameters = booterParameters;

        Properties providerProperties = providerParameters.getProviderProperties();
        this.playId =
            ( providerProperties.containsKey( "play.testId" ) ? providerProperties.getProperty( "play.testId" ) : "test" );
        this.playHome = providerProperties.getProperty( "play.home" );
        checkPath(this.playHome);
        this.applicationPath = providerProperties.getProperty( "application.path" );
        checkPath(this.applicationPath);

        this.testClassLoader = booterParameters.getTestClassLoader();
        this.consoleLogger = booterParameters.getConsoleLogger();
        DirectoryScannerParameters directoryScannerParameters = booterParameters.getDirectoryScannerParameters();
        this.directoryScanner = new PlayDirectoryScanner(new File(new File(applicationPath), "test"),
                                                         directoryScannerParameters.getIncludes(),
                                                         directoryScannerParameters.getExcludes(),
                                                         directoryScannerParameters.getRunOrder(),
                                                         consoleLogger);
        customRunListeners =
            JUnit4RunListenerFactory.createCustomListeners( booterParameters.getProviderProperties().getProperty( "listener" ) );
        jUnit4TestChecker = new JUnit4TestChecker( testClassLoader );
        requestedTestMethod = booterParameters.getTestRequest().getRequestedTestMethod();
    }

    // TODO-what exception classes should I throw?
    private void checkPath( String path )
    {
        if ( path == null )
        {
            throw new RuntimeException( "Path is null" );
        }
        File file = new File( path );
        if ( !file.exists() )
        {
            throw new RuntimeException( "Path \"" + path + "\" does not exist" );
        }
        if ( !file.isDirectory() )
        {
            throw new RuntimeException( "Path \"" + path + "\" is not a directory" );
        }
    }

    public RunResult invoke( Object forkTestSet )
        throws TestSetFailedException, ReporterException
    {
        consoleLogger.info( "Play! initialization\n" );
        initializePlayEngine();
        try
        {
            if ( testsToRun == null )
            {
                testsToRun = forkTestSet == null ? scanClassPath() : TestsToRun.fromClass( (Class<?>) forkTestSet );
            }

            upgradeCheck();

            final ReporterFactory reporterFactory = providerParameters.getReporterFactory();

            final RunListener reporter = reporterFactory.createReporter();


            ConsoleOutputCapture.startCapture( (ConsoleOutputReceiver) reporter );

            JUnit4RunListener jUnit4TestSetReporter = new JUnit4RunListener( reporter );

            Result result = new Result();
            RunNotifier runNotifer = getRunNotifer( jUnit4TestSetReporter, result, customRunListeners );

            runNotifer.fireTestRunStarted( null );

            for ( Class<?> clazz : testsToRun.getLocatedClasses() )
            {
                executeTestSet( clazz, reporter, runNotifer );
            }

            runNotifer.fireTestRunFinished( result );

            closeRunNotifer( jUnit4TestSetReporter, customRunListeners );

            return reporterFactory.close();
        }
        finally
        {
            consoleLogger.info( "Play! finalization\n" );
            finalizePlayEngine();
        }
    }

    private void initializePlayEngine()
    {
        Play.frameworkPath = new File(playHome);
        Play.init( new File(applicationPath), playId );
        Play.start();
    }

    private void finalizePlayEngine()
    {
        Play.stop();
    }

    private void executeTestSet( Class<?> clazz, RunListener reporter, RunNotifier listeners )
        throws ReporterException, TestSetFailedException
    {
        final ReportEntry report = new SimpleReportEntry( this.getClass().getName(), clazz.getName() );

        reporter.testSetStarting( report );

        try
        {
            PlayJUnit4TestSet.execute( clazz, listeners, this.requestedTestMethod );
        }
        catch ( TestSetFailedException e )
        {
            throw e;
        }
        catch ( Throwable e )
        {
            reporter.testError( new SimpleReportEntry( report.getSourceName(), report.getName(),
                                                       new PojoStackTraceWriter( report.getSourceName(),
                                                                                 report.getName(), e ) ) );
        }
        finally
        {
            reporter.testSetCompleted( report );
        }
    }

    private RunNotifier getRunNotifer( org.junit.runner.notification.RunListener main, Result result, List<org.junit.runner.notification.RunListener> others )
    {
        RunNotifier fNotifier = new RunNotifier();
        fNotifier.addListener( main );
        fNotifier.addListener(  result.createListener() );
        for ( org.junit.runner.notification.RunListener listener : others )
        {
            fNotifier.addListener( listener );
        }
        return fNotifier;
    }

    // I am not entierly sure as to why we do this explicit freeing, it's one of those
    // pieces of code that just seem to linger on in here ;)
    private void closeRunNotifer( org.junit.runner.notification.RunListener main,
                                  List<org.junit.runner.notification.RunListener> others )
    {
        RunNotifier fNotifier = new RunNotifier();
        fNotifier.removeListener( main );
        for ( org.junit.runner.notification.RunListener listener : others )
        {
            fNotifier.removeListener( listener );
        }
    }

    public Iterator<?> getSuites()
    {
        testsToRun = scanClassPath();
        return testsToRun.iterator();
    }

    private TestsToRun scanClassPath()
    {
        return directoryScanner.locateTestClasses( Play.classloader, jUnit4TestChecker );
    }

    private void upgradeCheck()
        throws TestSetFailedException
    {
        if ( isJunit4UpgradeCheck()
            && directoryScanner.getClassesSkippedByValidation().size() > 0 )
        {
            StringBuilder reason = new StringBuilder();
            reason.append( "Updated check failed\n" );
            reason.append( "There are tests that would be run with junit4 / surefire 2.6 but not with [2.7,):\n" );
            // noinspection unchecked
            for ( Class<?> testClass : (List<Class<?>>) directoryScanner.getClassesSkippedByValidation() )
            {
                reason.append( "   " );
                reason.append( testClass.getCanonicalName() );
                reason.append( "\n" );
            }
            throw new TestSetFailedException( reason.toString() );
        }
    }

    private boolean isJunit4UpgradeCheck()
    {
        final String property = System.getProperty( "surefire.junit4.upgradecheck" );
        return property != null;
    }

}
