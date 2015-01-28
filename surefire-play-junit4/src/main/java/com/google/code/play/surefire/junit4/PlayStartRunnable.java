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

package com.google.code.play.surefire.junit4;

import play.Play;

import java.io.File;

public class PlayStartRunnable
    extends AbstractRunnable
{
    private File playHome;

    private File applicationPath;

    private String playId;

    public PlayStartRunnable( File playHome, File applicationPath, String playId )
    {
        this.playHome = playHome;
        this.applicationPath = applicationPath;
        this.playId = playId;
    }

    public void methodToRun()
    {
        System.setProperty( "application.path", applicationPath.getAbsolutePath() );
        System.setProperty( "play.id", ( playId != null ? playId : "" ) );
        Play.frameworkPath = playHome;
        Play.init( applicationPath, playId );
        if ( !Play.started ) // in PROD mode or ... Play! is started automatically
        {
            Play.start();
        }
    }

}
