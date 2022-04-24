package com.mycompany.sample.logic.repositories;

import java.io.Closeable;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import com.mycompany.sample.logic.entities.Company;
import com.mycompany.sample.logic.entities.CompanyTransactions;
import com.mycompany.sample.logic.utilities.JsonFileReader;
import com.mycompany.sample.plumbing.errors.ErrorUtils;
import com.mycompany.sample.plumbing.logging.PerformanceBreakdown;

/*
 * A dedicated class for encapsulating multiple async calls
 */
public final class GetTransactionsOperation implements Closeable {

    private final JsonFileReader jsonReader;
    private final int companyId;
    private final PerformanceBreakdown breakdown;
    private Company foundCompany;

    public GetTransactionsOperation(
            final JsonFileReader jsonReader,
            final int companyId,
            final PerformanceBreakdown breakdown) {

        this.jsonReader = jsonReader;
        this.companyId = companyId;
        this.breakdown = breakdown;
        this.foundCompany = null;
    }

    /*
     * Run the first async operation, do intermediate work, then run the next async operation
     */
    public CompletableFuture<CompanyTransactions> execute() {

        return this.jsonReader.readFile("data/companyList.json", Company[].class)
                .handle(this::getAndFilterCompanies)
                .thenCompose(company -> {
                    this.foundCompany = company;
                    return this.jsonReader.readFile("data/companyTransactions.json", CompanyTransactions[].class)
                            .handle(this::getAndFilterTransactions);
                });
    }

    /*
     * Dispose the performance breakdown on completion
     */
    @Override
    public void close() {
        this.breakdown.close();
    }

    /*
     * Run the first async operation
     */
    public Company getAndFilterCompanies(final Company[] companiesData, final Throwable ex) {

        // End the performance breakdown when there is a companies read error
        if (ex != null) {
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
    public CompanyTransactions getAndFilterTransactions(
            final CompanyTransactions[] transactionsData,
            final Throwable ex) {

        // End the performance breakdown for the error case during transaction reads
        if (ex != null) {
            breakdown.close();
            throw ErrorUtils.fromException(ex);
        }

        // End the performance breakdown when no companies are found
        if (this.foundCompany == null) {
            return null;
        }

        // Find the transactions for the requested company
        Optional<CompanyTransactions> foundTransactions =
                Arrays.stream(transactionsData).filter(t -> t.getId() == companyId).findFirst();
        if (foundTransactions.isPresent()) {

            // Return the result for the success case
            var result = foundTransactions.get();
            result.setCompany(this.foundCompany);
            return result;
        }

        // Return null when transactions are not found
        return null;
    }
}
