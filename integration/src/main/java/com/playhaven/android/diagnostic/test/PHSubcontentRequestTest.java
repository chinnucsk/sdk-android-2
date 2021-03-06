/**
 * Copyright 2013 Medium Entertainment, Inc.
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
package com.playhaven.android.diagnostic.test;

import android.app.Instrumentation;
import android.content.Context;
import android.test.suitebuilder.annotation.SmallTest;
import com.jayway.jsonpath.JsonPath;
import com.playhaven.android.PlayHavenException;
import com.playhaven.android.diagnostic.Launcher;
import com.playhaven.android.req.SubcontentRequest;
import com.playhaven.android.util.JsonUtil;

public class PHSubcontentRequestTest extends PHTestCase <Launcher> {
    public static final String DISPATCH_CONTEXT = "{ \"url\": \"\", \"additional_parameters\": { \"skip_featured\": \"\", \"placement_id\": \"more_games\", \"args\": \"skip_featured\", \"skip_content\": \"\" } }";
    private String mModel;
    private Exception mE;

    public PHSubcontentRequestTest() {
        super(Launcher.class);
    }

    @SmallTest
    public void testSubcontentRequest() throws Throwable {
        Instrumentation instrumentation = getInstrumentation();
        Context insContext = instrumentation.getTargetContext();
        Launcher launcher = startActivitySync(Launcher.class);

        clearAndConfigurePlayHaven();

        TestSubcontentRequest subcontentRequest = null; 
        try {
            subcontentRequest = new TestSubcontentRequest(DISPATCH_CONTEXT);
        } catch (PlayHavenException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        enableThreadedTesting(subcontentRequest);
        subcontentRequest.send(insContext);
        waitForReady(subcontentRequest);

        if(mE != null)
            fail(mE.getMessage());

        assertNotNull(mModel);
        // A subcontent request, as currently understood, will return a more_games CU (which
        // has the items() element) If PlayHaven starts chaining requests for other reasons then 
        // the nature of the test will change.

        assertNotNull("If this assertion failed, make sure you did not get an empty response from the server", JsonUtil.getPath(mModel, "$.response"));
        assertNotNull(JsonUtil.getPath(mModel, "$.response.context.content.items"));

        launcher.finish();
    }

    private class TestSubcontentRequest extends SubcontentRequest {
        public TestSubcontentRequest(String dispatchContext) throws PlayHavenException {
            super(dispatchContext);
        }

        @Override
        protected void handleResponse(String json) {
            mModel = json;
            markReadyForTesting(this);
        }

        @Override
        protected void handleResponse(PlayHavenException e) {
            mE = e;
            markReadyForTesting(this);
        }
    }
}