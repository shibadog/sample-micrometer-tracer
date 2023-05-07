package com.github.shibadog.sample.micrometertracer.front;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporter;
import io.opentelemetry.sdk.trace.export.SpanExporter;


@SpringBootApplication
public class FrontApplication {

	public static void main(String[] args) {
		SpringApplication.run(FrontApplication.class, args);
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
	public static class DemoFrontController {
		private final RestTemplate restTemplate;
		public DemoFrontController(RestTemplateBuilder builder) {
			this.restTemplate = builder.build();
		}

		@GetMapping(value="/test")
		public String getMethodName() {
			return restTemplate.getForObject("http://localhost:8081/", String.class);
		}
		
	}
}
