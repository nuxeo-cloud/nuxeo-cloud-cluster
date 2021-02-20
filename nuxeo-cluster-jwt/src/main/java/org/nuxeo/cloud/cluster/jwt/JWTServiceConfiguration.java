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

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.runtime.model.Descriptor;

@XObject("configuration")
public class JWTServiceConfiguration implements Descriptor {

    public static final JWTServiceConfiguration DEFAULT = builder().build();

    @XNode("defaultTTL")
    private int defaultTTL;

    @XNode("maxTTL")
    private int maxTTL;

    @XNode("secretLength")
    private int secretLength;

    public int getDefaultTTL() {
        return defaultTTL;
    }

    public int getMaxTTL() {
        return maxTTL;
    }

    public int getSecretLength() {
        return secretLength;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int defaultTTL = 60;

        private int maxTTL = 60;

        private int secretLength = 30;

        public Builder withDefaultTTL(int defaultTTL) {
            this.defaultTTL = defaultTTL;
            return this;
        }

        public Builder withMaxTTL(int maxTTL) {
            this.maxTTL = maxTTL;
            return this;
        }

        public Builder withSecretLength(int secretLength) {
            this.secretLength = secretLength;
            return this;
        }

        public JWTServiceConfiguration build() {
            JWTServiceConfiguration conf = new JWTServiceConfiguration();
            conf.defaultTTL = this.defaultTTL;
            conf.maxTTL = this.maxTTL;
            conf.secretLength = this.secretLength;

            return conf;
        }

    }

    @Override
    public String getId() {
        return "conf";

    }

}
