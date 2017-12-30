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

package org.skywalking.apm.agent.core.context.trace;

import org.skywalking.apm.agent.core.context.util.KeyValuePair;
import org.skywalking.apm.agent.core.context.util.ThrowableTransformer;
import org.skywalking.apm.agent.core.dictionary.DictionaryUtil;
import org.skywalking.apm.network.proto.SpanObject;
import org.skywalking.apm.network.proto.SpanType;
import org.skywalking.apm.network.trace.component.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 链路追踪 Span 抽象类
 *
 * The <code>AbstractTracingSpan</code> represents a group of {@link AbstractSpan} implementations, which belongs a real
 * distributed trace.
 *
 * @author wusheng
 */
public abstract class AbstractTracingSpan implements AbstractSpan {

    /**
     * Span 编号
     */
    protected int spanId;
    /**
     * 父 Span 编号
     */
    protected int parentSpanId;
    /**
     * 键值标签数组
     */
    protected List<KeyValuePair> tags;
    /**
     * 操作名
     */
    protected String operationName;
    /**
     * 操作编号
     */
    protected int operationId;
    /**
     * 分层
     */
    protected SpanLayer layer;
    /**
     * The start time of this Span.
     */
    protected long startTime;
    /**
     * The end time of this Span.
     */
    protected long endTime;
    /**
     * Error has occurred in the scope of span.
     */
    protected boolean errorOccurred = false;
    /**
     * Component 编号
     */
    protected int componentId = 0;
    /**
     * Component 名字
     */
    protected String componentName;

    /**
     * Log is a concept from OpenTracing spec. <p> {@see https://github.com/opentracing/specification/blob/master/specification.md#log-structured-data}
     */
    protected List<LogDataEntity> logs;

    protected AbstractTracingSpan(int spanId, int parentSpanId, String operationName) {
        this.operationName = operationName;
        this.operationId = DictionaryUtil.nullValue();
        this.spanId = spanId;
        this.parentSpanId = parentSpanId;
    }

    protected AbstractTracingSpan(int spanId, int parentSpanId, int operationId) {
        this.operationName = null;
        this.operationId = operationId;
        this.spanId = spanId;
        this.parentSpanId = parentSpanId;
    }

    /**
     * Set a key:value tag on the Span.
     *
     * @return this Span instance, for chaining
     */
    @Override
    public AbstractTracingSpan tag(String key, String value) {
        if (tags == null) {
            tags = new LinkedList<KeyValuePair>();
        }
        tags.add(new KeyValuePair(key, value));
        return this;
    }

    /**
     * Finish the active Span. When it is finished, it will be archived by the given {@link TraceSegment}, which owners
     * it.
     *
     * @param owner of the Span.
     */
    public boolean finish(TraceSegment owner) {
        this.endTime = System.currentTimeMillis();
        owner.archive(this);
        return true;
    }

    @Override
    public AbstractTracingSpan start() {
        this.startTime = System.currentTimeMillis();
        return this;
    }

    /**
     * Record an exception event of the current walltime timestamp.
     *
     * @param t any subclass of {@link Throwable}, which occurs in this span.
     * @return the Span, for chaining
     */
    @Override
    public AbstractTracingSpan log(Throwable t) {
        if (logs == null) {
            logs = new LinkedList<LogDataEntity>();
        }
        // https://github.com/opentracing-contrib/opentracing-specification-zh/blob/master/semantic_conventions.md#log-field-%E6%B8%85%E5%8D%95
        logs.add(new LogDataEntity.Builder()
            .add(new KeyValuePair("event", "error"))
            .add(new KeyValuePair("error.kind", t.getClass().getName()))
            .add(new KeyValuePair("message", t.getMessage()))
            .add(new KeyValuePair("stack", ThrowableTransformer.INSTANCE.convert2String(t, 4000)))
            .build(System.currentTimeMillis()));
        return this;
    }

