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

package com.google.code.play.selenium;

import java.util.HashMap;
import java.util.Map;

public class StoredVars
{
    private Map<String, String> storedVars;

    public StoredVars()
    {
        storedVars = new HashMap<String, String>();
        storedVars.put( "space", " " );
        storedVars.put( "nbsp", "\u00A0" );
    }

    public String getVariable( String name )
    {
        String result = storedVars.get( name );
        if ( result == null )
        {
            result = ""; // ???
        }
        return result;
    }

    public void setVariable( String name, String value )
    {
        storedVars.put( name, value );
    }

    public String fillValues( String text )
    {
        String result = text;
        for ( String name : storedVars.keySet() )
        {
            String value = storedVars.get( name );
            if ( value == null )
            {
                value = ""; // ??
            }
            result = result.replace( "$[" + name + "]", value );
        }
        return result;
    }

    public String changeBraces( String text )
    {
        String result = text;
        for ( String name : storedVars.keySet() )
        {
            result = result.replace( "$[" + name + "]", "${" + name + "}" );
        }
        return result;
    }

}
