package com.mycompany.api.basicapi.logic;

import com.mycompany.api.basicapi.entities.Company;
import com.mycompany.api.basicapi.entities.CompanyTransactions;
import com.mycompany.api.basicapi.plumbing.errors.ClientError;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.concurrent.CompletableFuture;

/*
 * The API controller
 */
@RestController
@RequestMapping(value = "api/companies")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CompaniesController
{
    private final CompaniesRepository repository;

    /*
     * Receive dependencies
     */
    public CompaniesController(CompaniesRepository repository)
    {
        this.repository = repository;
    }

    /*
     * Return a list of companies
     */
    @GetMapping(value="")
    public CompletableFuture<Company[]> GetCompanyList()
    {
        return this.repository.GetCompanyList();
    }

    /*
     * Return a composite object containing company transactions
     */
    @GetMapping(value="{companyId}/transactions")
    public CompletableFuture<CompanyTransactions> GetCompanyTransactions(@PathVariable("companyId") Integer companyId) throws ClientError
    {
        return this.repository.GetCompanyTransactions(companyId);
    }
}
