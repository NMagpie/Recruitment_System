package com.rectruit.authentication.database;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.HashSet;
import java.util.Set;

@Document(collection = "users")
public class User {
    @Getter
    @Setter
    @MongoId
    private String _id;

    @Getter
    @Setter
    private String username;

    @Getter
    @Setter
    private String password;

    @Getter
    @Setter
    private String userType;

    @Getter
    @Setter
    private Set<String> roles = new HashSet<>();
}
