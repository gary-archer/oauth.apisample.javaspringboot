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
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;
import com.mycompany.sample.plumbing.errors.ErrorUtils;

/*
 * A controller called during token issuing to ask the API for custom claim values
 * This requires a capability for the Authorization Server to reach out to the API
 */
@RestController()
@Scope(value = CustomRequestScope.NAME)
@RequestMapping(value = "api/customclaims")
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class ClaimsController {

    private final SampleCustomClaimsProvider customClaimsProvider;

    public ClaimsController(final SampleCustomClaimsProvider customClaimsProvider) {
        this.customClaimsProvider = customClaimsProvider;
    }

    /*
     * This is called during token issuance when the Authorization Server supports it
     * The Authorization Server will then include domain specific claims in the JWT access token
     */
    @PostMapping
    public CompletableFuture<ObjectNode> getCustomClaims(final @RequestBody IdentityClaims identityClaims) {

        if (!StringUtils.hasLength(identityClaims.getSubject())) {
            throw ErrorUtils.fromMissingClaim("subject");
        }
        if (!StringUtils.hasLength(identityClaims.getEmail())) {
            throw ErrorUtils.fromMissingClaim("email");
        }

        // Look up domain specific attributes about the user, from the identity attributes
        var claims = (SampleCustomClaims) this.customClaimsProvider.issue(
                identityClaims.getSubject(),
                identityClaims.getEmail());

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
