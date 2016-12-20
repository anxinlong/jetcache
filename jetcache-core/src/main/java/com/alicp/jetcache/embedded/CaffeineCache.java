package com.alicp.jetcache.embedded;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

/**
 * Created on 2016/10/25.
 *
 * @author <a href="mailto:yeli.hl@taobao.com">huangli</a>
 */
public class CaffeineCache<K, V> extends AbstractEmbeddedCache<K, V> {

    private com.github.benmanes.caffeine.cache.Cache cache;

    public CaffeineCache(EmbeddedCacheConfig config) {
        super(config);
    }

    @Override
    public <T> T unwrap(Class<T> clazz) {
        if (clazz.equals(com.github.benmanes.caffeine.cache.Cache.class)) {
            return (T) cache;
        }
        throw new IllegalArgumentException(clazz.getName());
    }

    @Override
    protected InnerMap createAreaCache() {
        Caffeine<Object, Object> builder = Caffeine.newBuilder();
        builder.maximumSize(config.getLimit());
        if (config.isExpireAfterAccess()) {
            builder.expireAfterAccess(config.getDefaultExpireInMillis(), TimeUnit.MILLISECONDS);
        } else {
            builder.expireAfterWrite(config.getDefaultExpireInMillis(), TimeUnit.MILLISECONDS);
        }
        cache = builder.build();
        return new InnerMap() {
            @Override
            public Object getValue(Object key) {
                return cache.getIfPresent(key);
            }

            @Override
            public void putValue(Object key, Object value) {
                cache.put(key, value);
            }

            @Override
            public boolean removeValue(Object key) {
                return cache.asMap().remove(key) != null;
            }
        };
    }
}