    /**
     * Record a common log with multi fields, for supporting opentracing-java
     *
     * @param fields
     * @return the Span, for chaining
     */
    @Override
    public AbstractTracingSpan log(long timestampMicroseconds, Map<String, ?> fields) {
        if (logs == null) {
            logs = new LinkedList<LogDataEntity>();
        }
        LogDataEntity.Builder builder = new LogDataEntity.Builder();
        for (Map.Entry<String, ?> entry : fields.entrySet()) {
            builder.add(new KeyValuePair(entry.getKey(), entry.getValue().toString()));
        }
        logs.add(builder.build(timestampMicroseconds));
        return this;
    }

    /**
     * In the scope of this span tracing context, error occurred, in auto-instrumentation mechanism, almost means throw
     * an exception.
     *
     * @return span instance, for chaining.
     */
    @Override
    public AbstractTracingSpan errorOccurred() {
        this.errorOccurred = true;
        return this;
    }

    /**
     * Set the operation name, just because these is not compress dictionary value for this name. Use the entire string
     * temporarily, the agent will compress this name in async mode.
     *
     * @param operationName
     * @return span instance, for chaining.
     */
    @Override
    public AbstractTracingSpan setOperationName(String operationName) {
        this.operationName = operationName;
        this.operationId = DictionaryUtil.nullValue();
        return this;
    }

    /**
     * Set the operation id, which compress by the name.
     *
     * @param operationId
     * @return span instance, for chaining.
     */
    @Override
    public AbstractTracingSpan setOperationId(int operationId) {
        this.operationId = operationId;
        this.operationName = null;
        return this;
    }

    @Override
    public int getSpanId() {
        return spanId;
    }

    @Override
    public int getOperationId() {
        return operationId;
    }

    @Override
    public String getOperationName() {
        return operationName;
    }

    @Override
    public AbstractTracingSpan setLayer(SpanLayer layer) {
        this.layer = layer;
        return this;
    }

    /**
     * Set the component of this span, with internal supported. Highly recommend to use this way.
     *
     * @param component
     * @return span instance, for chaining.
     */
    @Override
    public AbstractTracingSpan setComponent(Component component) {
        this.componentId = component.getId();
        return this;
    }

    /**
     * Set the component name. By using this, cost more memory and network.
     *
     * @param componentName
     * @return span instance, for chaining.
     */
    @Override
    public AbstractTracingSpan setComponent(String componentName) {
        this.componentName = componentName;
        return this;
    }

    public SpanObject.Builder transform() {
        SpanObject.Builder spanBuilder = SpanObject.newBuilder();

        spanBuilder.setSpanId(this.spanId);
        spanBuilder.setParentSpanId(parentSpanId);
        spanBuilder.setStartTime(startTime);
        spanBuilder.setEndTime(endTime);
        if (operationId != DictionaryUtil.nullValue()) {
            spanBuilder.setOperationNameId(operationId);
        } else {
            spanBuilder.setOperationName(operationName);
        }
        if (isEntry()) {
            spanBuilder.setSpanType(SpanType.Entry);
        } else if (isExit()) {
            spanBuilder.setSpanType(SpanType.Exit);
        } else {
            spanBuilder.setSpanType(SpanType.Local);
        }
        if (this.layer != null) {
            spanBuilder.setSpanLayerValue(this.layer.getCode());
        }
        if (componentId != DictionaryUtil.nullValue()) {
            spanBuilder.setComponentId(componentId);
        } else {
            if (componentName != null) {
                spanBuilder.setComponent(componentName);
            }
        }
        spanBuilder.setIsError(errorOccurred);
        if (this.tags != null) {
            for (KeyValuePair tag : this.tags) {
                spanBuilder.addTags(tag.transform());
            }
        }
        if (this.logs != null) {
            for (LogDataEntity log : this.logs) {
                spanBuilder.addLogs(log.transform());
            }
        }

        return spanBuilder;
    }
}