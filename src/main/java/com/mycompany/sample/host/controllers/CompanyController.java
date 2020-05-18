package com.mycompany.sample.host.controllers;

import com.google.common.primitives.Ints;
import com.mycompany.sample.host.plumbing.errors.ErrorFactory;
import com.mycompany.sample.logic.entities.Company;
import com.mycompany.sample.logic.entities.CompanyTransactions;
import com.mycompany.sample.host.claims.SampleApiClaims;
import com.mycompany.sample.logic.errors.ErrorCodes;
import com.mycompany.sample.logic.services.CompanyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/*
 * A controller to return company related info to the caller
 */
@RestController()
@RequestScope
@RequestMapping(value = "api/companies")
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class CompanyController {

    private final CompanyService service;
    private final SampleApiClaims claims;

    public CompanyController(final CompanyService service, final SampleApiClaims claims) {
        this.service = service;
        this.claims = claims;
    }

    /*
     * Return a list of companies
     */
    @GetMapping(value = "")
    public CompletableFuture<List<Company>> getCompanyList() {
        return this.service.getCompanyList(claims.getRegionsCovered());
    }

    /*
     * Return a composite object containing company transactions
     */
    @GetMapping(value = "{companyId}/transactions")
    public CompletableFuture<CompanyTransactions> getCompanyTransactions(
            @PathVariable("companyId") final String companyId) {

        // Throw a 400 error if we have an invalid id
        var idValue = Ints.tryParse(companyId);
        if (idValue == null || idValue <= 0) {
            throw ErrorFactory.createClientError(
                    HttpStatus.BAD_REQUEST,
                    ErrorCodes.INVALID_COMPANY_ID,
                    "The company id must be a positive numeric integer");
        }

        return this.service.getCompanyTransactions(idValue, claims.getRegionsCovered());
    }
}
