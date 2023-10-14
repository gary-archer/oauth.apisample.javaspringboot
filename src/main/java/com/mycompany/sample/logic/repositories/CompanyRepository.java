package com.mycompany.sample.logic.repositories;

import java.util.Arrays;
import java.util.List;
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

        // Since there are multiple async calls, use an operation class to wrap them
        var operation = new GetTransactionsOperation(this.jsonReader, companyId, breakdown);
        return operation.execute();
    }
}
