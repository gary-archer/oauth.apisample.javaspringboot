package com.mycompany.api.basicapi.logic;

import com.mycompany.api.basicapi.entities.Company;
import com.mycompany.api.basicapi.entities.CompanyTransactions;
import com.mycompany.api.basicapi.plumbing.errors.ClientError;
import com.mycompany.api.basicapi.plumbing.oauth.ApiClaimsProvider;
import com.mycompany.api.basicapi.plumbing.utilities.JsonFileReader;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Repository;
import static com.ea.async.Async.await;
import static java.util.concurrent.CompletableFuture.completedFuture;

/*
 * The repository is created  to do data access and serve up company data
 */
@Repository
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class CompaniesRepository {

    /*
     * Injected dependencies
     */
    private final ApiClaimsProvider claimsProvider;
    private final JsonFileReader jsonReader;

    /*
     * Receive dependencies
     */
    public CompaniesRepository(ApiClaimsProvider claimsProvider, JsonFileReader jsonReader)
    {
        this.claimsProvider = claimsProvider;
        this.jsonReader = jsonReader;
    }

    /*
     * Read the companies list
     */
    public CompletableFuture<Company[]> GetCompanyList()
    {
        Company[] companies = await(this.jsonReader.ReadFile("/data/CompanyList.json", Company[].class));
        Company[] authorizedCompanies = Arrays.stream(companies).filter(c -> this.isUserAuthorizedForCompany(c.id)).toArray(Company[]::new);

        return completedFuture(authorizedCompanies);
    }

    /*
     * Read the companies list
     */
    public CompletableFuture<CompanyTransactions> GetCompanyTransactions(Integer companyId) throws ClientError
    {
        // If the user is not authorized indicate not found for user
        if(!this.isUserAuthorizedForCompany(companyId)) {
            String message = String.format("Transactions for company %d were not found for this user", companyId);
            throw new ClientError(404, "DataAccess", message);
        }

        // First read companies data
        Company[] companies = await(this.jsonReader.ReadFile("/data/companyList.json", Company[].class));

        // Find the required company
        Optional<Company> foundCompany = Arrays.stream(companies).filter(c -> c.id.equals(companyId)).findFirst();
        if(foundCompany.isPresent())
        {
            // Next read transactions
            CompanyTransactions[] transactions = await(this.jsonReader.ReadFile("/data/companyTransactions.json", CompanyTransactions[].class));

            // Find the required transactions
            Optional<CompanyTransactions> foundTransactions = Arrays.stream(transactions).filter(t -> t.id.equals(companyId)).findFirst();
            if(foundTransactions.isPresent())
            {
                // Form composite results
                CompanyTransactions result = foundTransactions.get();
                result.company = foundCompany.get();
                return completedFuture(result);
            }
        }

        // Indicate not found for this user
        String message = String.format("Transactions for company %d were not found for this user", companyId);
        throw new ClientError(404, "DataAccess", message);
    }

    /*
     * Apply claims that were read when the access token was first validated
     */
    private Boolean isUserAuthorizedForCompany(Integer companyId) {

        Integer[] companies = this.claimsProvider.getApiClaims().getUserCompanyIds();
        return Arrays.stream(companies).anyMatch(c -> c == companyId);
    }
}
