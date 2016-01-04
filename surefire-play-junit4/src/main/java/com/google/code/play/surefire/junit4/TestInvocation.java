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

package com.google.code.play.surefire.junit4;

import java.lang.reflect.Method;

import org.apache.maven.shared.utils.io.SelectorUtils;

import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

import play.Invoker;

public class TestInvocation
    extends Invoker.DirectInvocation
{
    private Class<?> testClass;

    private RunNotifier fNotifier;
    
    private String[] testMethods;

    public TestInvocation( Class<?> testClass, RunNotifier fNotifier, String[] testMethods )
    {
        this.testClass = testClass;
        this.fNotifier = fNotifier;
        this.testMethods = testMethods;
    }

    @Override
    public void execute()
    {
        if ( null != testMethods )
        {
            Method[] methods = testClass.getMethods();
            for ( Method method : methods )
            {
                for ( String testMethod : testMethods )
                {
                    if ( SelectorUtils.match( testMethod, method.getName() ) )
                    {
                        Runner junitTestRunner = Request.method( testClass, method.getName() ).getRunner();
                        junitTestRunner.run( fNotifier );
                    }
                }
            }
            return;
        }

        Runner junitTestRunner = Request.aClass( testClass ).getRunner();
        
        junitTestRunner.run( fNotifier );
    }

    // @Override
    // public Invoker.InvocationContext getInvocationContext() {
    // return new Invoker.InvocationContext(invocationType);
    // }
}
