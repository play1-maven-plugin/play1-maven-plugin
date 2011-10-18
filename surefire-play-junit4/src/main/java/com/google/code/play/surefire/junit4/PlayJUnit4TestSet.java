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

import java.lang.reflect.Constructor;

import org.apache.maven.surefire.testset.TestSetFailedException;
import org.junit.runner.notification.RunNotifier;

import play.Invoker;
import play.Play;

public class PlayJUnit4TestSet
{

    public static void execute( Class<?> testClass, RunNotifier fNotifier, String testMethod )
        throws TestSetFailedException
    {
        try
        {
            String invocationClassName = "com.google.code.play.surefire.junit4.TestInvocation";
            if ( "1.2".compareTo( Play.version ) <= 0 )
            {
                invocationClassName = "com.google.code.play.surefire.junit4.Play12TestInvocation";
            }
            Invoker.DirectInvocation invocation = getInvocation( invocationClassName, testClass, fNotifier, testMethod );
            Invoker.invokeInThread( invocation );
        }
        catch ( Throwable e )
        {
            throw new TestSetFailedException( e );
            // ????? throw ExceptionUtils.getRootCause(e);
        }
    }

    public static Invoker.DirectInvocation getInvocation( String invocationClassName, Class<?> testClass,
                                                          RunNotifier fNotifier, String testMethod )
        throws Throwable
    {
        Invoker.DirectInvocation invocation = null;
        Class<?> cl = Class.forName( invocationClassName );
        Constructor<?> c = cl.getConstructor( Class.class, RunNotifier.class, String.class );
        invocation = (Invoker.DirectInvocation) c.newInstance( testClass, fNotifier, testMethod );
        return invocation;
    }
}
