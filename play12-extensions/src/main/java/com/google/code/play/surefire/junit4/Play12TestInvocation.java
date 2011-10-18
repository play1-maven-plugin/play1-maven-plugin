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

import java.lang.reflect.Method;

import org.apache.maven.surefire.util.internal.StringUtils;

import org.codehaus.plexus.util.SelectorUtils;

import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

import play.Invoker;

public class Play12TestInvocation
    extends Invoker.DirectInvocation
{
    private Class<?> testClass;

    private RunNotifier fNotifier;

    private String testMethod;

    public Play12TestInvocation( Class<?> testClass, RunNotifier fNotifier, String testMethod )
    {
        this.testClass = testClass;
        this.fNotifier = fNotifier;
        this.testMethod = testMethod;
    }

    @Override
    public void execute()
    // throws Exception
    {
        if ( !StringUtils.isBlank( testMethod ) )
        {
            Method[] methods = testClass.getMethods();
            for ( int i = 0, size = methods.length; i < size; i++ )
            {
                if ( SelectorUtils.match( testMethod, methods[i].getName() ) )
                {
                    Runner junitTestRunner = Request.method( testClass, methods[i].getName() ).getRunner();
                    junitTestRunner.run( fNotifier );
                }
            }
            return;
        }

        Runner junitTestRunner = Request.aClass( testClass ).getRunner();

        junitTestRunner.run( fNotifier );
        /*
         * try { Runner junitTestRunner = Request.aClass( testClass ).getRunner(); junitTestRunner.run( fNotifier ); }
         * catch ( Throwable e ) { throw new RuntimeException( e ); }
         */
    }

    @Override
    public Invoker.InvocationContext getInvocationContext()
    {
        return new Invoker.InvocationContext( invocationType );
    }
}
