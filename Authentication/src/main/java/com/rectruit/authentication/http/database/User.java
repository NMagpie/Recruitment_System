package com.rectruit.authentication.http.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.HashSet;
import java.util.Set;

@Document(collection = "users")
public class User {
    @Getter
    @Setter
    @MongoId
    private ObjectId _id;

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Getter
    @Setter
    private String userType;

    @Getter
    @Setter
    private Set<String> roles = new HashSet<>();
}
