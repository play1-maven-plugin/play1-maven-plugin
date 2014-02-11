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

package com.google.code.play.selenium.step;

public class Verify
{
    /**
     * Protect constructor since it is a static only class
     */
    protected Verify()
    {
    }

    /**
     * Verifies that a condition is true. If it isn't it throws an {@link VerificationError} with the given message.
     * 
     * @param message the identifying message for the {@link VerificationError} (<code>null</code> okay)
     * @param condition condition to be checked
     */
    public static void verifyTrue( String message, boolean condition )
    {
        if ( !condition )
        {
            fail( message );
        }
    }

    /**
     * Verifies that a condition is true. If it isn't it throws an {@link VerificationError} without a message.
     * 
     * @param condition condition to be checked
     */
    public static void verifyTrue( boolean condition )
    {
        verifyTrue( null, condition );
    }

    /**
     * Verifies that a condition is false. If it isn't it throws an {@link VerificationError} with the given message.
     * 
     * @param message the identifying message for the {@link VerificationError} (<code>null</code> okay)
     * @param condition condition to be checked
     */
    public static void verifyFalse( String message, boolean condition )
    {
        verifyTrue( message, !condition );
    }

    /**
     * Verifies that a condition is false. If it isn't it throws an {@link VerificationError} without a message.
     * 
     * @param condition condition to be checked
     */
    public static void verifyFalse( boolean condition )
    {
        verifyFalse( null, condition );
    }

    /**
     * Fails a test with the given message.
     * 
     * @param message the identifying message for the {@link VerificationError} (<code>null</code> okay)
     * @see VerificationError
     */
    public static void fail( String message )
    {
        throw new VerificationError( message == null ? "" : message );
    }

    /**
     * Fails a test with no message.
     */
    public static void fail()
    {
        fail( null );
    }

}
