package sh.roadmap.java.cachingproxy;

import org.springframework.boot.SpringApplication;

public class TestCachingProxyApplication {

    public static void main(String[] args) {
        SpringApplication.from(CachingProxyApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
