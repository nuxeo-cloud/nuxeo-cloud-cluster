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
package org.nuxeo.cloud.cluster.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface ClusterService {

    /**
     * Returns a cluster wide named map.
     *
     * @param name
     * @return
     */
    <K, V> Map<K, V> getMap(String name);

    /**
     * Returns a cluster wide named list.
     *
     * @param name
     * @return
     */
    <T> List<T> getList(String name);

    /**
     * Schedule a runnable at fixed rate.
     *
     * @param command
     * @param initialDelay
     * @param period
     * @param unit
     */
    void scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit);

    void leave();

    void join();
}
