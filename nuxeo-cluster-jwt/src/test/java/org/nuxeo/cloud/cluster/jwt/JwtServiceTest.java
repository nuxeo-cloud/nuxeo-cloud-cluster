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
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.nuxeo.ecm.jwt.JWTClaims.CLAIM_ISSUER;
import static org.nuxeo.ecm.jwt.JWTClaims.CLAIM_SUBJECT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.nuxeo.cloud.cluster.api.ClusterService;
import org.nuxeo.ecm.core.api.impl.UserPrincipal;
import org.nuxeo.ecm.core.api.local.ClientLoginModule;
import org.nuxeo.ecm.core.api.local.LoginStack;

public class JwtServiceTest {

    protected static final String USERNAME = "bob";

    ClusteredJWTService jwtService;

    private List<Object> secrets;

    @Before
    public void doBefore() {
        LoginStack loginStack = ClientLoginModule.getThreadLocalLogin();
        loginStack.push(new UserPrincipal(USERNAME, Collections.emptyList(), false, false), null, null);

        secrets = new ArrayList<>();
        ClusterService clusterService = mock(ClusterService.class);
        when(clusterService.getList(anyString())).thenReturn(secrets);
        jwtService = new ClusteredJWTService(clusterService, JWTServiceConfiguration.builder().withMaxTTL(1).build());
    }

    @Test
    public void can_create_and_verify_token() throws Exception {
        String token = jwtService.newBuilder().withClaim("myclaim", Long.valueOf(123456)).build();
        Map<String, Object> claims = jwtService.verifyToken(token);
        String subject = (String) claims.get(CLAIM_SUBJECT);
        assertEquals(USERNAME, subject);
        String issuer = (String) claims.get(CLAIM_ISSUER);
        assertEquals("nuxeo", issuer);
        Long myclaim = (Long) claims.get("myclaim");
        assertEquals(Long.valueOf(123456), myclaim);

    }

    @Test
    public void can_create_rotate_secret_and_verify_tokens() throws Exception {
        String token = jwtService.newBuilder().withClaim("myclaim", Long.valueOf(123456)).build();
        assertThat(jwtService.verifyToken(token)).isNotNull();

        jwtService.rotateSecret();

        assertThat(jwtService.verifyToken(token)).isNotNull();

        token = jwtService.newBuilder().withClaim("otherclaim", Long.valueOf(456)).build();
        assertThat(jwtService.verifyToken(token)).isNotNull();

        assertThat(secrets).hasSize(2);

    }

    @Test
    public void expired_secrets_are_removed() throws Exception {
        String token = jwtService.newBuilder().withClaim("myclaim", Long.valueOf(123456)).build();
        assertThat(jwtService.verifyToken(token)).isNotNull();

        jwtService.rotateSecret();
        jwtService.rotateSecret();
        jwtService.rotateSecret();
        assertThat(secrets).hasSize(4);

        Thread.sleep(TimeUnit.SECONDS.toMillis(1) + 1);
        jwtService.rotateSecret();
        assertThat(secrets).hasSize(1);

    }

}
