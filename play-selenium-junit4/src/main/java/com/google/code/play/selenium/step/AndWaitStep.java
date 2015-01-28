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

public class AndWaitStep
    extends AbstractSeleniumStep
{

    private VoidSeleniumCommand innerCommand;

    public AndWaitStep( VoidSeleniumCommand innerCommand )
    {
        this.innerCommand = innerCommand;
    }

    protected void doExecute()
        throws Exception
    {
        innerCommand.execute();
        innerCommand.commandProcessor.doCommand( "waitForPageToLoad", new String[] { "30000" } );
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( innerCommand.command ).append( "AndWait('" );
        if ( !"".equals( innerCommand.param1 ) )
        {
            buf.append( innerCommand.param1 );
            if ( !"".equals( innerCommand.param2 ) )
            {
                buf.append( "', '" ).append( innerCommand.param2 );
            }
        }
        buf.append( "')" );
        return buf.toString();
    }

}
