package com.rectruit.authentication.saga_pattern;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionLogRepository extends MongoRepository<TransactionLog, ObjectId> {
    List<TransactionLog> findByDocumentIdAndDocumentType(String document_id, String documentType);
}
