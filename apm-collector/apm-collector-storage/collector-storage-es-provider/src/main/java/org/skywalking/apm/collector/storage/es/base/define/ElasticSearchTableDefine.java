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

package org.skywalking.apm.collector.storage.es.base.define;

import org.skywalking.apm.collector.core.data.TableDefine;

/**
 * 基于 Elasticsearch 的表定义抽象类
 *
 * @author peng-yongsheng
 */
public abstract class ElasticSearchTableDefine extends TableDefine {

    public ElasticSearchTableDefine(String name) {
        super(name);
    }

    /**
     * 文档元数据 _type 字段
     *
     * @return {@link org.skywalking.apm.collector.core.data.CommonTable#TABLE_TYPE}
     */
    public final String type() {
        return "type";
    }

    /**
     * @return 索引刷新频率，https://www.elastic.co/guide/cn/elasticsearch/guide/current/near-real-time.html#refresh-api
     */
    public abstract int refreshInterval();

}
