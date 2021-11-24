package net.kamradtfamily.factory;

import brave.baggage.BaggageField;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

import static org.springframework.web.reactive.function.BodyExtractors.toMono;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Slf4j
@Component
@Profile("mailroom")
public class MailroomController {
    @Value("${stockroom.server}")
    String stockroomServer="http://localhost:8080/stockroom";
    // create the web client via a bean so that sleuth can instrument it with headers
    @Bean
    WebClient getWebClient() {
        return WebClient.create(stockroomServer);

    }
    @Bean
    RouterFunction<?> getMailroomRoute(MailroomRepository repository,
                                       WebClient webClient,
                                       BaggageField userIdField) {
        return route(GET("/mailroom/{id}"),
                request -> {
                    log.info("in get by id {}", request.pathVariable("id"));
                    Mono<Mailroom> mailroom = Mono.justOrEmpty(request.pathVariable("id"))
                            .flatMap(repository::findById)
                            .doOnNext(s -> log.info("on next {}", s));
                    return ok().body(fromPublisher(mailroom, Mailroom.class));
                })
                .andRoute(GET("/mailroom"),
                        request -> {
                            log.info("in get all");
                            Flux<Mailroom> mailroom = repository.findAll()
                                    .doOnNext(s -> log.info("on next {}", s));
                            return ok().body(fromPublisher(mailroom, Mailroom.class));
                        })
                .andRoute(POST("/mailroom"),
                        request -> {
                            log.info("in post");
                            Mono<Mailroom> mailroom = request.body(toMono(Mailroom.class));
                            return ok().body(fromPublisher(repository.saveAll(mailroom)
                                    .doOnNext(s -> log.info("on next {}", s)), Mailroom.class));
                        })
                .andRoute(POST("/mailroom/{id}/{attn}/receive"),
                request -> {
                    userIdField.updateValue(request.pathVariable("id"));
                    log.info("in receive");
                    Mono<Stockroom>  stockroom = repository.findById(request.pathVariable("id"))
                            .flatMap(m -> webClient.post()
                                    .uri(uriBuilder -> uriBuilder
                                            .pathSegment(request.pathVariable("attn"),"receive")
                                            .build())
                                    .body(request.body(toMono(Inventory.class)), Inventory.class)
                                    .accept(MediaType.APPLICATION_JSON)
                                    .exchangeToMono(clientResponse -> clientResponse.bodyToMono(Stockroom.class)))
                            .doOnNext(i -> log.info("on next {}", i));
                    return ok().body(fromPublisher(stockroom, Stockroom.class));
                });
    }
}

