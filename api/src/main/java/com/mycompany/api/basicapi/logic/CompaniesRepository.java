package com.mycompany.api.basicapi.logic;

import com.mycompany.api.basicapi.entities.BasicApiClaims;
import com.mycompany.api.basicapi.entities.Company;
import com.mycompany.api.basicapi.entities.CompanyTransactions;
import com.mycompany.api.basicapi.utilities.BasicApiClaimsAccessor;
import com.mycompany.api.basicapi.plumbing.errors.ClientError;
import com.mycompany.api.basicapi.utilities.JsonFileReader;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import com.mycompany.api.basicapi.utilities.RequestScopeObjectFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.context.annotation.RequestScope;
import static com.ea.async.Async.await;
import static java.util.concurrent.CompletableFuture.completedFuture;

/*
 * The repository is created  to do data access and serve up company data
 */
@Repository
@RequestScope
public class CompaniesRepository {

    /*
     * Injected dependencies
     */
    private final BasicApiClaims claims;
    private final JsonFileReader jsonReader;

    /*
     * Receive dependencies
     */
    public CompaniesRepository(BasicApiClaimsAccessor claimsAccessor, RequestScopeObjectFactory factory)
    {
        this.claims = claimsAccessor.getApiClaims();
        this.jsonReader = factory.createJsonFileReader();
    }

    /*
     * Read the companies list
     */
    public CompletableFuture<Company[]> getCompanyList()
    {
        var companies = await(this.jsonReader.readFile("/data/CompanyList.json", Company[].class));
        var authorizedCompanies = Arrays.stream(companies).filter(c -> this.isUserAuthorizedForCompany(c.getId())).toArray(Company[]::new);

        return completedFuture(authorizedCompanies);
    }

    /*
     * Read the companies list
     */
    public CompletableFuture<CompanyTransactions> getCompanyTransactions(int companyId)
    {
        // If the user is not authorized indicate not found for user
        if(!this.isUserAuthorizedForCompany(companyId)) {
            throw this.unauthorizedError(companyId);
        }

        // First read companies data
        var companies = await(this.jsonReader.readFile("/data/companyList.json", Company[].class));

        // Find the required company
        Optional<Company> foundCompany = Arrays.stream(companies).filter(c -> c.getId() == companyId).findFirst();
        if(foundCompany.isPresent())
        {
            // Next read transactions
            var transactions = await(this.jsonReader.readFile("/data/companyTransactions.json", CompanyTransactions[].class));

            // Find the required transactions
            Optional<CompanyTransactions> foundTransactions = Arrays.stream(transactions).filter(t -> t.getId() == companyId).findFirst();
            if(foundTransactions.isPresent())
            {
                // Form composite results
                var result = foundTransactions.get();
                result.setCompany(foundCompany.get());
                return completedFuture(result);
            }
        }

        // Indicate not found for this user
        throw this.unauthorizedError(companyId);
    }

    /*
     * Apply claims that were read when the access token was first validated
     */
    private boolean isUserAuthorizedForCompany(int companyId) {

        var companies = this.claims.getAccountsCovered();
        return Arrays.stream(companies).anyMatch(c -> c == companyId);
    }

    /*
     * Return an unauthorized error
     */
    private ClientError unauthorizedError(int companyId) {

        var message = String.format("Transactions for company %d were not found for this user", companyId);
        return new ClientError(HttpStatus.NOT_FOUND, "company_not_found", message);
    }
}
