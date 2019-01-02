package com.mycompany.api.basicapi.logic;

import com.mycompany.api.basicapi.entities.Company;
import com.mycompany.api.basicapi.entities.CompanyTransactions;
import com.mycompany.api.basicapi.plumbing.errors.ClientError;
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

    private final JsonFileReader jsonReader;

    /*
     * Receive dependencies
     */
    public CompaniesRepository(JsonFileReader jsonReader)
    {
        this.jsonReader = jsonReader;
    }

    /*
     * Read the companies list
     */
    public CompletableFuture<Company[]> GetCompanyList()
    {
        return this.jsonReader.ReadFile("/data/CompanyList.json", Company[].class);
    }

    /*
     * Read the companies list
     */
    public CompletableFuture<CompanyTransactions> GetCompanyTransactions(Integer companyId) throws ClientError
    {
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
}
