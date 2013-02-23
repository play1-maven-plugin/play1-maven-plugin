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

/*
 * Copyright 2006 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

/**
 * Modified com.thoughtworks.selenium.SeleneseTestBase.seleniumEquals( Object, Object ) method.
 * - "System.out.println( ... )" statements removed
 * - "seleniumNotEquals( String, String )" method added
 */
package com.google.code.play.selenium.step;

import java.util.regex.Pattern;

public class EqualsHelper
{

    private EqualsHelper()
    {
    }

    /**
     * Compares two strings, but handles "regexp:" strings like HTML Selenese
     * 
     * @param expectedPattern
     * @param actual
     * @return true if actual matches the expectedPattern, or false otherwise
     */
    public static boolean seleniumEquals( String expectedPattern, String actual )
    {
        if ( actual.startsWith( "regexp:" ) || actual.startsWith( "regex:" ) || actual.startsWith( "regexpi:" )
            || actual.startsWith( "regexi:" ) )
        {
            // swap 'em
            String tmp = actual;
            actual = expectedPattern;
            expectedPattern = tmp;
        }
        Boolean b;
        b = equalsHandleRegex( "regexp:", expectedPattern, actual, 0 );
        if ( b != null )
        {
            return b.booleanValue();
        }
        b = equalsHandleRegex( "regex:", expectedPattern, actual, 0 );
        if ( b != null )
        {
            return b.booleanValue();
        }
        b = equalsHandleRegex( "regexpi:", expectedPattern, actual, Pattern.CASE_INSENSITIVE );
        if ( b != null )
        {
            return b.booleanValue();
        }
        b = equalsHandleRegex( "regexi:", expectedPattern, actual, Pattern.CASE_INSENSITIVE );
        if ( b != null )
        {
            return b.booleanValue();
        }

        if ( expectedPattern.startsWith( "exact:" ) )
        {
            String expectedExact = expectedPattern.replaceFirst( "exact:", "" );
            if ( !expectedExact.equals( actual ) )
            {
                // System.out.println( "expected " + actual + " to not match " + expectedPattern );
                return false;
            }
            return true;
        }

        String expectedGlob = expectedPattern.replaceFirst( "glob:", "" );
        expectedGlob = expectedGlob.replaceAll( "([\\]\\[\\\\{\\}$\\(\\)\\|\\^\\+.])", "\\\\$1" );

        expectedGlob = expectedGlob.replaceAll( "\\*", ".*" );
        expectedGlob = expectedGlob.replaceAll( "\\?", "." );
        if ( !Pattern.compile( expectedGlob, Pattern.DOTALL ).matcher( actual ).matches() )
        {
            // System.out.println( "expected \"" + actual + "\" to not match glob \"" + expectedPattern
            // + "\" (had transformed the glob into regexp \"" + expectedGlob + "\"" );
            return false;
        }
        return true;
    }

    private static Boolean equalsHandleRegex( String prefix, String expectedPattern, String actual, int flags )
    {
        if ( expectedPattern.startsWith( prefix ) )
        {
            String expectedRegEx = expectedPattern.replaceFirst( prefix, ".*" ) + ".*";
            Pattern p = Pattern.compile( expectedRegEx, flags );
            if ( !p.matcher( actual ).matches() )
            {
                // System.out.println( "expected " + actual + " to not match regexp " + expectedPattern );
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
        return null;
    }

    /**
     * Compares two strings, but handles "regexp:" strings like HTML Selenese
     * 
     * @param expectedPattern
     * @param actual
     * @return true if actual matches the expectedPattern, or false otherwise
     */
    public static boolean seleniumNotEquals( String expectedPattern, String actual )
    {
        if ( actual.startsWith( "regexp:" ) || actual.startsWith( "regex:" ) || actual.startsWith( "regexpi:" )
            || actual.startsWith( "regexi:" ) )
        {
            // swap 'em
            String tmp = actual;
            actual = expectedPattern;
            expectedPattern = tmp;
        }
        Boolean b;
        b = notEqualsHandleRegex( "regexp:", expectedPattern, actual, 0 );
        if ( b != null )
        {
            return b.booleanValue();
        }
        b = notEqualsHandleRegex( "regex:", expectedPattern, actual, 0 );
        if ( b != null )
        {
            return b.booleanValue();
        }
        b = notEqualsHandleRegex( "regexpi:", expectedPattern, actual, Pattern.CASE_INSENSITIVE );
        if ( b != null )
        {
            return b.booleanValue();
        }
        b = notEqualsHandleRegex( "regexi:", expectedPattern, actual, Pattern.CASE_INSENSITIVE );
        if ( b != null )
        {
            return b.booleanValue();
        }

        if ( expectedPattern.startsWith( "exact:" ) )
        {
            String expectedExact = expectedPattern.replaceFirst( "exact:", "" );
            if ( /* ! */expectedExact.equals( actual ) )
            {
                // System.out.println( "expected " + actual + " to not match " + expectedPattern );
                return false;
            }
            return true;
        }

        String expectedGlob = expectedPattern.replaceFirst( "glob:", "" );
        expectedGlob = expectedGlob.replaceAll( "([\\]\\[\\\\{\\}$\\(\\)\\|\\^\\+.])", "\\\\$1" );

        expectedGlob = expectedGlob.replaceAll( "\\*", ".*" );
        expectedGlob = expectedGlob.replaceAll( "\\?", "." );
        if ( /* ! */Pattern.compile( expectedGlob, Pattern.DOTALL ).matcher( actual ).matches() )
        {
            // System.out.println( "expected \"" + actual + "\" to not match glob \"" + expectedPattern
            // + "\" (had transformed the glob into regexp \"" + expectedGlob + "\"" );
            return false;
        }
        return true;
    }

    private static Boolean notEqualsHandleRegex( String prefix, String expectedPattern, String actual, int flags )
    {
        if ( expectedPattern.startsWith( prefix ) )
        {
            String expectedRegEx = expectedPattern.replaceFirst( prefix, ".*" ) + ".*";
            Pattern p = Pattern.compile( expectedRegEx, flags );
            if ( /* ! */p.matcher( actual ).matches() )
            {
                // System.out.println( "expected " + actual + " to not match regexp " + expectedPattern );
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }
        return null;
    }

}
