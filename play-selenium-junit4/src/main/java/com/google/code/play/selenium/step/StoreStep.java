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

public class StoreStep
    extends CommandStep
{

    public StoreStep( VoidSeleniumCommand innerStoreCommand )
    {
        super( innerStoreCommand );
    }

    protected void doExecute()
        throws Exception
    {
        super.doExecute();
        String varName = "".equals( command.param2 ) ? command.param1 : command.param2;
        updateLocalVar( varName );
        //updateLocalVar( command.param1 );
        //updateLocalVar( command.param2 );
    }

    private void updateLocalVar( String varName )
    {
        if ( !"".equals( varName ) )
        {
            String varValue = command.commandProcessor.getString( "getEval", new String[] { "storedVars['" + varName + "']" } );
            if ( varValue != null )
            {
                if ( !"null".equals( varValue ) )
                {
                    command.storedVars.setVariable( varName, varValue );
                }
                else
                {
                    // Variable value is "null" or (more probably) variable undefined.
                    boolean varUndefined =
                        command.commandProcessor.getBoolean( "getEval", new String[] { "storedVars['" + varName
                            + "'] === undefined" } );
                    if ( varUndefined )
                    {
                        command.storedVars.removeVariable( varName );
                    }
                    else
                    {
                        command.storedVars.setVariable( varName, varValue );
                    }
                }
            }
            else
            {
                command.storedVars.removeVariable( varName );
            }
        }
    }

}
