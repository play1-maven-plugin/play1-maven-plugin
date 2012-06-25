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

public class WaitForEqualsStep
    extends AbstractSeleniumStep
{

    private StringSeleniumCommand innerCommand;

    private String expected;

    public WaitForEqualsStep( StringSeleniumCommand innerCommand, String expected )
    {
        this.innerCommand = innerCommand;
        this.expected = expected;
    }

    protected void doExecute()
        throws Exception
    {
        String innerCommandResult = null; // tmp
        String xexpected = innerCommand.storedVars.fillValues( expected );
        xexpected = MultiLineHelper.brToNewLine( xexpected );
        for ( int second = 0;; second++ )
        {
            if ( second >= 60 )
            {
                String assertMessage =
                    "Actual value \"" + innerCommandResult + "\" did not match \"" + xexpected + "\"";
                Assert.fail( assertMessage );
            }
            try
            {
                /* tmpString */innerCommandResult = innerCommand.getString();
                boolean seleniumEqualsResult = EqualsHelper.seleniumEquals( xexpected, innerCommandResult );
                if ( seleniumEqualsResult )
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
        String cmd = innerCommand.command.substring( "get".length() );

        StringBuffer buf = new StringBuffer();
        buf.append( "waitFor" ).append( cmd ).append( "(" );
        if ( !"".equals( innerCommand.param1 ) )
        {
            buf.append( "'" ).append( innerCommand.param1 ).append( "', " );
        }
        buf.append( "'" ).append( expected ).append( "')" );
        return buf.toString();
    }

}
