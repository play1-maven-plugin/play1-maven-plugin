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

package com.google.code.play.selenium;

public class Timeout
{
    public final static int DEFAULT_TIMEOUT = 30 * 1000; // milliseconds

    private int defaultTimeout = Timeout.DEFAULT_TIMEOUT;

    public int get()
    {
        return defaultTimeout;
    }

    public void reset()
    {
        this.defaultTimeout = Timeout.DEFAULT_TIMEOUT;
    }

    public void set( int timeout )
    {
        this.defaultTimeout = timeout;
    }

}
