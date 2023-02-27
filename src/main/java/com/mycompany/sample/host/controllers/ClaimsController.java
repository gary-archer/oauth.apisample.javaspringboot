package com.mycompany.sample.host.controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;
import java.util.concurrent.CompletableFuture;
import org.springframework.context.annotation.Scope;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.logic.claims.IdentityClaims;
import com.mycompany.sample.logic.claims.SampleCustomClaimsProvider;
import com.mycompany.sample.logic.entities.SampleCustomClaims;
import com.mycompany.sample.plumbing.configuration.OAuthConfiguration;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.mycompany.sample.plumbing.oauth.ScopeVerifier;

/*
 * A controller called during token issuing to ask the API for custom claim values
 * This requires a capability for the Authorization Server to reach out to the API
 */
@RestController()
@Scope(value = CustomRequestScope.NAME)
@RequestMapping(value = "investments/customclaims")
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class ClaimsController {

    private final OAuthConfiguration configuration;
    private final SampleCustomClaimsProvider customClaimsProvider;

    public ClaimsController(
            final OAuthConfiguration configuration,
            final SampleCustomClaimsProvider customClaimsProvider) {

        this.configuration = configuration;
        this.customClaimsProvider = customClaimsProvider;
    }

    /*
     * This is called during token issuance when the Authorization Server supports it
     * The Authorization Server will then include domain specific claims in the JWT access token
     */
    @PostMapping
    public CompletableFuture<ObjectNode> getCustomClaims(final @RequestBody IdentityClaims identityClaims) {

        // The endpoint is only enabled when this claims strategy is used
        if (!this.configuration.getClaimsStrategy().equals("jwt")) {
            ScopeVerifier.deny();
        }

        // Sanity checks on required input
        if (!StringUtils.hasLength(identityClaims.getSubject())) {
            throw ErrorUtils.fromMissingClaim("subject");
        }
        if (!StringUtils.hasLength(identityClaims.getEmail())) {
            throw ErrorUtils.fromMissingClaim("email");
        }

        // Send identity claims and receive domain specific claims
        var claims = (SampleCustomClaims) this.customClaimsProvider.issue(
                identityClaims.getSubject(),
                identityClaims.getEmail());

        // Extract and return values
        var mapper = new ObjectMapper();
        var data = mapper.createObjectNode();
        data.put("user_id", claims.getUserId());
        data.put("user_role", claims.getUserRole());

        var regionsNode = mapper.createArrayNode();
        for (String region: claims.getUserRegions()) {
            regionsNode.add(region);
        }

        data.set("user_regions", regionsNode);
        return completedFuture(data);
    }
}
