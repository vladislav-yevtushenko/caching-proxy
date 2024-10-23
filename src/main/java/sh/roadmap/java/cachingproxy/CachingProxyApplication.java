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
package sh.roadmap.java.cachingproxy;

import java.util.Optional;
import java.util.OptionalInt;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.core.env.SimpleCommandLinePropertySource;

@SpringBootApplication
@EnableCaching
public class CachingProxyApplication {

    public static final int DEFAULT_PORT = 3000;
    public static final String DEFAULT_ORIGIN_URL = "http://dummyjson.com";
    public static final String ORIGIN_ARG = "origin";
    public static final String PORT_ARG = "port";

    public static void main(String[] args) {
        final int port = getPortFromCmdLine(args).orElse(DEFAULT_PORT);
        final String origin = getOriginFromCmdLine(args).orElse(DEFAULT_ORIGIN_URL);

        new SpringApplication(CachingProxyApplication.class)
            .run("--server.port=" + port, "--global.origin=" + origin);
    }

    private static OptionalInt getPortFromCmdLine(final String[] args) {
        final var source = new SimpleCommandLinePropertySource(args);
        return source.containsProperty(PORT_ARG) ?
               OptionalInt
                   .of(Integer.parseInt(source.getProperty(PORT_ARG))) : OptionalInt.empty();

    }

    private static Optional<String> getOriginFromCmdLine(final String[] args) {
        final var source = new SimpleCommandLinePropertySource(args);
        return source.containsProperty(ORIGIN_ARG) ?
               Optional.ofNullable(source.getProperty(ORIGIN_ARG)) : Optional.empty();
    }

}
