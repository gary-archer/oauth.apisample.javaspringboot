package com.mycompany.sample.logic.repositories;

import static java.util.concurrent.CompletableFuture.completedFuture;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import static com.ea.async.Async.await;
import com.mycompany.sample.logic.entities.Company;
import com.mycompany.sample.logic.entities.CompanyTransactions;
import com.mycompany.sample.logic.utilities.JsonFileReader;
import com.mycompany.sample.plumbing.logging.LogEntry;

/*
 * The repository receives user context to apply authorization rules
 */
@Repository
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class CompanyRepository {

    private final JsonFileReader jsonReader;
    private final LogEntry logEntry;

    public CompanyRepository(final JsonFileReader jsonReader, final LogEntry logEntry) {
        this.jsonReader = jsonReader;
        this.logEntry = logEntry;
    }

    /*
     * Read the companies list
     */
    public CompletableFuture<List<Company>> getCompanyList() {

        try (var breakdown = this.logEntry.createPerformanceBreakdown("getCompanyList")) {

            var companies = await(this.jsonReader.readFile("data/companyList.json", Company[].class));
            return completedFuture(Arrays.stream(companies).collect(Collectors.toList()));
        }
    }

    /*
     * Read the transactions from a database
     */
    public CompletableFuture<CompanyTransactions> getCompanyTransactions(final int companyId) {

        // Record the time taken for data access
        try (var breakdown = this.logEntry.createPerformanceBreakdown("getCompanyTransactions")) {

            // First read companies data
            var companies = await(this.jsonReader.readFile("data/companyList.json", Company[].class));

            // Find the required company
            Optional<Company> foundCompany = Arrays.stream(companies).filter(c -> c.getId() == companyId).findFirst();
            if (foundCompany.isPresent()) {

                // Next read transactions
                var transactions = await(
                    this.jsonReader.readFile("data/companyTransactions.json",
                    CompanyTransactions[].class));

                // Find the required transactions
                Optional<CompanyTransactions> foundTransactions =
                    Arrays.stream(transactions).filter(t -> t.getId() == companyId).findFirst();

                if (foundTransactions.isPresent()) {

                    // Form composite results
                    var result = foundTransactions.get();
                    result.setCompany(foundCompany.get());
                    return completedFuture(result);
                }
            }

            // Indicate no data found
            return completedFuture(null);
        }
    }
}
