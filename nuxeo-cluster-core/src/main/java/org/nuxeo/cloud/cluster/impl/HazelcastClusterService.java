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

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.nuxeo.cloud.cluster.api.ClusterService;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.internal.util.XmlUtil;
import com.hazelcast.scheduledexecutor.IScheduledExecutorService;

public class HazelcastClusterService implements ClusterService {

    private HazelcastInstance hzInstance;

    @Override
    public <K, V> Map<K, V> getMap(String name) {
        return hzInstance.getMap(name);
    }

    @Override
    public void leave() {
        hzInstance.shutdown();
    }

    @Override
    public void join() {
        System.setProperty(XmlUtil.SYSTEM_PROPERTY_IGNORE_XXE_PROTECTION_FAILURES, "true");
        hzInstance = Hazelcast.newHazelcastInstance();

    }

    @Override
    public <T> List<T> getList(String name) {
        return hzInstance.getList(name);
    }

    @Override
    public void scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        IScheduledExecutorService es = hzInstance.getScheduledExecutorService("nuxeoSchedule");
        es.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

}
