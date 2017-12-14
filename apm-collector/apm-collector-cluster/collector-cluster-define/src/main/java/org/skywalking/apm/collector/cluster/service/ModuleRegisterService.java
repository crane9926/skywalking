/*
 * Copyright 2017, OpenSkywalking Organization All rights reserved.
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
 * Project repository: https://github.com/OpenSkywalking/skywalking
 */

package org.skywalking.apm.collector.cluster.service;

import org.skywalking.apm.collector.cluster.ModuleRegistration;
import org.skywalking.apm.collector.core.module.Service;

/**
 * 模块注册服务
 *
 * @author peng-yongsheng
 */
public interface ModuleRegisterService extends Service {

    /**
     * 注册模块注册信息
     *
     * @param moduleName 模块名字
     * @param providerName 服务提供者名字
     * @param registration 模块注册信息
     */
    void register(String moduleName, String providerName, ModuleRegistration registration);

}
