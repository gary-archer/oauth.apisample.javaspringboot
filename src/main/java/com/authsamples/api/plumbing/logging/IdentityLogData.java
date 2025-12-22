package com.authsamples.api.plumbing.logging;

import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.node.ObjectNode;

/*
 * Identity data to log
 */
public class IdentityLogData {

    // A stable anonymous identifier for the user
    @Getter
    @Setter
    private String userId;

    // The delegation ID
    @Getter
    @Setter
    private String delegationId;

    // The client ID or name
    @Getter
    @Setter
    private String clientId;

    // The scope to audit
    @Getter
    @Setter
    private String scope;

    // Claims to audit
    @Getter
    @Setter
    private ObjectNode claims;
}
