package net.kamradtfamily.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.stream.Collectors;

import static org.springframework.web.reactive.function.BodyExtractors.toMono;
import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@Slf4j
@Component
@Profile("stockroom")
public class StockroomController {
    @Bean
    RouterFunction<?> getStockroomRoute(StockroomRepository repository) {
        return route(GET("/stockroom/{id}"),
                request -> {
                    log.info("in get by id {}", request.pathVariable("id"));
                    Mono<Stockroom> stockroom = Mono.justOrEmpty(request.pathVariable("id"))
                            .flatMap(repository::findById)
                            .doOnNext(s -> log.info("on next {}", s));
                    return ok().body(fromPublisher(stockroom, Stockroom.class));
                })
                .andRoute(GET("/stockroom"),
                        request -> {
                            log.info("in get all");
                            Flux<Stockroom> stockroom = repository.findAll()
                                    .doOnNext(s -> log.info("on next {}", s));
                            return ok().body(fromPublisher(stockroom, Stockroom.class));
                        })
                .andRoute(POST("/stockroom"),
                        request -> {
                            log.info("in post");
                            Mono<Stockroom> stockroom = request.body(toMono(Stockroom.class));
                            return ok().body(fromPublisher(repository.saveAll(stockroom)
                                    .doOnNext(s -> log.info("on next {}", s)), Stockroom.class));
                        })
                .andRoute(POST("/stockroom/{id}/receive"),
                request -> {
                    log.info("in receive");
                    request.headers().asHttpHeaders().entrySet().forEach(es -> log.info("header {}: {}", es.getKey(), es.getValue().stream().collect(Collectors.joining())));
                    Mono<Inventory> inventory = request.body(toMono(Inventory.class));
                    Mono<Stockroom> stockroom = repository.findById(request.pathVariable("id"))
                            .zipWith(inventory)
                            .map(t -> {
                                if(t.getT1().getInventoryList() == null) {
                                    t.getT1().setInventoryList(new ArrayList<>());
                                }
                                t.getT1().getInventoryList().add(t.getT2());
                                return t.getT1();
                            });
                    return ok().body(fromPublisher(repository.saveAll(stockroom)
                            .doOnNext(s -> log.info("on next {}", s))
                            .last(), Stockroom.class));
                });
    }
}

