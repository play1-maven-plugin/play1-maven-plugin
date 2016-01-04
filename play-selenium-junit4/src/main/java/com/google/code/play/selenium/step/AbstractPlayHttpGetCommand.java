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

import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import com.google.code.play.selenium.StoredVars;
import com.thoughtworks.selenium.CommandProcessor;

public abstract class AbstractPlayHttpGetCommand
    extends StringSeleniumCommand
{

    private String applicationRootUrl;

    public AbstractPlayHttpGetCommand( StoredVars storedVars, CommandProcessor commandProcessor, String command,
                                       String param1, String applicationRootUrl )
    {
        super( storedVars, commandProcessor, command, param1 );
        this.applicationRootUrl = applicationRootUrl;
    }

    public String getString()
        throws Exception
    {
        String result = null;

        String commandRelativeUrl = getCommandRelativeUrl();
        String urlStr = String.format( "%s/%s", applicationRootUrl, commandRelativeUrl );
        URL connectUrl = new URL( urlStr );
        URLConnection conn = connectUrl.openConnection();
        Map<String, List<String>> maps = conn.getHeaderFields();
        List<String> l = maps.get( null );
        if ( l.size() > 0 )
        {
            String s0 = l.get( 0 );
            if ( s0.contains( "200 OK" ) )
            {
                int contentLength = conn.getContentLength();
                byte[] content = new byte[contentLength];
                conn.getInputStream().read( content, 0, contentLength );
                // String contentType = conn.getContentType();
                result = new String( content, "UTF-8" ); // TODO - get from contentType
            }
        }
        return result;
    }

    protected abstract String getCommandRelativeUrl();

}
