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

import static java.util.stream.Collectors.toMap;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.nuxeo.cloud.cluster.api.ClusterService;
import org.nuxeo.ecm.jwt.JWTServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;

public class ClusteredJWTService extends JWTServiceImpl {

    private static final String SECRETS_LIST_KEY = "org.nuxeo.jwt.secrets";

    private static final Logger LOG = LoggerFactory.getLogger(ClusteredJWTService.class);

    public static class SecretHolder implements Serializable {

        private static final long serialVersionUID = 1L;

        String value;

        long creationTime;

        public SecretHolder(String value) {
            this.value = value;
            this.creationTime = System.currentTimeMillis();
        }

        @Override
        public int hashCode() {
            return Objects.hash(creationTime, value);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SecretHolder other = (SecretHolder) obj;
            return creationTime == other.creationTime && Objects.equals(value, other.value);
        }
    }

    private ClusterService clusterService;

    JWTServiceConfiguration configuration;

    /**
     * @param service
     */
    public ClusteredJWTService(ClusterService clusterService, JWTServiceConfiguration configuration) {
        this.clusterService = clusterService;
        this.configuration = configuration;

        // Rotate key at max TTL
        clusterService.scheduleAtFixedRate(this::rotateSecret, configuration.getMaxTTL(), configuration.getMaxTTL(),
                TimeUnit.SECONDS);

    }

    public ClusteredJWTService(ClusterService clusterService) {
        this(clusterService, JWTServiceConfiguration.DEFAULT);
    }

    @Override
    protected Algorithm getAlgorithm() {
        return getCurrentAlgorithm();
    }

    protected Algorithm getCurrentAlgorithm() {
        List<SecretHolder> secrets = clusterService.getList(SECRETS_LIST_KEY);
        if (secrets.isEmpty()) {
            secrets.add(new SecretHolder(RandomStringUtils.random(30, true, true)));
        }

        return Algorithm.HMAC512(secrets.get(0).value);
    }

    public void rotateSecret() {
        LOG.info("Rotating JWT secret");
        List<SecretHolder> secrets = clusterService.getList(SECRETS_LIST_KEY);
        List<SecretHolder> toRemove = secrets.stream()
                                             .filter(s -> s.creationTime < System.currentTimeMillis()
                                                     - TimeUnit.SECONDS.toMillis(this.configuration.getMaxTTL()))
                                             .collect(Collectors.toList());

        secrets.removeAll(toRemove);
        secrets.add(0, new SecretHolder(RandomStringUtils.random(30, true, true)));
    }

    @Override
    public Map<String, Object> verifyToken(String token) {

        List<SecretHolder> secrets = clusterService.getList(SECRETS_LIST_KEY);

        for (SecretHolder secret : secrets) {
            try {

                Objects.requireNonNull(token);
                Algorithm algorithm = Algorithm.HMAC512(secret.value);

                JWTVerifier verifier = JWT.require(algorithm) //
                                          .withIssuer(JWTServiceImpl.NUXEO_ISSUER)
                                          .build();
                DecodedJWT jwt = verifier.verify(token);
                return jwtTokenToMap(jwt);

            } catch (JWTVerificationException e) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("token verification failed: " + e.toString());
                }
            }
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Token verification failed");
        }
        return null;

    }

    private Map<String, Object> jwtTokenToMap(DecodedJWT jwt) {
        Object payload = getFieldValue(jwt, "payload"); // com.auth0.jwt.impl.PayloadImpl
        Map<String, JsonNode> tree = getFieldValue(payload, "tree");
        return tree.entrySet().stream().collect(toMap(Entry<String, JsonNode>::getKey, e -> nodeToValue(e.getValue())));
    }

}
