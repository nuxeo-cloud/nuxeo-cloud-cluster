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
package org.nuxeo.cloud.cluster.impl;

import org.nuxeo.cloud.cluster.api.ClusterService;
import org.nuxeo.runtime.model.ComponentContext;
import org.nuxeo.runtime.model.DefaultComponent;

public class ClusterServiceComponent extends DefaultComponent {

    protected ClusterService clusterService;

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (ClusterService.class.equals(adapter)) {
            return (T) clusterService;
        }
        return null;
    }

    private ClusterService buildClusterService() {

        ClusterService cluster = new HazelcastClusterService();
        cluster.join();
        return cluster;
    }

    @Override
    public void activate(ComponentContext context) {
        super.activate(context);
        clusterService = buildClusterService();
    }

    @Override
    public void deactivate(ComponentContext context) {
        super.deactivate(context);
        clusterService.leave();
    }

}
