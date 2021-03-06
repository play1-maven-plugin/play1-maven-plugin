/*
 * Copyright 2010-2016 Grzegorz Slowikowski
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

import com.google.code.play.selenium.StoredVars;

public class PauseStep
    extends AbstractSeleniumStep
{

    private StoredVars storedVars;

    private String param;
                    
    public PauseStep( StoredVars storedVars, String param )
    {
        this.storedVars = storedVars;
        this.param = param;
    }

    public void doExecute()
        throws Exception
    {
        long pauseTimeMillis = 0L;
        if ( !"".equals( param ) )
        {
            String millisStr = storedVars.fillValues( param );
            pauseTimeMillis = Long.parseLong( millisStr );
            if ( pauseTimeMillis > 0L )
            {
                try
                {
                    Thread.sleep( pauseTimeMillis );
                }
                catch ( InterruptedException e )
                {
                    throw new RuntimeException( e );
                }
            }
        }
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "pause('" ).append( param ).append( "')" );
        return buf.toString();
    }

}
