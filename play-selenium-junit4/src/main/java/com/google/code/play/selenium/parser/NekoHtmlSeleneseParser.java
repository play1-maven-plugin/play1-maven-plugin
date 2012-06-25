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

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.html.dom.HTMLDocumentImpl;
import org.cyberneko.html.parsers.DOMFragmentParser;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLDocument;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.code.play.selenium.SeleneseParser;

public class NekoHtmlSeleneseParser
    implements SeleneseParser
{

    public List<List<String>> parseSeleneseContent( String content )
        throws SAXException, IOException
    {
        List<List<String>> result = new ArrayList<List<String>>();

        DOMFragmentParser parser = new DOMFragmentParser();
        HTMLDocument document = new HTMLDocumentImpl();
        DocumentFragment fragment = document.createDocumentFragment();
        InputSource inputSource = new InputSource( new StringReader( content ) );
        parser.parse( inputSource, fragment );
        // print( fragment, "frag: " );
        List<Node> tables = findSubNodes( fragment, Node.ELEMENT_NODE, "TABLE" );
        for ( Node table : tables )
        {
            Node tbody = findFirstSubNode( table, Node.ELEMENT_NODE, "TBODY" );
            NodeList childNodes = tbody.getChildNodes();
            for ( int i = 0; i < childNodes.getLength(); i++ )
            {
                Node child = childNodes.item( i );
                // print( child, "frag.Child[" + i + "]: " );
                if ( child.getNodeType() == Node.ELEMENT_NODE && "TR".equals( child.getNodeName() ) )
                {
                    List<String> command = getCommand( child );
                    result.add( command );
                }
            }
        }
        return result;
    }

    // processing table row
    private List<String> getCommand( Node trNode )
    {
        List<String> result = new ArrayList<String>();
        NodeList trChildNodes = trNode.getChildNodes();
        for ( int i = 0; i < trChildNodes.getLength(); i++ )
        {
            Node trChild = trChildNodes.item( i );
            if ( trChild.getNodeType() == Node.ELEMENT_NODE && "TD".equals( trChild.getNodeName() ) )
            {
                result.add( getTableDataValue( trChild ) );
            }
        }
        if ( result.size() != 1 && result.size() != 3 )
        {
            throw new RuntimeException( "Something strange" ); // FIXME
        }
        return result;
    }

    // processing table data
    private String getTableDataValue( Node tdNode )
    {
        StringBuffer buf = new StringBuffer();
        NodeList childNodes = tdNode.getChildNodes();
        for ( int i = 0; i < childNodes.getLength(); i++ )
        {
            Node tdChild = childNodes.item( i );
            switch ( tdChild.getNodeType() )
            {
                case Node.TEXT_NODE:
                    buf.append( tdChild.getNodeValue() );
                    break;
                case Node.ELEMENT_NODE:
                    if ( "BR".equals( tdChild.getNodeName() ) )
                    {
                        buf.append( "<br />" );
                    }
                    break;
                default:
                    // No other cases allowed
                    break;
            }
            // print( tdChild, "tdFrag.Child[" + i + "]: " );
        }
        return buf.toString();
    }

    private Node findFirstSubNode( Node parent, short nodeType, String nodeName )
    {
        Node result = null;
        NodeList childNodes = parent.getChildNodes();
        for ( int i = 0; i < childNodes.getLength(); i++ )
        {
            Node child = childNodes.item( i );
            if ( child.getNodeType() == nodeType && nodeName.equals( child.getNodeName() ) )
            {
                result = child;
                break;
            }
        }
        return result;
    }

    private List<Node> findSubNodes( Node parent, short nodeType, String nodeName )
    {
        List<Node> result = new ArrayList<Node>();
        NodeList childNodes = parent.getChildNodes();
        for ( int i = 0; i < childNodes.getLength(); i++ )
        {
            Node child = childNodes.item( i );
            if ( child.getNodeType() == nodeType && nodeName.equals( child.getNodeName() ) )
            {
                result.add( child );
            }
        }
        return result;
    }

    // private void print( Node node, String prefix )
    // {
    // System.out.println( prefix + node.getNodeName() + ":" + node.getNodeType() + ":" + node.getNodeValue() );
    // }

}
