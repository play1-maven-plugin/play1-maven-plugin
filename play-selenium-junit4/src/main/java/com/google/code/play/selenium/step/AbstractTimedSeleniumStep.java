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

package com.google.code.play.selenium.step;

import com.google.code.play.selenium.Timeout;

import junit.framework.Assert;

public abstract class AbstractTimedSeleniumStep
    extends AbstractSeleniumStep
{

    private Timeout timeout;

    public AbstractTimedSeleniumStep( Timeout timeout )
    {
        this.timeout = timeout;
    }

    protected String getTimeout()
    {
        return timeout.get();
    }

    protected int getTimeoutAsInt()
    {
        String timeoutString = timeout.get();
        try
        {
            return Integer.parseInt( timeoutString );
        }
        catch ( NumberFormatException e )
        {
            String errorMessage = String.format( "Timeout is not a number: '%s'", timeoutString );
            throw new RuntimeException( errorMessage, e );
        }
    }

}
