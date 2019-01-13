package com.mycompany.api.basicapi.logic;

import com.mycompany.api.basicapi.entities.Company;
import com.mycompany.api.basicapi.entities.CompanyTransactions;
import com.mycompany.api.basicapi.plumbing.errors.ClientError;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import java.util.concurrent.CompletableFuture;

/*
 * For fewer thread safety risks we create the controller class on every API request
 * We implement async according to the below post, which also shows how to take greater control over threading
 * http://humansreadcode.com/spring-boot-completablefuture/
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
        // Generic properties that we should ensure are set correctly
        /*Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("*** DEBUG: Is authenticated: " + authentication.isAuthenticated());
        System.out.println("*** DEBUG: Identity name: " + authentication.getName());
        System.out.println("*** DEBUG: Principal: " + authentication.getPrincipal());
        System.out.println("*** DEBUG: Credentials: " + authentication.getCredentials());*/

        return this.repository.GetCompanyList();
    }

    /*
     * Return a composite object containing company transactions
     */
    @GetMapping(value="{companyId}/transactions")
    public CompletableFuture<CompanyTransactions> GetCompanyTransactions(@PathVariable("companyId") Integer companyId) throws ClientError
    {
        //String info = String.format("*** CompanyTransactions current thread is %d", Thread.currentThread().getId());
        //System.out.println(info);

        return this.repository.GetCompanyTransactions(companyId);
    }
}
