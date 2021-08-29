package com.mycompany.sample.host.controllers;

import static java.util.concurrent.CompletableFuture.completedFuture;
import java.util.concurrent.CompletableFuture;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mycompany.sample.host.claims.SampleClaimsProvider;
import com.mycompany.sample.plumbing.dependencies.CustomRequestScope;

/*
 * A controller called during token issuing to ask the API for custom claim values
 * This requires a capability for the Authorization Server to reach out to the API
 */
@RestController()
@Scope(value = CustomRequestScope.NAME)
@RequestMapping(value = "api/customclaims")
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class ClaimsController {

    private final SampleClaimsProvider claimsProvider;

    public ClaimsController(final SampleClaimsProvider claimsProvider) {
        this.claimsProvider = claimsProvider;
    }

    /*
     * This is called during token issuance by the Authorization Server when using the StandardAuthorizer
     * The custom claims are then included in the access token
     */
    @GetMapping(value = "{subject}")
    public CompletableFuture<ObjectNode> getCustomClaims(
            @PathVariable("subject") final String subject) {

        var claims = this.claimsProvider.supplyCustomClaimsFromSubject(subject);

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
