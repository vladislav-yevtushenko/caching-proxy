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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.function.BiFunction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheResolver;
import org.springframework.cache.interceptor.SimpleCacheResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;
import static sh.roadmap.java.cachingproxy.controller.CacheHeader.MISS;

@Configuration
public class Config {
    @Value("${global.origin}")
    private String origin;

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("cache1");
    }

    @Bean
    public CacheResolver cacheResolver(final CacheManager cacheManager) {
        return new CustomCacheResolver(new SimpleCacheResolver(cacheManager));
    }

    @Bean
    public BiFunction<HttpRequest, URI, HttpRequest> httpRequestBiFunction() {
        return (request, uri) -> new HttpRequest() {
            @Override
            public HttpMethod getMethod() {
                return request.getMethod();
            }

            @Override
            public URI getURI() {
                return uri;
            }

            @Override
            public HttpHeaders getHeaders() {
                return request.getHeaders();
            }
        };
    }

    @Bean
    public ClientHttpRequestInterceptor clientHttpRequestInterceptor(final BiFunction<HttpRequest, URI, HttpRequest> createNewRequest) {
        return new ClientHttpRequestInterceptor() {
            @Override
            @NonNull
            public ClientHttpResponse intercept(@NonNull final HttpRequest request,
                                                @NonNull final byte[] body,
                                                @NonNull final ClientHttpRequestExecution execution) throws IOException {
                ClientHttpResponse resp = execution.execute(request, body);
                while (resp.getStatusCode().is3xxRedirection()) {
                    final var location = resp.getHeaders().getLocation().toString();
                    final URI uri = UriComponentsBuilder.fromUriString(location).build().toUri();
                    final var newRequest = createNewRequest.apply(request, uri);
                    resp = execution.execute(newRequest, body);
                }
                return getResponseWithXCacheMissHeader(resp);
            }
        };
    }

    private ClientHttpResponse getResponseWithXCacheMissHeader(final ClientHttpResponse resp) {
        return new ClientHttpResponse() {
            @Override
            public HttpHeaders getHeaders() {
                final HttpHeaders originalHeaders = resp.getHeaders();
                final HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.put("X-Cache", List.of(MISS.toString()));
                httpHeaders.putAll(originalHeaders);
                return httpHeaders;
            }

            @Override
            public InputStream getBody() throws IOException {
                return resp.getBody();
            }

            @Override
            public HttpStatusCode getStatusCode() throws IOException {
                return resp.getStatusCode();
            }

            @Override
            public String getStatusText() throws IOException {
                return resp.getStatusText();
            }

            @Override
            public void close() {
                resp.close();
            }
        };
    }


    @Bean
    public RestClient restClient(final ClientHttpRequestInterceptor interceptor) {

        return RestClient.builder().baseUrl(origin).requestInterceptor(interceptor).build();
    }

    @Bean
    public RestClientAdapter restClientAdapter(final RestClient restClient) {
        return RestClientAdapter.create(restClient);
    }

    @Bean
    public HttpServiceProxyFactory httpServiceProxyFactory(final RestClientAdapter restClientAdapter) {
        return HttpServiceProxyFactory.builderFor(restClientAdapter).build();
    }

    @Bean
    public ProxyService proxyService(final HttpServiceProxyFactory httpServiceProxyFactory) {
        return httpServiceProxyFactory.createClient(ProxyService.class);
    }

}
