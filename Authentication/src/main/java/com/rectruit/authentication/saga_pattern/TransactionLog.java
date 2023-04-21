package com.rectruit.authentication.saga_pattern;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Map;

@Document(collection = "transactions")
public class TransactionLog {

    public enum Action {
        CREATE("create"),
        UPDATE("update"),
        DELETE("delete");

        private final String action_string;

        Action(String action_string) {
            this.action_string = action_string;
        }

        @Override
        public String toString() {
            return action_string;
        }
    }


    @Getter
    @Setter
    @MongoId
    private ObjectId _id;

    @Getter
    @Setter
    private String documentType;

    @Getter
    @Setter
    private String documentId;

    @Getter
    @Setter
    private Action action;

    @Getter
    @Setter
    private Map<String, Object> previousState;
}

