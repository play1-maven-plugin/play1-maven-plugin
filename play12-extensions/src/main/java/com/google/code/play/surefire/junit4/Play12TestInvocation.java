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

import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

import play.Invoker;

public class Play12TestInvocation
    extends Invoker.DirectInvocation
{
    private Runner runner;
    
    private RunNotifier notifier;

    public Play12TestInvocation( Runner runner, RunNotifier notifier )
    {
        this.runner = runner;
        this.notifier = notifier;
    }

    @Override
    public void execute()
    {
        runner.run( notifier );
    }

    @Override
    public Invoker.InvocationContext getInvocationContext()
    {
        return new Invoker.InvocationContext( invocationType );
    }
}
