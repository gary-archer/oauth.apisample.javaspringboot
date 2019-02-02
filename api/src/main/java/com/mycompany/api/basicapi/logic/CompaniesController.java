package com.mycompany.api.basicapi.logic;

import com.mycompany.api.basicapi.entities.Company;
import com.mycompany.api.basicapi.entities.CompanyTransactions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import java.util.concurrent.CompletableFuture;

/*
 * For fewer thread safety risks we create the controller class on every API request and use request scoped objects
 */
@RestController()
@RequestMapping(value = "api/companies")
@RequestScope
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
        return this.repository.getCompanyList();
    }

    /*
     * Return a composite object containing company transactions
     */
    @GetMapping(value="{companyId}/transactions")
    public CompletableFuture<CompanyTransactions> GetCompanyTransactions(@PathVariable("companyId") Integer companyId)
    {
        return this.repository.getCompanyTransactions(companyId);
    }
}
