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

import com.thoughtworks.selenium.CommandProcessor;

import com.google.code.play.selenium.StoredVars;

public class BooleanSeleniumCommand
    extends AbstractSeleniumCommand
{

    public BooleanSeleniumCommand( StoredVars storedVars, CommandProcessor commandProcessor, String command,
                                   String param1 )
    {
        super( storedVars, commandProcessor, command, param1 );
    }

    public BooleanSeleniumCommand( StoredVars storedVars, CommandProcessor commandProcessor, String command,
                                   String param1, String param2 )
    {
        super( storedVars, commandProcessor, command, param1, param2 );
    }

    public boolean getBoolean()
        throws Exception
    {
        boolean result = false;

        String xparam1 = param1;
        if ( !"".equals( xparam1 ) )
        {
            xparam1 = storedVars.changeBraces( param1 );
            // not needed here
            // xparam1 = MultiLineHelper.brToNewLine( xparam1 );
        }
        String xparam2 = param2;
        if ( !"".equals( xparam2 ) )
        {
            xparam2 = storedVars.changeBraces( param2 );
            // not needed here
            // xparam2 = MultiLineHelper.brToNewLine( xparam2 );
        }

        if ( !"".equals( param2 ) )
        {
            result = commandProcessor.getBoolean( command, new String[] { xparam1, xparam2 } );
        }
        else if ( !"".equals( param1 ) )
        {
            result = commandProcessor.getBoolean( command, new String[] { xparam1 } );
        }
        else
        {
            result = commandProcessor.getBoolean( command, new String[] {} ); // czy to moze sie zdarzyc?
        }
        return result;
    }

}
