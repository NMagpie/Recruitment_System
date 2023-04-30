package com.rectruit.authentication.saga_pattern;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class TransactionService {

    @Autowired
    private final TransactionLogRepository transactionLogRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public TransactionService(TransactionLogRepository transactionLogRepository) {
        this.transactionLogRepository = transactionLogRepository;
    }

    public boolean isDocumentLocked(String documentId, String documentType) {
        List<TransactionLog> transactionLogs = transactionLogRepository.findByDocumentIdAndDocumentType(documentId, documentType);
        return !transactionLogs.isEmpty();
    }

    @Transactional
    public TransactionLog prepare_document(Document document, TransactionLog.Action action, String collectionName) {
        MongoCollection<Document> collection = mongoTemplate.getCollection(collectionName);
        Document previousState = new Document();
        String documentId = document.getString("_id");

        switch (action) {
            case CREATE -> documentId = collection.insertOne(document).getInsertedId().asObjectId().getValue().toString();
            case UPDATE -> {
                Document oldDocument = collection.find(Filters.eq("_id", documentId)).first();
                if (oldDocument != null) {
                    previousState = oldDocument;
                    collection.replaceOne(Filters.eq("_id", documentId), document);
                }
            }
            case DELETE -> {
                previousState = collection.find(Filters.eq("_id", documentId)).first();
                collection.deleteOne(Filters.eq("_id", documentId));
            }
        }

        TransactionLog log = new TransactionLog();
        log.setDocumentType(collectionName);
        log.setDocumentId(documentId);
        log.setAction(action);
        log.setPreviousState(previousState);
        mongoTemplate.save(log);

        return log;
    }

    @Transactional
    public void saga_success(String transactionId) {
        transactionLogRepository.deleteById(new ObjectId(transactionId));
    }

    @Transactional
    public void saga_fail(String transactionId) {
        // Get the transaction log object with the specified ID
        TransactionLog transactionLog = mongoTemplate.findById(new ObjectId(transactionId), TransactionLog.class);

        // Get the action of the transaction log
        TransactionLog.Action action = transactionLog.getAction();

        // Perform action based on transaction log
        switch (action) {
            case CREATE ->
                // Delete document from corresponding collection
                    mongoTemplate.remove(Query.query(Criteria.where("_id").is(transactionLog.getDocumentId())), transactionLog.getDocumentType());
            case UPDATE -> {
                // Get the document with the corresponding ID from the corresponding collection
                Document previousState = new Document(transactionLog.getPreviousState());
                mongoTemplate.updateFirst(Query.query(Criteria.where("_id").is(transactionLog.getDocumentId())), Update.fromDocument(previousState), transactionLog.getDocumentType());
            }
            case DELETE ->
                // Create document in the corresponding collection based on previous state
                    mongoTemplate.save(new Document(transactionLog.getPreviousState()), transactionLog.getDocumentType());
        }

        // Delete the transaction log object from the database
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(new ObjectId(transactionId))), TransactionLog.class);
    }
}

