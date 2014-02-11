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

package com.google.code.play;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Play&#33; configuration file ("conf/application.conf") reader.
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 */
public class ConfigurationParser
{
    public static final String configurationFileName = "application.conf";

    private String playId;

    private File applicationDirectory;

    private File playDirectory;

    private Properties properties;

    public ConfigurationParser( String playId, File applicationDirectory, File playDirectory )
    {
        this.playId = playId;
        this.applicationDirectory = applicationDirectory;
        this.playDirectory = playDirectory;

        this.properties = null;
    }

    public String getProperty( String key )
    {
        return properties.getProperty( key );
    }

    public String getProperty( String key, String defaultValue )
    {
        return properties.getProperty( key, defaultValue );
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
            String strKey = key.toString();
            if ( strKey.startsWith( "module." ) )
            {
                String moduleName = strKey.substring( 7 );
                String modulePath = properties.getProperty( strKey );
                modules.put( moduleName, modulePath );
            }
        }
        return modules;
    }

    public void parse()
        throws IOException
    {
        Set<File> confs = new HashSet<File>( 1 );
        this.properties = readOneConfigurationFile( configurationFileName, confs );
    }

    private Properties readOneConfigurationFile( String configurationFileName, Set<File> confs )
        throws IOException
    {
        File confDir = new File( applicationDirectory, "conf" );
        File configurationFile = new File( confDir, configurationFileName );
        if ( !configurationFile.exists() )
        {
            throw new IOException( String.format( "Configuration reader - \"%s\" file does not exist.",
                                                  configurationFile.getName() ) );
        }
        if ( !configurationFile.isFile() )
        {
            throw new IOException( String.format( "Configuration reader - \"%s\" is not a file.",
                                                  configurationFile.getName() ) );
        }
        if ( confs.contains( configurationFile ) )
        {
            throw new IOException(
                                   String.format( "Configuration reader - detected recursive @include usage. Have seen the \"%s\" file before.",
                                                  configurationFile.getName() ) );
        }

        Properties propsFromFile = new OrderSafeProperties();
        FileInputStream fis = new FileInputStream( configurationFile );
        try
        {
            propsFromFile.load( fis );
        }
        finally
        {
            fis.close();
        }
        confs.add( configurationFile );

        // OK, check for instance specifics configuration
        Properties newConfiguration = new OrderSafeProperties();
        Pattern pattern = Pattern.compile( "^%([a-zA-Z0-9_\\-]+)\\.(.*)$" );
        for ( Object key : propsFromFile.keySet() )
        {
            Matcher matcher = pattern.matcher( key + "" );
            if ( !matcher.matches() )
            {
                newConfiguration.put( key, propsFromFile.get( key ).toString().trim() );
            }
        }
        for ( Object key : propsFromFile.keySet() )
        {
            Matcher matcher = pattern.matcher( key + "" );
            if ( matcher.matches() )
            {
                String instance = matcher.group( 1 );
                if ( instance.equals( this.playId ) )
                {
                    newConfiguration.put( matcher.group( 2 ), propsFromFile.get( key ).toString().trim() );
                }
            }
        }
        propsFromFile = newConfiguration;
        // Resolve ${..}
        pattern = Pattern.compile( "\\$\\{([^}]+)}" );
        for ( Object key : propsFromFile.keySet() )
        {
            String value = propsFromFile.getProperty( key.toString() );
            Matcher matcher = pattern.matcher( value );
            StringBuffer newValue = new StringBuffer( 100 );
            while ( matcher.find() )
            {
                String jp = matcher.group( 1 );
                String r = null;
                if ( jp.equals( "play.id" ) )
                {
                    r = this.playId != null ? this.playId : "";
                }
                else if ( jp.equals( "application.path" ) )
                {
                    r = this.applicationDirectory.getAbsolutePath();
                }
                else if ( jp.equals( "play.path" ) )
                {
                    r = this.playDirectory.getAbsolutePath();
                }
                else
                {
                    r = System.getProperty( jp );
                }
                if ( r == null )
                {
                    //??? tLogger.warn("Cannot replace %s in configuration (%s=%s)", jp, key, value);
                    continue;
                }
                matcher.appendReplacement( newValue, r.replaceAll( "\\\\", "\\\\\\\\" ) );
            }
            matcher.appendTail( newValue );
            propsFromFile.setProperty( key.toString(), newValue.toString() );
        }
        // Include
        Map<Object, Object> toInclude = new HashMap<Object, Object>( 16 );
        for ( Object key : propsFromFile.keySet() )
        {
            if ( key.toString().startsWith( "@include." ) )
            {
                String filenameToInclude = propsFromFile.getProperty( key.toString() );
                toInclude.putAll( readOneConfigurationFile( filenameToInclude, confs ) );
            }
        }
        propsFromFile.putAll( toInclude );

        return propsFromFile;
    }

}
