package com.recruit.orchestrator.http;

import lombok.*;

@AllArgsConstructor
@RequiredArgsConstructor
public class Transaction {

    @Getter
    @Setter
    @NonNull
    private String id;

    @Getter
    @Setter
    private String prefix = "";

}
