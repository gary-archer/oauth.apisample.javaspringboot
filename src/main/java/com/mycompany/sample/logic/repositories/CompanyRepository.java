package com.mycompany.sample.logic.repositories;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import com.mycompany.sample.logic.entities.Company;
import com.mycompany.sample.logic.entities.CompanyTransactions;
import com.mycompany.sample.logic.utilities.JsonFileReader;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.mycompany.sample.plumbing.logging.LogEntry;

/*
 * The repository should be able to use simple code to do async processing
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
     * Read the companies list using async await syntax
     */
    public CompletableFuture<List<Company>> getCompanyList() {

        var breakdown = this.logEntry.createPerformanceBreakdown("getCompanyList");

        BiFunction<Company[], Throwable, List<Company>> callback = (data, ex) -> {

            // End the performance breakdown
            breakdown.close();

            // Handle read errors
            if (ex != null) {
                throw ErrorUtils.fromException(ex);
            }

            // Return the result
            return Arrays.stream(data).collect(Collectors.toList());
        };

        return this.jsonReader.readFile("data/companyList.json", Company[].class)
            .handle(callback);
    }

    /*
     * Read the transactions from a database, which does 2 async reads surrounded by a performance breakdown
     * Java currently requires horrific syntax when coding nested completable futures
     */
    public CompletableFuture<CompanyTransactions> getCompanyTransactions(final int companyId) {

        // This will hold the result of the first async callback
        final AtomicReference<Company> foundCompanyRef = new AtomicReference<>();

        // Start recording the time taken for data access
        var breakdown = this.logEntry.createPerformanceBreakdown("getCompanyTransactions");

        // Find the requested company in company data, after the companies read operation completes
        BiFunction<Company[], Throwable, Company>
                companiesCallback = (companiesData, ex1) -> {

            // End the performance breakdown when there is a companies read error
            if (ex1 != null) {
                breakdown.close();
                throw ErrorUtils.fromException(ex1);
            }

            // Find the requested company and return it
            var companies = Arrays.stream(companiesData).collect(Collectors.toList());
            var found = companies.stream().filter(c -> c.getId() == companyId).findFirst();
            return found.orElse(null);
        };

        // Filter the transactions data for the found company, after the transactions read operation completes
        BiFunction<CompanyTransactions[], Throwable, CompanyTransactions>
                transactionsCallback = (transactionsData, ex2) -> {

            // End the performance breakdown for the error case during transaction reads
            if (ex2 != null) {
                breakdown.close();
                throw ErrorUtils.fromException(ex2);
            }

            // End the performance breakdown when no companies are found
            var foundCompany = foundCompanyRef.get();
            if (foundCompany == null) {
                breakdown.close();
                return null;
            }

            // Find the transactions for the requested company
            Optional<CompanyTransactions> foundTransactions =
                    Arrays.stream(transactionsData).filter(t -> t.getId() == companyId).findFirst();
            if (foundTransactions.isPresent()) {

                // End the performance breakdown when transactions are found
                breakdown.close();

                // Return the result for the success case
                var result = foundTransactions.get();
                result.setCompany(foundCompany);
                return result;
            }

            // End the performance breakdown when transactions are not found
            breakdown.close();
            return null;
        };

        // Kick off the async await code, and update the atomic reference after the first read
        return this.jsonReader.readFile("data/companyList.json", Company[].class)
            .handle(companiesCallback)
            .thenCompose(company -> {
                foundCompanyRef.set(company);
                return this.jsonReader.readFile("data/companyTransactions.json", CompanyTransactions[].class)
                        .handle(transactionsCallback);
            });
    }

    /*
     * The above code used to look like this when I used the ea-async library, but it is no longer maintained
     *
    public CompletableFuture<CompanyTransactions> getCompanyTransactions-ea-async(final int companyId) {

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
    }*/
}
