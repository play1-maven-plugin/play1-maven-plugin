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

package com.google.code.play.selenium.parser;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import com.google.code.play.selenium.SeleneseParser;

public class JSoupSeleneseParser
    implements SeleneseParser
{

    public List<List<String>> parseSeleneseContent( String content )
    {
        List<List<String>> result = new ArrayList<List<String>>();

        Document doc = Jsoup.parseBodyFragment( content );
        Element body = doc.body();
        Elements tables = body.getElementsByTag( "TABLE" );
        for ( Element table : tables )
        {
            Element tbody = findFirstSubNode( table, "TBODY" );
            Elements childNodes = tbody.getElementsByTag( "TR" );
            for ( Element tr : childNodes )
            {
                List<String> command = getCommand( tr );
                result.add( command );
            }
        }
        return result;
    }

    // processing table row
    private List<String> getCommand( Element trNode )
    {
        List<String> result = new ArrayList<String>();
        Elements trChildNodes = trNode.getElementsByTag( "TD" );
        for ( Element trChild : trChildNodes )
        {
            result.add( getTableDataValue( trChild ) );
        }
        if ( result.size() != 1 && result.size() != 3 )
        {
            throw new RuntimeException( "Something strange" ); // FIXME
        }
        return result;
    }

    // processing table data
    private String getTableDataValue( Element tdNode )
    {
        //return tdNode.html();
        StringBuffer buf = new StringBuffer();
        List<Node> childNodes = tdNode.childNodes();
        for ( Node tdChild : childNodes )
        {
            if ( tdChild instanceof TextNode )
            {
                buf.append( ( (TextNode) tdChild ).text() );
            }
            else if ( tdChild instanceof Element )
            {
                Element tdChildElement = (Element) tdChild;
                if ( "br".equals( tdChildElement.tagName() ) )
                {
                    buf.append( "<br />" );
                }
            }
        }
        return buf.toString();
    }

    private Element findFirstSubNode( Element parent, String tagName )
    {
        Element result = null;
        Elements childNodes = parent.getElementsByTag( tagName );
        if ( childNodes != null && !childNodes.isEmpty() )
        {
            result = childNodes.get( 0 );
        }
        return result;
    }

}
