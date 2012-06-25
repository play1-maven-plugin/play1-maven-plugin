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

import org.junit.Assert;

import com.google.code.play.selenium.StoredVars;

public class PlayAssertEqualsStep
    extends AbstractSeleniumStep
{

    private StoredVars storedVars;

    private String param1;

    private String param2;

    public PlayAssertEqualsStep( StoredVars storedVars, String param1, String param2 )
    {
        this.storedVars = storedVars;
        this.param1 = param1;
        this.param2 = param2;
    }

    public void doExecute()
        throws Exception
    {
        String param1Filtered = storedVars.fillValues( param1 );
        String param2Filtered = storedVars.fillValues( param2 );

        if ( !param1Filtered.equals( param2Filtered ) )
        {
            String assertMessage = String.format( "%s != %s", param1Filtered, param2Filtered );
            Assert.fail( assertMessage );
        }
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "assertEquals('" ).append( param1 ).append( "', '" ).append( param2 ).append( "')" );
        return buf.toString();
    }

}
