/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.config;

/**
 * This enum lists all the supported configuration keys.
 *
 * @author Martin Kouba
 * @see WeldConfiguration
 * @see SystemPropertiesConfiguration
 */
public enum ConfigurationKey {

    /**
     * Indicates whether ConcurrentDeployer and ConcurrentValidator should be enabled. If enabled, ConcurrentDeployer and ConcurrentValidator execute their
     * subtasks using {@link org.jboss.weld.manager.api.ExecutorServices} which can be configured separately.
     *
     * Otherwise, single-threaded version of Deployer and Validator are used.
     *
     * By default, concurrent deployment is enabled.
     */
    CONCURRENT_DEPLOYMENT("org.jboss.weld.bootstrap.concurrentDeployment", true),

    /**
     * The number of threads used by ContainerLifecycleEventPreloader. The ContainerLifecycleEventPreloader allows observer methods for container lifecycle
     * events to be resolved upfront while the deployment is waiting for classloader or reflection API.
     *
     * ContainerLifecycleEventPreloader has its own thread pool whose size is configured by this property.
     *
     * If set to 0, ContainerLifecycleEventPreloader is not installed.
     *
     * If not specified, the value is set to Math.max(1, Runtime.getRuntime().availableProcessors() - 1)).
     *
     */
    PRELOADER_THREAD_POOL_SIZE("org.jboss.weld.bootstrap.preloaderThreadPoolSize", Math.max(1, Runtime.getRuntime().availableProcessors() - 1)),

    /**
     * Allows an integrator to enable the non-portable mode. Non-portable mode is suggested by the specification to overcome problems with legacy applications
     * not using CDI SPI properly.
     *
     * The non-portable mode is disabled by default.
     */
    NON_PORTABLE_MODE("org.jboss.weld.nonPortableMode", false),

    /**
     * The number of threads to be used for bean loading and deployment.
     */
    EXECUTOR_THREAD_POOL_SIZE("org.jboss.weld.executor.threadPoolSize", Runtime.getRuntime().availableProcessors()),

    /**
     * If set to true, debug timing information is printed to the standard output.
     */
    EXECUTOR_THREAD_POOL_DEBUG("org.jboss.weld.executor.threadPoolDebug", false),

    /**
     * The type of the thread pool. Possible values are: FIXED, FIXED_TIMEOUT, NONE, SINGLE_THREAD.
     */
    EXECUTOR_THREAD_POOL_TYPE("org.jboss.weld.executor.threadPoolType", ""),

    /**
     * Keep-alive time in seconds. Passed to the constructor of the ThreadPoolExecutor class, maximum time that excess idle threads will wait for new tasks
     * before terminating.
     */
    EXECUTOR_THREAD_POOL_KEEP_ALIVE_TIME("org.jboss.weld.executor.threadPoolKeepAliveTime", 60L),

    /**
     * Weld caches already resolved injection points in order to resolve them faster in the future. There exists a separate type safe resolver for beans,
     * decorators, disposers, interceptors and observers. Each of them stores resolved injection points in its cache, which maximum size is bounded by a default
     * value (common to all of them).
     *
     * @see <a href="https://issues.jboss.org/browse/WELD-1323">WELD-1323</a>
     */
    RESOLUTION_CACHE_SIZE("org.jboss.weld.resolution.cacheSize", 0x100000L),

    /**
     * For debug purposes, it's possible to dump the generated bytecode of proxies and subclasses.
     */
    PROXY_DUMP("org.jboss.weld.proxy.dump", ""),

    /**
     * Weld supports a non-standard workaround to be able to create client proxies for Java types that cannot be proxied by the container, using non-portable
     * JVM APIs.
     */
    PROXY_UNSAFE("org.jboss.weld.proxy.unsafe", false),

    /**
     * XML descriptor validation is enabled by default.
     */
    DISABLE_XML_VALIDATION("org.jboss.weld.xml.disableValidating", false),

    /**
     * For certain combinations of scopes, the container is permitted to optimize an injectable reference lookup. The optimization is disabled by default as it
     * does not match the {@link javax.enterprise.context.spi.AlterableContext} contract.
     */
    INJECTABLE_REFERENCE_OPTIMIZATION("org.jboss.weld.injection.injectableReferenceOptimization", false),

    /**
     * A regular expression - types matching this pattern is excluded from monitoring.
     */
    PROBE_INVOCATION_MONITOR_EXCLUDE_TYPE("org.jboss.weld.probe.invocationMonitor.excludeType", ""),

    /**
     * If set to <code>true</code> the JavaBean accessor methods are not monitored.
     */
    PROBE_INVOCATION_MONITOR_SKIP_JAVABEAN_PROPERTIES("org.jboss.weld.probe.invocationMonitor.skipJavaBeanProperties", true),

    ;

    /**
     *
     * @param key The string representation of the key
     * @param defaultValue The default value
     */
    ConfigurationKey(String key, Object defaultValue) {
        this.key = key;
        // Fail fast if a new key with unsupported value type is introduced
        if (!isValueTypeSupported(defaultValue.getClass())) {
            throw new IllegalArgumentException("Unsupported value type: " + defaultValue);
        }
        this.defaultValue = defaultValue;
    }

    private final String key;

    private final Object defaultValue;

    /**
     * @return the string representation of the key
     */
    public String get() {
        return key;
    }

    /**
     * @return the default value
     */
    public Object getDefaultValue() {
        return defaultValue;
    }

    /**
     *
     * @param value
     * @return <code>true</code> if the given value corresponds to the type of the default value, <code>false</code> otherwise
     */
    public boolean isValidValue(Object value) {
        return isValidValueType(value.getClass());
    }

    /**
     *
     * @param valueType
     * @return <code>true</code> if the given value type corresponds to the type of the default value, <code>false</code> otherwise
     */
    public boolean isValidValueType(Class<?> valueType) {
        return defaultValue.getClass().isAssignableFrom(valueType);
    }

    /**
     *
     * @param value
     * @return the converted value
     */
    public Object convertValue(String value) {
        if (defaultValue instanceof Boolean) {
            return Boolean.valueOf(value);
        } else if (defaultValue instanceof Long) {
            return Long.valueOf(value);
        } else if (defaultValue instanceof Integer) {
            return Integer.valueOf(value);
        } else {
            return value.toString();
        }
    }

    /**
     *
     * @param valueType
     * @return <code>true</code> if the given value type is supported, <code>false</code> otherwise
     */
    public static boolean isValueTypeSupported(Class<?> valueType) {
        return valueType.equals(String.class) || valueType.equals(Boolean.class) || valueType.equals(Integer.class) || valueType.equals(Long.class);
    }

    /**
     *
     * @param from
     * @return the key with the given value, or <code>null</code> if no such exists
     */
    public static ConfigurationKey fromString(String from) {
        for (ConfigurationKey key : values()) {
            if (key.get().equals(from)) {
                return key;
            }
        }
        return null;
    }

}
