package net.kamradtfamily.factory;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockroomRepository extends ReactiveMongoRepository<Stockroom, String> {
}
