/*
 * (C) Copyright 2006-2021 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 */
package org.nuxeo.cloud.cluster.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.impl.UserPrincipal;
import org.nuxeo.ecm.core.api.local.ClientLoginModule;
import org.nuxeo.ecm.core.api.local.LoginStack;
import org.nuxeo.ecm.jwt.JWTService;
import org.nuxeo.runtime.mockito.MockitoFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.RuntimeFeature;

@RunWith(FeaturesRunner.class)
@Features({ RuntimeFeature.class, MockitoFeature.class })

@Deploy("org.nuxeo.ecm.jwt")
@Deploy("org.nuxeo.cloud.cluster")
@Deploy("org.nuxeo.cloud.cluster.jwt")
public class JwtComponentTest {

    protected static final String USERNAME = "bob";

    @Inject
    protected JWTService jwtService;

    @Before
    public void doBefore() throws Exception {
        LoginStack loginStack = ClientLoginModule.getThreadLocalLogin();
        loginStack.push(new UserPrincipal(USERNAME, Collections.emptyList(), false, false), null, null);
    }

    @Test
    public void jwt_service_is_clustered() throws Exception {
        assertThat(jwtService).isInstanceOf(ClusteredJWTService.class);
        ClusteredJWTService service = (ClusteredJWTService) jwtService;
        assertThat(service.configuration.getDefaultTTL()).isEqualTo(60);
        assertThat(service.configuration.getMaxTTL()).isEqualTo(60);
        assertThat(service.configuration.getSecretLength()).isEqualTo(30);

    }

    @Test
    @Deploy("org.nuxeo.cloud.cluster.jwt:jwt-configuration.xml")
    public void can_configure_jwt_service() throws Exception {

        ClusteredJWTService service = (ClusteredJWTService) jwtService;
        assertThat(service.configuration.getDefaultTTL()).isEqualTo(120);
        assertThat(service.configuration.getMaxTTL()).isEqualTo(120);
        assertThat(service.configuration.getSecretLength()).isEqualTo(45);
    }



}
