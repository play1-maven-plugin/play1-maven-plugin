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

public abstract class AbstractRunnable
    implements Runnable
{
    private boolean executed;

    private Exception exception;

    public void run()
    {
        try
        {
            methodToRun();
            synchronized ( this )
            {
                this.executed = true;
            }
        }
        catch ( Exception e )
        {
            synchronized ( this )
            {
                this.executed = true;
                this.exception = e;
            }
        }
    }

    public abstract void methodToRun();

    public boolean isExecuted()
    {
        boolean result;
        synchronized ( this )
        {
            result = executed;
        }
        return result;
    }

    public Exception getException()
    {
        Exception result = null;
        synchronized ( this )
        {
            result = exception;
        }
        return result;
    }

}
