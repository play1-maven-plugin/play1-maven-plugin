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

package com.google.code.play.selenium.step;

import junit.framework.Assert;

public class WaitForFalseStep
    extends AbstractSeleniumStep
{

    private BooleanSeleniumCommand innerCommand;

    public WaitForFalseStep( BooleanSeleniumCommand innerCommand )
    {
        this.innerCommand = innerCommand;
    }

    protected void doExecute()
        throws Exception
    {
        for ( int second = 0;; second++ )
        {
            if ( second >= 60 )
            {
                String assertMessage = null;
                String cmd = innerCommand.command.substring( "is".length() );
                if ( cmd.endsWith( "Present" ) )
                {
                    assertMessage =
                        cmd.replace( "Present", ( !"".equals( innerCommand.param1 ) ? " '" + innerCommand.param1 + "'"
                                        : "" ) + " present" );
                }
                else
                {
                    assertMessage = "'" + innerCommand.param1 + "' " + cmd; // in this case the parameters is always not
                                                                            // empty
                }
                Assert.fail( assertMessage );
            }
            try
            {
                boolean innerCommandResult = !innerCommand.getBoolean();
                if ( innerCommandResult )
                {
                    break;
                }
            }
            catch ( Exception e )
            {
            }
            Thread.sleep( 1000 );
        }
    }

    public String toString()
    {
        String cmd = innerCommand.command.substring( "is".length() );

        StringBuffer buf = new StringBuffer();
        buf.append( "waitFor" );
        if ( cmd.endsWith( "Present" ) )
        {
            buf.append( cmd.replace( "Present", "NotPresent" ) );
        }
        else
        {
            buf.append( "Not" ).append( cmd );
        }
        buf.append( "('" ).append( innerCommand.param1 ).append( "')" );
        return buf.toString();
    }

}
