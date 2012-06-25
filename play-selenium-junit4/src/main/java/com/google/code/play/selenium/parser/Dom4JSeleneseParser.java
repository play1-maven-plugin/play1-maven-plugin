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

package com.google.code.play.selenium.parser;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import com.google.code.play.selenium.SeleneseParser;

public class Dom4JSeleneseParser
    implements SeleneseParser
{

    public List<List<String>> parseSeleneseContent( String content )
        throws DocumentException
    {
        List<List<String>> result = new ArrayList<List<String>>();

        content = "<root>" + content + "</root>"; // root needed
        Document xmlDoc = DocumentHelper.parseText( content );
        Element root = xmlDoc.getRootElement();
        List<Element> tables = root.elements( "table" );
        for ( Element table: tables )
        {
            Element tbody = table.element( "tbody" );
            List<Element> rows = tbody.elements( "tr" );
            for ( Element row : rows )
            {
                List<Element> data = row.elements( "td" );
                if ( data.size() == 1 )
                { // comment
                    String cmt = data.get( 0 ).getTextTrim();
                    System.out.println( "comment: '" + cmt + "'" );
                    List<String> command = new ArrayList<String>( 1 );
                    command.add( cmt );
                    result.add( command );
                }
                else if ( data.size() == 3 )
                {
                    String cmd = data.get( 0 ).getText();
                    String param1 = getStringValue( data.get( 1 ) );
                    String param2 = getStringValue( data.get( 2 ) );
                    List<String> command = new ArrayList<String>( 1 );
                    command.add( cmd );
                    command.add( param1 );
                    command.add( param2 );
                    result.add( command );
                }
                else
                {
                    throw new RuntimeException( "Something strange" ); // FIXME
                }
            }
        }
        return result;
    }

    private String getStringValue( Element element )
    {
        StringBuffer buf = new StringBuffer();
        for ( int i = 0; i < element.nodeCount(); i++ )
        {
            Node subNode = element.node( i );
            short subNodeType = subNode.getNodeType();
            switch ( subNodeType )
            {
                case Node.TEXT_NODE:
                    buf.append( subNode.getText() );
                    break;
                case Node.ELEMENT_NODE:
                    String name = subNode.getName();
                    if ( "br".equals( name ) )
                    {
                        buf.append( "<br />" );
                    }
                    // else ?????
                    break;
            }
        }
        return buf.toString();
    }
}
