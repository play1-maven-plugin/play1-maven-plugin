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

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Run Play&#33; server in test mode ("play test" equivalent).
 * 
 * @author <a href="mailto:gslowikowski@gmail.com">Grzegorz Slowikowski</a>
 * @since 1.0.0
 */
@Mojo( name = "test", requiresDependencyResolution = ResolutionScope.TEST )
public class PlayTestRunMojo
    extends AbstractPlayRunMojo
{
    /**
     * Play! test id (profile) used.
     * 
     * @since 1.0.0
     */
    @Parameter( property = "play.testId", defaultValue = "test" )
    private String playTestId;

    @Override
    protected String getPlayId()
    {
        return playTestId;
    }

}
