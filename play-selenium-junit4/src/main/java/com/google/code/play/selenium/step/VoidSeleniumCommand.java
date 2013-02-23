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

package com.google.code.play.selenium.step;

import com.thoughtworks.selenium.CommandProcessor;

import com.google.code.play.selenium.StoredVars;

public class VoidSeleniumCommand
    extends AbstractSeleniumCommand
{

    public VoidSeleniumCommand( StoredVars storedVars, CommandProcessor commandProcessor, String command, String param1 )
    {
        super( storedVars, commandProcessor, command, param1 );
    }

    public VoidSeleniumCommand( StoredVars storedVars, CommandProcessor commandProcessor, String command,
                                String param1, String param2 )
    {
        super( storedVars, commandProcessor, command, param1, param2 );
    }

    public void execute()
        throws Exception
    {
        String xparam1 = param1;
        if ( !"".equals( param1 ) )
        {
            xparam1 = storedVars.changeBraces( param1 );
            xparam1 = MultiLineHelper.brToNewLine( xparam1 );
        }
        String xparam2 = param2;
        if ( !"".equals( param2 ) )
        {
            xparam2 = storedVars.changeBraces( param2 );
            xparam2 = MultiLineHelper.brToNewLine( xparam2 );
        }

        if ( !"".equals( param2 ) )
        {
            commandProcessor.doCommand( command, new String[] { xparam1, xparam2 } );
        }
        else if ( !"".equals( param1 ) )
        {
            commandProcessor.doCommand( command, new String[] { xparam1 } );
        }
        else
        {
            commandProcessor.doCommand( command, new String[] {} ); // czy ro moze sie zdarzyc?
        }
    }

}
