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

public class WaitForNotEqualsStep
    extends AbstractSeleniumStep
{

    private StringSeleniumCommand innerCommand;

    private String expected;

    private Timeout timeout;

    public WaitForNotEqualsStep( StringSeleniumCommand innerCommand, String expected, Timeout timeout )
    {
        this.innerCommand = innerCommand;
        this.expected = expected;
        this.timeout = timeout;
    }

    protected void doExecute()
        throws Exception
    {
        String innerCommandResult = null;
        String xexpected = innerCommand.storedVars.fillValues( expected );
        xexpected = MultiLineHelper.brToNewLine( xexpected );

        boolean success = false;
        long endTimeMillis = System.currentTimeMillis() + timeout.get();

        while ( !success && System.currentTimeMillis() < endTimeMillis )
        {
            innerCommandResult = innerCommand.getString();
            success = EqualsHelper.seleniumNotEquals( xexpected, innerCommandResult );
            /*TEMP if ( !success )
            {
                long remainingWaitTimeMillis = endTimeMillis - System.currentTimeMillis();
                if ( remainingWaitTimeMillis > 0L )
                {
                    long sleepTimeMillis = remainingWaitTimeMillis >= 1000L ? 1000L : remainingWaitTimeMillis;
                    try
                    {
                        Thread.sleep( sleepTimeMillis );
                    }
                    catch ( InterruptedException e )
                    {
                        throw new RuntimeException( e );
                    }
                }
            }*/
        }
        if ( !success )
        {
            String assertMessage = "Actual value \"" + innerCommandResult + "\" did match \"" + xexpected + "\"";
            Verify.fail( assertMessage );
        }
    }

    public String toString()
    {
        String cmd = innerCommand.command.substring( "get".length() );

        StringBuffer buf = new StringBuffer();
        buf.append( "waitForNot" ).append( cmd ).append( "(" );
        if ( !"".equals( innerCommand.param1 ) )
        {
            buf.append( "'" ).append( innerCommand.param1 ).append( "', " );
        }
        buf.append( "'" ).append( expected ).append( "')" );
        return buf.toString();
    }

}
