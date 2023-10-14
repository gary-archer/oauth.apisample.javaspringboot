package com.mycompany.sample.logic.repositories;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import com.mycompany.sample.logic.entities.Company;
import com.mycompany.sample.logic.entities.CompanyTransactions;
import com.mycompany.sample.logic.utilities.JsonFileReader;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.mycompany.sample.plumbing.logging.LogEntry;
import com.mycompany.sample.plumbing.logging.PerformanceBreakdown;

/*
 * The repository to load data
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

        return this.jsonReader.readFile("data/companyList.json", Company[].class)
            .handle((data, ex) -> {

                // End the performance breakdown
                breakdown.close();

                // Handle read errors
                if (ex != null) {
                    throw ErrorUtils.fromException(ex);
                }

                // Return the result
                return Arrays.stream(data).collect(Collectors.toList());
            });
    }

    /*
     * Read the transactions from a database, which does 2 async reads surrounded by a performance breakdown
     */
    public CompletableFuture<CompanyTransactions> getCompanyTransactions(final int companyId) {

        // Start recording the time taken for data access
        var breakdown = this.logEntry.createPerformanceBreakdown("getCompanyTransactions");

        // Do the async work, which is a little unreadable in Java, due to a missing await keyword
        return this.jsonReader.readFile("data/companyList.json", Company[].class)
                .handle((companies, companiesException) ->
                        this.getAndFilterCompanies(
                                companyId,
                                companies,
                                breakdown,
                                companiesException))
                .thenCompose(foundCompany ->
                        this.jsonReader.readFile("data/companyTransactions.json", CompanyTransactions[].class)
                        .handle((transactions, transactionsException) ->
                                this.getAndFilterTransactions(
                                        companyId,
                                        foundCompany,
                                        transactions,
                                        breakdown,
                                        transactionsException)));
    }

    /*
     * Run the first async operation
     */
    private Company getAndFilterCompanies(
            final int companyId,
            final Company[] companiesData,
            final PerformanceBreakdown breakdown,
            final Throwable ex) {

        // End the performance breakdown for the case when there is an error during the companies read
        if (ex != null) {
            breakdown.close();
            throw ErrorUtils.fromException(ex);
        }

        // Find the requested company and return it
        var companies = Arrays.stream(companiesData).toList();
        var found = companies.stream().filter(c -> c.getId() == companyId).findFirst();
        return found.orElse(null);
    }

    /*
     * Run the next async operation, using the results of the first
     */
    private CompanyTransactions getAndFilterTransactions(
            final int companyId,
            final Company foundCompany,
            final CompanyTransactions[] transactionsData,
            final PerformanceBreakdown breakdown,
            final Throwable ex) {

        // End the performance breakdown for the case when there is an error during the transactions read
        if (ex != null) {
            breakdown.close();
            throw ErrorUtils.fromException(ex);
        }

        CompanyTransactions result = null;
        if (foundCompany != null) {

            // Find the transactions for the requested company
            Optional<CompanyTransactions> foundTransactions =
                    Arrays.stream(transactionsData).filter(t -> t.getId() == companyId).findFirst();
            if (foundTransactions.isPresent()) {

                // Return the result for the success case
                result = foundTransactions.get();
                result.setCompany(foundCompany);
            }
        }

        breakdown.close();
        return result;
    }
}
