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

/**
 * Run Play! Server ("play run" equivalent).
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @goal run
 * @requiresDependencyResolution test
 */
public class PlayRunMojo
    extends AbstractPlayRunMojo
{
    /**
     * Play! id (profile) used.
     * 
     * @parameter expression="${play.id}" default-value=""
     * @since 1.0.0
     */
    private String playId;

    /**
     * Play! id (profile) used when running server with tests.
     * 
     * @parameter expression="${play.testId}" default-value="test"
     * @since 1.0.0
     */
    private String playTestId;

    /**
     * Run server with test profile.
     * 
     * @parameter expression="${play.runWithTests}" default-value="false"
     * @since 1.0.0
     */
    private boolean runWithTests;

    @Override
    protected String getPlayId()
    {
        return ( runWithTests ? playTestId : playId );
    }

}
