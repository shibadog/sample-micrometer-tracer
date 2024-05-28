package com.github.shibadog.sample.micrometertracer.front;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import ch.qos.logback.access.tomcat.LogbackValve;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
public class FrontApplication {

	public static void main(String[] args) {
		SpringApplication.run(FrontApplication.class, args);
	}

    @Bean
    TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcatServletWebServerFactory = new TomcatServletWebServerFactory();
        // LogbackValveはresources以下を参照するため、これでlogback-access.xmlの内容が反映される
        tomcatServletWebServerFactory.addContextValves(new LogbackValve());
        return tomcatServletWebServerFactory;
    }

	@Service
	@Slf4j
	public static class DemoFrontService {

		@Observed(name = "service function")
		public String exec() {
			try {
				TimeUnit.MILLISECONDS.sleep(100L);
				log.info("service test");
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			return "OK";
		}
	}

	@RestController
	@Slf4j
	public static class DemoFrontController {
		private final RestTemplate restTemplate;
		private final ObservationRegistry registry;
		private final DemoFrontService service;
		private final String backendUrl;

		public DemoFrontController(ObservationRegistry registry,
				@Value("${app.backend-url}") String backendUrl,
				RestTemplateBuilder builder,
				DemoFrontService service) {
			this.registry = registry;
			this.backendUrl = backendUrl;
			this.restTemplate = builder.build();
			this.service = service;
		}

		@GetMapping(value="/test")
		public String getMethodName() {
			Observation.createNotStarted("test", registry)
				.observe(() -> log.info("test"));
			service.exec();
			return restTemplate.getForObject(backendUrl, String.class);
		}
		
	}
}
