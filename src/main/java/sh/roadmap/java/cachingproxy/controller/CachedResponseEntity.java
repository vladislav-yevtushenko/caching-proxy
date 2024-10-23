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

import java.util.List;
import java.util.Map;
import lombok.Builder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import static java.util.stream.Collectors.toUnmodifiableMap;
import static sh.roadmap.java.cachingproxy.controller.CacheHeader.HIT;

@Builder
public final class CachedResponseEntity extends ResponseEntity<String> {

    public static final String X_CACHE = "X-Cache";
    private ResponseEntity<String> delegate;

    public CachedResponseEntity(final ResponseEntity<String> delegate) {
        super(delegate.getBody(), delegate.getStatusCode());
        this.delegate = delegate;

    }

    @Override
    @NonNull
    public HttpHeaders getHeaders() {
        final Map<String, List<String>> originalHeaders = this.delegate.getHeaders()
                                                                       .entrySet()
                                                                       .stream()
                                                                       .filter(e -> !e.getKey().equals(X_CACHE))
                                                                       .collect(toUnmodifiableMap(Map.Entry::getKey,
                                                                                                  Map.Entry::getValue
                                                                       ));


        final HttpHeaders cacheInfoHeaders = new HttpHeaders();
        cacheInfoHeaders.putAll(originalHeaders);
        cacheInfoHeaders.add(X_CACHE, HIT.toString());
        return cacheInfoHeaders;
    }


}
