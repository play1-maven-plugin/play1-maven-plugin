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

package com.google.code.play.selenium.step;

import org.junit.Assert;

import com.thoughtworks.selenium.SeleniumException;

public class AssertStep
    extends AbstractSeleniumStep
{

    private VoidSeleniumCommand assertCommand;

    public AssertStep( VoidSeleniumCommand assertCommand )
    {
        this.assertCommand = assertCommand;
    }

    public void doExecute()
        throws Exception
    {
        try
        {
            assertCommand.execute();
        }
        catch ( SeleniumException e )
        {
            String message = e.getMessage();
            if ( message.startsWith( "ERROR: " ) )
            {
                message = message.substring( "ERROR: ".length() );
            }
            Assert.fail( message );
        }
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( assertCommand.command ).append( "('" ).append( assertCommand.param1 ).append( "', '" ).append( assertCommand.param2 ).append( "')" );
        return buf.toString();
    }

}
