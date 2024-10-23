/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024-2024 Vladislav Yevtushenko
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package sh.roadmap.java.cachingproxy.controller;

import java.util.concurrent.Callable;
import org.springframework.cache.Cache;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;

public class CustomHeaderResponseCache implements ResponseEntityCache {

    private final Cache delegate;

    public CustomHeaderResponseCache(final Cache delegate) {
        this.delegate = delegate;
    }

    @Override
    @NonNull
    public String getName() {
        return delegate.getName();
    }

    @Override
    @NonNull
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    @Override
    public ValueWrapper get(@NonNull final Object key) {
        return delegate.get(key);
    }

    @Override
    public <T> T get(@NonNull final Object key, final Class<T> type) {
        return delegate.get(key, type);
    }

    @Override
    public <T> T get(@NonNull final Object key, @NonNull final Callable<T> valueLoader) {
        return delegate.get(key, valueLoader);
    }

    @Override
    public void evict(@NonNull final Object key) {
        delegate.evict(key);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public void put(@NonNull final Object key, final ResponseEntity<String> value) {
        delegate.put(key, new CachedResponseEntity(value));
    }
}
