package com.authsamples.api.logic.repositories;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Repository;
import com.authsamples.api.logic.entities.Company;
import com.authsamples.api.logic.entities.CompanyTransactions;
import com.authsamples.api.logic.utilities.JsonFileReader;
import com.authsamples.api.plumbing.logging.LogEntry;

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
    public List<Company> getCompanyList() {

        try (var breakdown = this.logEntry.createPerformanceBreakdown("getCompanyList")) {

            var companies = this.jsonReader.readFile("data/companyList.json", Company[].class);
            return Arrays.stream(companies).collect(Collectors.toList());
        }
    }

    /*
     * Read the transactions from a database, which does 2 async reads surrounded by a performance breakdown
     */
    public CompanyTransactions getCompanyTransactions(final int companyId) {

        try (var breakdown = this.logEntry.createPerformanceBreakdown("getCompanyTransactions")) {

            var companies = this.jsonReader.readFile("data/companyList.json", Company[].class);
            var foundCompany = this.getAndFilterCompanies(companyId, companies);

            var transactions = this.jsonReader.readFile("data/companyTransactions.json", CompanyTransactions[].class);
            return this.getAndFilterTransactions(companyId, foundCompany, transactions);
        }
    }

    /*
     * Find and return the requested company
     */
    private Company getAndFilterCompanies(final int companyId, final Company[] companiesData) {

        var companies = Arrays.stream(companiesData).toList();
        var found = companies.stream().filter(c -> c.getId() == companyId).findFirst();
        return found.orElse(null);
    }

    /*
     * Find and return transactions for the found company
     */
    private CompanyTransactions getAndFilterTransactions(
            final int companyId,
            final Company foundCompany,
            final CompanyTransactions[] transactionsData) {

        CompanyTransactions result = null;
        if (foundCompany != null) {

            Optional<CompanyTransactions> foundTransactions =
                    Arrays.stream(transactionsData).filter(t -> t.getId() == companyId).findFirst();
            if (foundTransactions.isPresent()) {

                result = foundTransactions.get();
                result.setCompany(foundCompany);
            }
        }

        return result;
    }
}
