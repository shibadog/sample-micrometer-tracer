package com.github.shibadog.sample.micrometertracer.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;


@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
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
