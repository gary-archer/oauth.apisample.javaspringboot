package com.authsamples.api.host.controllers;

import java.util.List;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.authsamples.api.logic.entities.Company;
import com.authsamples.api.logic.entities.CompanyTransactions;
import com.authsamples.api.logic.errors.SampleErrorCodes;
import com.authsamples.api.logic.services.CompanyService;
import com.authsamples.api.plumbing.dependencies.CustomRequestScope;
import com.authsamples.api.plumbing.errors.ErrorFactory;
import com.google.common.primitives.Ints;

/*
 * A controller to return company related secure data to the caller
 */
@RestController()
@Scope(value = CustomRequestScope.NAME)
@RequestMapping(value = "investments/companies")
public class CompanyController {

    private final CompanyService service;

    /*
     * Claims are injected into the controller after OAuth processing
     */
    public CompanyController(final CompanyService service) {
        this.service = service;
    }

    /*
     * Return a list of companies
     */
    @GetMapping(value = "")
    public List<Company> getCompanyList() {
        return this.service.getCompanyList();
    }

    /*
     * Return a composite object containing company transactions
     */
    @GetMapping(value = "{companyId}/transactions")
    public CompanyTransactions getCompanyTransactions(
            @PathVariable("companyId") final String companyId) {

        // Throw a 400 error if we have a malformed ID
        var idValue = Ints.tryParse(companyId);
        if (idValue == null || idValue <= 0) {
            throw ErrorFactory.createClientError(
                    HttpStatus.BAD_REQUEST,
                    SampleErrorCodes.INVALID_COMPANY_ID,
                    "The company ID must be a positive numeric integer");
        }

        // Next authorize access based on claims
        return this.service.getCompanyTransactions(idValue);
    }
}
