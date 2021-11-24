package net.kamradtfamily.factory;

import brave.baggage.BaggageField;
import brave.baggage.CorrelationScopeConfig;
import brave.context.slf4j.MDCScopeDecorator;
import brave.propagation.CurrentTraceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class FactoryApplication {

	public static void main(String[] args) {
		SpringApplication.run(FactoryApplication.class, args);
	}

	// configuration
	@Bean
	BaggageField userIdField() {
		return BaggageField.create("user-id");
	}

	@Bean
	CurrentTraceContext.ScopeDecorator mdcScopeDecorator() {
		return MDCScopeDecorator.newBuilder()
				.clear()
				.add(CorrelationScopeConfig.SingleCorrelationField.newBuilder(userIdField())
						.flushOnUpdate()
						.build())
				.build();
	}

}
