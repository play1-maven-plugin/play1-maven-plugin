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

package com.google.code.play;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;

import play.Play;
import play.server.Server;

public class PlayServerBooter
{

    public static void main( String[] args )
        throws Exception
    {
        String outFile = System.getProperty( "outFile" );
        if ( outFile != null )
        {
            PrintStream out = new PrintStream( new File( outFile ) );
            System.setOut( out );
            System.setErr( out );
            System.getProperties().remove( "outFile" );
        }

        String pidFileName = System.getProperty( "pidFile" );
        if ( pidFileName != null )
        {
            // http://blog.igorminar.com/2007/03/how-java-application-can-discover-its.html
            String name = ManagementFactory.getRuntimeMXBean().getName();
            String pidStr = "unknown";
            int p = name.indexOf( '@' );
            if ( p > 0 )
            {
                pidStr = name.substring( 0, p );
            }
            File pidFile = new File( pidFileName );
            FileWriter fw = new FileWriter( pidFile );
            try
            {
                fw.write( pidStr );
            }
            finally
            {
                fw.close();
            }
            System.getProperties().remove( "pidFile" );
        }

        Play.frameworkPath = new File( System.getProperty( "play.home" ) );
        Server.main( args );
    }

}
