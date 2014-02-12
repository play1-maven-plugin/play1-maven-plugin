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
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.RunOrderCalculator;
import org.apache.maven.surefire.util.TestsToRun;
import org.apache.maven.surefire.util.internal.StringUtils;

import org.junit.runner.Result;
import org.junit.runner.notification.RunNotifier;

import play.Invoker;
import play.Play;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class PlayJUnit4Provider
    extends AbstractProvider
{
    private final ClassLoader testClassLoader;

    private final List<org.junit.runner.notification.RunListener> customRunListeners;

    private final JUnit4TestChecker jUnit4TestChecker;

    private final String requestedTestMethod;

    private TestsToRun testsToRun;
    
    private final ProviderParameters providerParameters;

    private final RunOrderCalculator runOrderCalculator;

    private final PlayScanResult scanResult;

    private final ConsoleLogger consoleLogger;

    public PlayJUnit4Provider( ProviderParameters booterParameters )
    {
        this.providerParameters = booterParameters;
        this.testClassLoader = booterParameters.getTestClassLoader();
        this.consoleLogger = booterParameters.getConsoleLogger();
        this.scanResult = PlayScanResult.from( booterParameters.getProviderProperties(), consoleLogger );
        this.runOrderCalculator = booterParameters.getRunOrderCalculator();
        customRunListeners =
            JUnit4RunListenerFactory.createCustomListeners( booterParameters.getProviderProperties().getProperty( "listener" ) );
        jUnit4TestChecker = new JUnit4TestChecker( testClassLoader );
        requestedTestMethod = booterParameters.getTestRequest().getRequestedTestMethod();
    }

    private String getProviderProperty( String key, String defaultValue )
    {
        Properties providerProperties = providerParameters.getProviderProperties();
        return providerProperties.getProperty( key, defaultValue );
    }

    private File getApplicationPath()
        throws TestSetFailedException
    {
        return checkPath( System.getProperty( "user.dir" ) );
    }

    // Copy of AbstractPlayMojo.getPlayHome() method (with getCanonicalPath() changed to getAbsolutePath() )
    private File getPlayHome( File applicationPath )
        throws TestSetFailedException
    {
        File targetDir = new File( applicationPath, "target" );
        File playTmpDir = new File( targetDir, "play" );
        File playTmpHomeDir = new File( playTmpDir, "home" );
        if ( !playTmpHomeDir.exists() )
        {
            throw new TestSetFailedException( String.format( "Play! home directory \"%s\" does not exist",
                                                       playTmpHomeDir.getAbsolutePath() ) );
        }
        if ( !playTmpHomeDir.isDirectory() )
        {
            throw new TestSetFailedException( String.format( "Play! home directory \"%s\" is not a directory",
                                                       playTmpHomeDir.getAbsolutePath() ) );
        }
        // Additional check whether the temporary Play! home directory is created by this plugin
        File warningFile = new File( playTmpHomeDir, "WARNING.txt" );
        if ( warningFile.exists() )
        {
            if ( !warningFile.isFile() )
            {
                throw new TestSetFailedException( String.format( "Play! home directory warning file \"%s\" is not a file",
                                                           warningFile.getAbsolutePath() ) );
            }
        }
        else
        {
            throw new TestSetFailedException( String.format( "Play! home directory warning file \"%s\" does not exist",
                                                       warningFile.getAbsolutePath() ) );
        }
        return playTmpHomeDir;
    }

    private File checkPath( String path ) throws TestSetFailedException
    {
        if ( path == null )
        {
            throw new TestSetFailedException( "Path is null" );
        }
        File file = new File( path );
        if ( !file.exists() )
        {
            throw new TestSetFailedException( "Path \"" + path + "\" does not exist" );
        }
        if ( !file.isDirectory() )
        {
            throw new TestSetFailedException( "Path \"" + path + "\" is not a directory" );
        }
        return file;
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
                if ( forkTestSet instanceof TestsToRun )
                {
                    testsToRun = (TestsToRun) forkTestSet;
                }
                else if ( forkTestSet instanceof Class )
                {
                    testsToRun = TestsToRun.fromClass( (Class<?>) forkTestSet );
                }
                else
                {
                    testsToRun = scanClassPath();
                }
            }

            upgradeCheck();

            final ReporterFactory reporterFactory = providerParameters.getReporterFactory();

            final RunListener reporter = reporterFactory.createReporter();

            ConsoleOutputCapture.startCapture( (ConsoleOutputReceiver) reporter );

            JUnit4RunListener jUnit4TestSetReporter = new JUnit4RunListener( reporter );

            Result result = new Result();
            RunNotifier runNotifer = getRunNotifer( jUnit4TestSetReporter, result, customRunListeners );

            runNotifer.fireTestRunStarted( null );

            for ( Class<?> aTestsToRun : testsToRun )
            {
                executeTestSet( aTestsToRun, reporter, runNotifer );
            }

            runNotifer.fireTestRunFinished( result );

            JUnit4RunListener.rethrowAnyTestMechanismFailures( result );

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
        throws TestSetFailedException
    {
        File applicationPath = getApplicationPath();
        File playHome = getPlayHome( applicationPath );
        String playId = getProviderProperty( "play.testId", "test" );
        int playStartTimeout = Integer.parseInt( getProviderProperty( "play.startTimeout", "0" ) );
        runRunnable( new PlayStartRunnable( playHome, applicationPath, playId ), "Play! initialization",
                       playStartTimeout );
    }

    private void finalizePlayEngine()
        throws TestSetFailedException
    {
        int playStopTimeout = Integer.parseInt( getProviderProperty( "play.stopTimeout", "0" ) );
        runRunnable( new PlayStopRunnable(), "Play! finalization", playStopTimeout );
    }

    private void runRunnable( AbstractRunnable runnable, String threadName, int timeout )
        throws TestSetFailedException
    {
        if ( timeout <= 0 )
        {
            try
            {
                runnable.methodToRun();
            }
            catch ( Exception e )
            {
                throw new TestSetFailedException( threadName + " error", e );
            }
        }
        else
        {
            Thread t = new Thread( runnable, threadName );
            t.setDaemon( true );
            t.start();
            try
            {
                t.join( timeout );
            }
            catch ( InterruptedException e )
            {
                t.interrupt();
                throw new TestSetFailedException( threadName + " interrupted", e );
            }
            Exception runnerException = runnable.getException();
            if ( runnerException != null )
            {
                // If there is an exception, the thread is not alive anymore t.interrupt();
                throw new TestSetFailedException( threadName + " error", runnerException );
            }
            if ( !runnable.isExecuted() ) // Thread still alive
            {
                t.interrupt();
                throw new TestSetFailedException( threadName + " timed out" );
            }
        }
    }

    private void executeTestSet( Class<?> clazz, RunListener reporter, RunNotifier listeners )
        throws ReporterException, TestSetFailedException
    {
        final ReportEntry report = new SimpleReportEntry( this.getClass().getName(), clazz.getName() );

        reporter.testSetStarting( report );

        try
        {
            if ( !StringUtils.isBlank( this.requestedTestMethod ) )
            {
                String actualTestMethod = getMethod( clazz, this.requestedTestMethod ); //add by rainLee
                String[] testMethods = StringUtils.split( actualTestMethod, "+" );
                execute( clazz, listeners, testMethods );
            }
            else
            { //the original way
                execute( clazz, listeners, null );
            }
        }
        catch ( TestSetFailedException e )
        {
            throw e;
        }
        catch ( Throwable e )
        {
            reporter.testError( SimpleReportEntry.withException( report.getSourceName(), report.getName(),
                                                                 new PojoStackTraceWriter( report.getSourceName(),
                                                                                           report.getName(), e ) ) );
        }
        finally
        {
            reporter.testSetCompleted( report );
        }
    }

    private RunNotifier getRunNotifer( org.junit.runner.notification.RunListener main, Result result,
                                       List<org.junit.runner.notification.RunListener> others )
    {
        RunNotifier fNotifier = new RunNotifier();
        fNotifier.addListener( main );
        fNotifier.addListener( result.createListener() );
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
        final TestsToRun scannedClasses = scanResult.applyFilter( jUnit4TestChecker, Play.classloader/*testClassLoader*/ );
        return runOrderCalculator.orderTestClasses( scannedClasses );
    }

    private void upgradeCheck()
        throws TestSetFailedException
    {
        if ( isJunit4UpgradeCheck() )
        {
            List<Class<?>> classesSkippedByValidation =
                scanResult.getClassesSkippedByValidation( jUnit4TestChecker, testClassLoader );
            if ( !classesSkippedByValidation.isEmpty() )
            {
                StringBuilder reason = new StringBuilder();
                reason.append( "Updated check failed\n" );
                reason.append( "There are tests that would be run with junit4 / surefire 2.6 but not with [2.7,):\n" );
                for ( Class<?> testClass : classesSkippedByValidation )
                {
                    reason.append( "   " );
                    reason.append( testClass.getName() );
                    reason.append( "\n" );
                }
                throw new TestSetFailedException( reason.toString() );
            }
        }
    }

    private boolean isJunit4UpgradeCheck()
    {
        final String property = System.getProperty( "surefire.junit4.upgradecheck" );
        return property != null;
    }

    public static void execute( Class<?> testClass, RunNotifier fNotifier, String[] testMethods )
        throws TestSetFailedException
    {
        try
        {
            String invocationClassName = "com.google.code.play.surefire.junit4.TestInvocation";
            if ( "1.2".compareTo( Play.version ) <= 0 )
            {
                invocationClassName = "com.google.code.play.surefire.junit4.Play12TestInvocation";
            }
            Invoker.DirectInvocation invocation =
                getInvocation( invocationClassName, testClass, fNotifier, testMethods );
            Invoker.invokeInThread( invocation );
        }
        catch ( Throwable e )
        {
            throw new TestSetFailedException( e );
            // ????? throw ExceptionUtils.getRootCause(e);
        }
    }


    /**
     * this method retrive  testMethods from String like "com.xx.ImmutablePairTest#testBasic,com.xx.StopWatchTest#testLang315+testStopWatchSimpleGet"
     * <br>
     * and we need to think about cases that 2 or more method in 1 class. we should choose the correct method
     *
     * @param testClass     the testclass
     * @param testMethodStr the test method string
     * @return a string ;)
     */
    private static String getMethod( Class<?> testClass, String testMethodStr )
    {
        String className = testClass.getName();

        if ( !testMethodStr.contains( "#" ) && !testMethodStr.contains( "," ) )
        { //the original way
            return testMethodStr;
        }
        testMethodStr += ","; //for the bellow  split code
        int beginIndex = testMethodStr.indexOf( className );
        int endIndex = testMethodStr.indexOf( ",", beginIndex );
        String classMethodStr =
            testMethodStr.substring( beginIndex, endIndex ); //String like "StopWatchTest#testLang315"

        int index = classMethodStr.indexOf( '#' );
        if ( index >= 0 )
        {
            return classMethodStr.substring( index + 1, classMethodStr.length() );
        }
        return null;
    }

    public static Invoker.DirectInvocation getInvocation( String invocationClassName, Class<?> testClass,
                                                          RunNotifier fNotifier, String[] testMethods )
        throws Throwable
    {
        Invoker.DirectInvocation invocation = null;
        Class<?> cl = Class.forName( invocationClassName );
        Constructor<?> c = cl.getConstructor( Class.class, RunNotifier.class, String[].class );
        invocation = (Invoker.DirectInvocation) c.newInstance( testClass, fNotifier, testMethods );
        return invocation;
    }

}
