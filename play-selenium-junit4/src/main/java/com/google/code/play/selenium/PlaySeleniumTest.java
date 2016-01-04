/*
 * Copyright 2010-2015 Grzegorz Slowikowski
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

package com.google.code.play.selenium;

import com.thoughtworks.selenium.CommandProcessor;
import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.HttpCommandProcessor;
import com.thoughtworks.selenium.Selenium;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
//import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.google.code.play.selenium.parser.JSoupSeleneseParser;
import com.google.code.play.selenium.step.*;

public abstract class PlaySeleniumTest
{

    private String seleniumUrl = null;

    private CommandProcessor commandProcessor = null;

    /** Use this object to run all of your selenium tests */
    private Selenium selenium;

    private boolean traceTest = false;

    @Before
    public void setUp()
    {
        String traceTestProperty = System.getProperty( "selenium.test.trace", "false" );
        traceTest = ( "true".equals( traceTestProperty ) );

        String seleniumBrowser = System.getProperty( "selenium.browser" );
        if ( seleniumBrowser == null )
        {
            seleniumBrowser = "*chrome";
        }

        seleniumUrl = System.getProperty( "selenium.url" ); //TODO-remove this property
        if ( seleniumUrl == null )
        {
            seleniumUrl = "http://localhost:9000"; //TODO-read protocol and port from "conf/application.conf"
        }

        String seleniumServerHost = System.getProperty( "selenium.server.host" );
        if ( seleniumServerHost == null )
        {
            seleniumServerHost = "localhost";
        }

        int seleniumServerPort = 4444; //TODO-add constant
        String seleniumServerPortStr = System.getProperty( "selenium.server.port" );
        if ( seleniumServerPortStr != null )
        {
            try
            {
                seleniumServerPort = Integer.valueOf( seleniumServerPortStr );
            }
            catch ( NumberFormatException e )
            {
                // ignore?
            }
        }

        commandProcessor =
            new HttpCommandProcessor( seleniumServerHost, seleniumServerPort, seleniumBrowser, seleniumUrl );
        selenium = new DefaultSelenium( commandProcessor );
        selenium.start();
        // There are no cookies by default
        // selenium.deleteCookie("PLAY_SESSION",
        // "path=/,domain=localhost,recurse=true");
        // selenium.deleteCookie("PLAY_ERRORS",
        // "path=/,domain=localhost,recurse=true");
        // selenium.deleteCookie("PLAY_FLASH",
        // "path=/,domain=localhost,recurse=true");

        // store $[space] and $[nbsp] because in Java client they are not predefined
        commandProcessor.doCommand( "storeExpression", new String[] { " ", "space" } );
        commandProcessor.doCommand( "storeExpression", new String[] { "\u00A0", "nbsp" } );
    }

    @After
    public void tearDown()
    {
        if ( selenium != null )
        {
            selenium.stop();
            selenium = null;
        }
    }

    protected void seleniumTest( String testPath )
        throws Exception
    {
        URL testUrl = new URL( seleniumUrl + "/@tests/" + testPath );
        URLConnection conn = testUrl.openConnection();

        // System.out.println( "contentType:" + conn.getContentType() );
        // System.out.println( "contentLength:" + conn.getContentLength() );
        // System.out.println( "Header fields:" );
        // java.util.Map<String, List<String>> headerFields = conn.getHeaderFields();
        // for ( String headerField : headerFields.keySet() )
        // {
        // System.out.println( "  " + headerField + " : " + headerFields.get( headerField ) );
        // }

        if ( "HTTP/1.1 200 OK".equals( conn.getHeaderField( null ) ) )
        {
            InputStream is = (InputStream) conn.getContent();
            try
            {
                String content = readContent( is );
                String contentType = conn.getContentType();
                if ( contentType.startsWith( "text/html" ) )
                {
                    SeleneseParser parser = new JSoupSeleneseParser();
                    List<List<String>> commands = parser.parseSeleneseContent( content );
                    StoredVars storedVars = new StoredVars();
                    List<Step> steps = processContent( commands, storedVars );
                    executeTestSteps( steps );
                }
                else if ( contentType.startsWith( "text/plain" ) )
                {
                    throw new RuntimeException( content );
                }
                else
                {
                    throw new RuntimeException( "Unknown content type: " + contentType );
                }
            }
            finally
            {
                is.close();
            }
        }
        else
        {
            String content = null;
            try
            {
                InputStream is = (InputStream) conn.getContent();
                try
                {
                    content = readContent( is );
                }
                finally
                {
                    is.close();
                }
            }
            catch ( IOException e )
            {
                throw new RuntimeException( "Template rendering error", e );
            }
            // if no exception thrown print what we have (temporary solution):
            // System.out.println( "contentType:" + conn.getContentType() );
            // System.out.println( "contentLength:" + conn.getContentLength() );
            // System.out.println( "Header fields:" );
            // java.util.Map<String, List<String>> headerFields = conn.getHeaderFields();
            // for ( String headerField : headerFields.keySet() )
            // {
            // System.out.println( "  " + headerField + " : " + headerFields.get( headerField ) );
            // }
            // System.out.println( "Content:" );
            // System.out.println( content );
            // System.out.println( "End of content." );
            throw new RuntimeException( content/* "Template rendering error, check Play! server log" */ ); // TODO-add all fields here?
        }
    }

    private String readContent( InputStream is )
        throws IOException
    {
        StringBuffer buf = new StringBuffer();
        InputStreamReader r = new InputStreamReader( is, "UTF-8" );
        BufferedReader br = new BufferedReader( r );
        try
        {
            String line = br.readLine();
            while ( line != null )
            {
                buf.append( line ).append( '\n' );
                line = br.readLine();
            }
        }
        finally
        {
            br.close();
        }
        return buf.toString();
    }

    private List<Step> processContent( List<List<String>> content, StoredVars storedVars )
    {
        // StoredVars storedVars = new StoredVars();
        List<Step> result = new ArrayList<Step>();

        for ( List<String> row : content )
        {
            Step cmd = null;
            if ( row.size() == 1 )
            { // comment
                String cmt = row.get( 0 );
                cmt = cmt.trim();
                cmd = new CommentStep( cmt );
            }
            else
            {
                String command = row.get( 0 );
                String param1 = row.get( 1 );
                // if ( !"".equals( param1 ) )
                // {
                // param1 = xmlUnescape( param1 );
                // }
                String param2 = row.get( 2 );
                // if ( !"".equals( param2 ) )
                // {
                // param2 = xmlUnescape( param2 );
                // }

                if ( "echo".equals( command ) )
                {
                    cmd = new EchoStep( storedVars, param1 );
                }
                else if ( "pause".equals( command ) )
                {
                    cmd = new PauseStep( storedVars, param1 );
                }
                else if ( command.endsWith( "AndWait" ) )
                {
                    String innerCmd = command.substring( 0, command.indexOf( "AndWait" ) );
                    cmd =
                        new AndWaitStep( new VoidSeleniumCommand( storedVars, commandProcessor, innerCmd, param1,
                                                                  param2 ) );
                }
                else if ( command.startsWith( "store" ) )
                {
                    String storeWhat = command.substring( "store".length() );
                    cmd =
                        new StoreStep( storedVars, new VoidSeleniumCommand( storedVars, commandProcessor, command,
                                                                            param1, param2 ),
                                       new StringSeleniumCommand( storedVars, commandProcessor, "get" + storeWhat,
                                                                  param1 ) );
                }
                else if ( command.startsWith( "verify" ) )
                {
                    String verifyWhat = command.substring( "verify".length() );
                    if ( verifyWhat.endsWith( "NotPresent" ) )
                    {
                        String innerCmd = verifyWhat.replace( "NotPresent", "Present" );
                        cmd =
                            new VerifyFalseStep( new BooleanSeleniumCommand( storedVars, commandProcessor, "is"
                                + innerCmd, param1 ) );
                    }
                    else if ( verifyWhat.startsWith( "Not" ) )
                    {
                        String innerCmd = verifyWhat.substring( "Not".length() );
                        if ( "Equals".equals( innerCmd ) )
                        {
                            // Play! extension (see "user-extensions.js" file)
                            cmd = new PlayVerifyNotEqualsStep( storedVars, param1, param2 );
                        }
                        else
                        {
                            // standard Selenium commands
                            if ( isParameterLessCommand( innerCmd ) )
                            {
                                param2 = param1; // value to compare with
                                param1 = ""; // parameterless command
                            }
                            if ( isBooleanCommand( innerCmd ) )
                            {
                                cmd =
                                    new VerifyFalseStep( new BooleanSeleniumCommand( storedVars, commandProcessor, "is"
                                        + innerCmd, param1 ) );
                            }
                            else
                            {
                                StringSeleniumCommand innerCommand =
                                    getInnerStringCommandByName( innerCmd, storedVars, param1 );
                                cmd = new VerifyNotEqualsStep( innerCommand, param2 );
                            }
                        }
                    }
                    else
                    {
                        String innerCmd = verifyWhat;
                        if ( "Equals".equals( innerCmd ) )
                        {
                            // Play! extension (see "user-extensions.js" file)
                            cmd = new PlayVerifyEqualsStep( storedVars, param1, param2 );
                        }
                        else if ( "TextLike".equals( innerCmd ) )
                        {
                            // Play! extension (see "user-extensions.js" file)
                            cmd = new PlayVerifyTextLikeStep( storedVars, param1, param2 );
                        }
                        else if ( "Selected".equals( innerCmd ) )
                        {
                            // deprecated, but still works in Play! Test Runner
                            cmd = new VerifySelectedStep( storedVars, commandProcessor, param1, param2 );
                        }
                        else
                        {
                            // standard Selenium commands
                            if ( isParameterLessCommand( innerCmd ) )
                            {
                                param2 = param1; // value to compare with
                                param1 = ""; // parameterless command
                            }
                            if ( isBooleanCommand( innerCmd ) )
                            {
                                cmd =
                                    new VerifyTrueStep( new BooleanSeleniumCommand( storedVars, commandProcessor, "is"
                                        + innerCmd, param1 ) );
                            }
                            else
                            {
                                StringSeleniumCommand innerCommand =
                                    getInnerStringCommandByName( innerCmd, storedVars, param1 );
                                cmd = new VerifyEqualsStep( innerCommand, param2 );
                            }
                        }
                    }
                }
                else if ( command.startsWith( "assert" ) )
                {
                    String assertWhat = command.substring( "assert".length() );
                    if ( assertWhat.endsWith( "NotPresent" ) )
                    {
                        String innerCmd = "is" + assertWhat.replace( "NotPresent", "Present" );
                        cmd =
                            new AssertFalseStep( new BooleanSeleniumCommand( storedVars, commandProcessor, innerCmd,
                                                                             param1 ) );
                    }
                    else if ( assertWhat.startsWith( "Not" ) )
                    {
                        String innerCmd = assertWhat.substring( "Not".length() );
                        if ( "Equals".equals( innerCmd ) )
                        {
                            // Play! extension (see "user-extensions.js" file)
                            cmd = new PlayAssertNotEqualsStep( storedVars, param1, param2 );
                        }
                        else
                        {
                            // standard Selenium commands
                            if ( isParameterLessCommand( innerCmd ) )
                            {
                                param2 = param1; // value to compare with
                                param1 = ""; // parameterless command
                            }
                            if ( isBooleanCommand( innerCmd ) )
                            {
                                cmd =
                                    new AssertFalseStep( new BooleanSeleniumCommand( storedVars, commandProcessor, "is"
                                        + innerCmd, param1 ) );
                            }
                            else
                            {
                                StringSeleniumCommand innerCommand =
                                    getInnerStringCommandByName( innerCmd, storedVars, param1 );
                                cmd = new AssertNotEqualsStep( innerCommand, param2 );
                            }
                        }
                    }
                    else
                    {
                        String innerCmd = assertWhat;
                        if ( "Equals".equals( innerCmd ) )
                        {
                            // Play! extension (see "user-extensions.js" file)
                            cmd = new PlayAssertEqualsStep( storedVars, param1, param2 );
                        }
                        else if ( "TextLike".equals( innerCmd ) )
                        {
                            // Play! extension (see "user-extensions.js" file)
                            cmd = new PlayAssertTextLikeStep( storedVars, param1, param2 );
                        }
                        else if ( "Selected".equals( innerCmd ) )
                        {
                            // deprecated, but still works in Play! Test Runner
                            cmd = new AssertSelectedStep( storedVars, commandProcessor, param1, param2 );
                        }
                        else
                        {
                            // standard Selenium commands
                            if ( isParameterLessCommand( innerCmd ) )
                            {
                                param2 = param1; // value to compare with
                                param1 = ""; // parameterless command
                            }
                            if ( isBooleanCommand( innerCmd ) )
                            {
                                cmd =
                                    new AssertTrueStep( new BooleanSeleniumCommand( storedVars, commandProcessor, "is"
                                        + innerCmd, param1 ) );
                            }
                            else
                            {
                                StringSeleniumCommand innerCommand =
                                    getInnerStringCommandByName( innerCmd, storedVars, param1 );
                                cmd = new AssertEqualsStep( innerCommand, param2 );
                            }
                        }
                    }
                }
                else if ( command.startsWith( "waitFor" ) )
                {
                    String waitForWhat = command.substring( "waitFor".length() );
                    if ( "Condition".equals( waitForWhat ) || "FrameToLoad".equals( waitForWhat )
                        || "PageToLoad".equals( waitForWhat ) || "PopUp".equals( waitForWhat ) )
                    {
                        cmd =
                            new CommandStep( new VoidSeleniumCommand( storedVars, commandProcessor, command, param1,
                                                                      param2 ) );
                    }
                    else
                    {
                        if ( waitForWhat.endsWith( "NotPresent" ) )
                        {
                            String innerCmd = waitForWhat.replace( "NotPresent", "Present" );
                            cmd =
                                new WaitForFalseStep( new BooleanSeleniumCommand( storedVars, commandProcessor, "is"
                                    + innerCmd, param1 ) );
                        }
                        else if ( waitForWhat.startsWith( "Not" ) )
                        {
                            String innerCmd = waitForWhat.substring( "Not".length() );
                            if ( isParameterLessCommand( innerCmd ) )
                            {
                                param2 = param1; // value to compare with
                                param1 = ""; // parameterless command
                            }
                            if ( isBooleanCommand( innerCmd ) )
                            {
                                cmd =
                                    new WaitForFalseStep( new BooleanSeleniumCommand( storedVars, commandProcessor,
                                                                                      "is" + innerCmd, param1 ) );
                            }
                            else
                            {
                                StringSeleniumCommand innerCommand =
                                    getInnerStringCommandByName( innerCmd, storedVars, param1 );
                                cmd = new WaitForNotEqualsStep( innerCommand, param2 );
                            }
                        }
                        else
                        {
                            String innerCmd = waitForWhat;
                            if ( isParameterLessCommand( innerCmd ) )
                            {
                                param2 = param1; // value to compare with
                                param1 = ""; // parameterless command
                            }
                            if ( isBooleanCommand( innerCmd ) )
                            {
                                cmd =
                                    new WaitForTrueStep( new BooleanSeleniumCommand( storedVars, commandProcessor, "is"
                                        + innerCmd, param1 ) );
                            }
                            else
                            {
                                StringSeleniumCommand innerCommand =
                                    getInnerStringCommandByName( innerCmd, storedVars, param1 );
                                cmd = new WaitForEqualsStep( innerCommand, param2 );
                            }
                        }
                    }
                }
                else
                {
                    cmd =
                        new CommandStep(
                                         new VoidSeleniumCommand( storedVars, commandProcessor, command, param1, param2 ) );
                }
            }
            result.add( cmd );
            // System.out.println( cmd.toString() );
        }

        return result;
    }

    private boolean isBooleanCommand( String command )
    {
        boolean result =
            ( "AlertPresent".equals( command ) || "Checked".equals( command ) || "ConfirmationPresent".equals( command )
                || "CookiePresent".equals( command ) || "Editable".equals( command )
                || "ElementPresent".equals( command ) || "Ordered".equals( command )
                || "PromptPresent".equals( command ) || "SomethingSelected".equals( command )
                || "TextPresent".equals( command ) || "Visible".equals( command ) );
        return result;
    }

    private boolean isParameterLessCommand( String command )
    {
        boolean result =
            ( "Alert".equals( command ) || "BodyText".equals( command ) || "Confirmation".equals( command )
                || "Cookie".equals( command ) || "HtmlSource".equals( command ) || "Location".equals( command )
                || "MouseSpeed".equals( command ) || "Prompt".equals( command ) || "Speed".equals( command ) || "Title".equals( command ) );
        return result;
    }

    private void executeTestSteps( List<Step> steps )
        throws Exception
    {
        StringBuffer testTraceBuf = new StringBuffer();
        StringBuffer verificationFailuresBuf = new StringBuffer();
        int line = 0;
        for ( Step step : steps )
        {
            line++;
            try
            {
                step.execute();
                String logLine = dumpTestStep( line, step );
                if ( traceTest )
                {
                    System.out.println( logLine );
                }
                else
                {
                    testTraceBuf.append( logLine ).append( '\n' );
                }
            }
            catch ( VerificationError e )
            {
                String msg = "verification failure: " + e.getMessage();
                String logLine = dumpTestStepWhenError( line, step, msg );
                if ( traceTest )
                {
                    System.out.println( logLine );
                }
                else
                {
                    testTraceBuf.append( logLine ).append( '\n' );
                }
                verificationFailuresBuf.append( '\n' ).append( e.getMessage() );
            }
            catch ( AssertionError e )
            {
                String msg = "assertion failure: " + e.getMessage();
                String logLine = dumpTestStepWhenError( line, step, msg );
                if ( traceTest )
                {
                    System.out.println( logLine );
                }
                else
                {
                    // buf.append( logLine ).append( '\n' );
                    testTraceBuf.append( logLine );
                    System.out.println( testTraceBuf.toString() );
                }
                throw e;
            }
            catch ( Error e )//TODO-should I handle errors?
            {
                String msg = "error: " + e.getMessage();
                String logLine = dumpTestStepWhenError( line, step, msg );
                if ( traceTest )
                {
                    System.out.println( logLine );
                }
                else
                {
                    // buf.append( logLine ).append( '\n' );
                    testTraceBuf.append( logLine );
                    System.out.println( testTraceBuf.toString() );
                }
                throw e;
            }
            catch ( RuntimeException e )
            {
                String msg = "runtime exception: " + e.getMessage();
                String logLine = dumpTestStepWhenError( line, step, msg );
                if ( traceTest )
                {
                    System.out.println( logLine );
                }
                else
                {
                    // buf.append( logLine ).append( '\n' );
                    testTraceBuf.append( logLine );
                    System.out.println( testTraceBuf.toString() );
                }
                throw e;
            }
        }

        if ( verificationFailuresBuf.length() > 0 )
        {
            if ( !traceTest )
            {
                System.out.println( testTraceBuf.toString() );
            }
            Assert.fail( "There are verification failures:" + verificationFailuresBuf.toString() );
        }
    }

    private String dumpTestStep( int line, Step step )
    {
        long executionTimeMillis = step.getExecutionTimeMillis();

        StringBuilder sb = new StringBuilder();
        sb.append( " " );
        sb.append( String.format( "%4s", line ) );
        sb.append( ": " );
        sb.append( step.toString() );
        if ( executionTimeMillis >= 0 )
        {
            sb.append( " [" );
            sb.append( executionTimeMillis );
            sb.append( "ms]" );
        }

        return sb.toString();
    }

    private String dumpTestStepWhenError( int line, Step step, String errorMessage )
    {
        long executionTimeMillis = step.getExecutionTimeMillis();

        StringBuilder sb = new StringBuilder();
        sb.append( "*" );
        sb.append( String.format( "%4s", line ) );
        sb.append( ": " );
        sb.append( step.toString() );
        if ( executionTimeMillis >= 0 )
        {
            sb.append( " [" );
            sb.append( executionTimeMillis );
            sb.append( "ms]" );
        }
        sb.append( " " );
        sb.append( errorMessage );

        return sb.toString();
    }

    private StringSeleniumCommand getInnerStringCommandByName( String innerCmd, StoredVars storedVars, String param1 )
    {
        StringSeleniumCommand result = null;
        if ( "CacheEntry".equals( innerCmd ) )
        {
            result = new PlayGetCacheEntryCommand( storedVars, commandProcessor, seleniumUrl, param1 );
        }
        else if ( "LastReceivedEmailBy".equals( innerCmd ) )
        {
            result = new PlayGetLastReceivedEmailByCommand( storedVars, commandProcessor, seleniumUrl, param1 );
        }
        else
        {
            result = new StringSeleniumCommand( storedVars, commandProcessor, "get" + innerCmd, param1 );
        }
        return result;
    }

}
