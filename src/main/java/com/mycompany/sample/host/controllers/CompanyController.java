package com.mycompany.sample.host.controllers;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import com.google.common.primitives.Ints;
import com.mycompany.sample.host.claims.SampleApiClaims;
import com.mycompany.sample.logic.entities.Company;
import com.mycompany.sample.logic.entities.CompanyTransactions;
import com.mycompany.sample.logic.errors.SampleErrorCodes;
import com.mycompany.sample.logic.services.CompanyService;
import com.mycompany.sample.plumbing.errors.ErrorFactory;

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

    /*
     * The claims object is injected into the controller or other classes after OAuth processing
     */
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
                    SampleErrorCodes.INVALID_COMPANY_ID,
                    "The company id must be a positive numeric integer");
        }

        return this.service.getCompanyTransactions(idValue, claims.getRegionsCovered());
    }
}
