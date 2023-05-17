package com.github.shibadog.sample.micrometertracer.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.qos.logback.access.tomcat.LogbackValve;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;


@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

    @Bean
    TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcatServletWebServerFactory = new TomcatServletWebServerFactory();
        // LogbackValveはresources以下を参照するため、これでlogback-access.xmlの内容が反映される
        tomcatServletWebServerFactory.addContextValves(new LogbackValve());
        return tomcatServletWebServerFactory;
    }

	@Configuration
	public static class AppConfig {
		@Bean
		SpanExporter otlpHttpSpanExporter(
				@Value("${management.otlp.tracing.endpoint}") String endpoint
		) {
			return OtlpHttpSpanExporter.builder()
					.setEndpoint(endpoint)
					.build();
		}
	}

	@RestController
	public static class DemoController {
		@GetMapping(value="/")
		public String getMapping() {
			return "aaaa";
		}
	}
}
