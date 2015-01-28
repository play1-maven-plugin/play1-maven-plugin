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

import com.thoughtworks.selenium.CommandProcessor;

import com.google.code.play.selenium.StoredVars;

public abstract class AbstractSeleniumCommand
{

    protected StoredVars storedVars;

    protected CommandProcessor commandProcessor;

    protected String command;

    protected String param1;

    protected String param2;

    public AbstractSeleniumCommand( StoredVars storedVars, CommandProcessor commandProcessor, String command,
                                    String param1 )
    {
        this.storedVars = storedVars;
        this.commandProcessor = commandProcessor;
        this.command = command;
        this.param1 = param1;
        this.param2 = "";
    }

    public AbstractSeleniumCommand( StoredVars storedVars, CommandProcessor commandProcessor, String command,
                                    String param1, String param2 )
    {
        this( storedVars, commandProcessor, command, param1 );
        this.param2 = param2;
    }

}
