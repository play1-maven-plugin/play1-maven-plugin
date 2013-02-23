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

package com.google.code.play;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConfigurationParser
{

    private File configurationFile;

    private String playId;

    private Properties properties;

    public ConfigurationParser( File configurationFile, String playId )
    {
        this.configurationFile = configurationFile;
        this.playId = playId;
        this.properties = null;
    }

    public String getProperty( String key )
    {
        Object value = null;
        if ( playId != null && !"".equals( playId ) )
        {
            value = properties.get( "%" + playId + "." + key );
        }
        if ( value == null )
        {
            value = properties.get( key );
        }
        return (String) value;
    }

    public String getProperty( String key, String defaultValue )
    {
        String result = getProperty( key );
        if ( result == null )
        {
            result = defaultValue;
        }
        return result;
    }

    public String getApplicationName()
    {
        return getProperty( "application.name" );
    }

    public Map<String, String> getModules()
    {
        Map<String, String> modules = new HashMap<String, String>();
        for ( Object key : properties.keySet() )
        {
            String strKey = (String) key;
            if ( strKey.startsWith( "module." ) )
            {
                String moduleName = strKey.substring( 7 );
                String modulePath = (String) properties.get( key );
                modules.put( moduleName, modulePath );
            }
        }
        // optimize?
        for ( Object key : properties.keySet() )
        {
            String strKey = (String) key;
            if ( strKey.startsWith( "%" + playId + ".module." ) )
            {
                String moduleName = strKey.substring( 7 + playId.length() + 2 );
                String modulePath = (String) properties.get( key );
                modules.put( moduleName, modulePath );
            }
        }
        return modules;
    }

    public void parse()
        throws IOException
    {
        InputStream inputStream = new BufferedInputStream( new FileInputStream( configurationFile ) );
        try
        {
            Properties props = new Properties();
            props.load( inputStream );
            this.properties = props;
        }
        finally
        {
            inputStream.close();
        }
    }

}
