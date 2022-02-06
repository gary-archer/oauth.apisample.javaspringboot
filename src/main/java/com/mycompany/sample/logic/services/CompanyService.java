package com.mycompany.sample.logic.services;

import static java.util.concurrent.CompletableFuture.completedFuture;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import static com.ea.async.Async.await;
import com.mycompany.sample.logic.entities.Company;
import com.mycompany.sample.logic.entities.CompanyTransactions;
import com.mycompany.sample.logic.entities.SampleCustomClaims;
import com.mycompany.sample.logic.errors.SampleErrorCodes;
import com.mycompany.sample.logic.repositories.CompanyRepository;
import com.mycompany.sample.plumbing.claims.CustomClaims;
import com.mycompany.sample.plumbing.errors.ClientError;
import com.mycompany.sample.plumbing.errors.ErrorFactory;

/*
 * Our service layer class applies business authorization
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class CompanyService {

    private final CompanyRepository repository;
    private final SampleCustomClaims claims;

    public CompanyService(final CompanyRepository repository, final CustomClaims claims) {
        this.repository = repository;
        this.claims = (SampleCustomClaims) claims;
    }

    /*
     * Forward to the repository to get the company list
     */
    public CompletableFuture<List<Company>> getCompanyList() {

        // Use a micro services approach of getting all data
        var companies = await(this.repository.getCompanyList());

        // Filter on what the user is allowed to access
        return completedFuture(
                companies.stream()
                         .filter(this::isUserAuthorizedForCompany)
                         .collect(Collectors.toList()));
    }

    /*
     * Forward to the repository to get the company transactions
     */
    public CompletableFuture<CompanyTransactions> getCompanyTransactions(final int companyId) {

        // Deny access if required
        var data = await(this.repository.getCompanyTransactions(companyId));
        if (data == null || !this.isUserAuthorizedForCompany(data.getCompany())) {
            throw this.unauthorizedError(companyId);
        }

        return completedFuture(data);
    }

    /*
     * A simple example of applying domain specific claims
     */
    private boolean isUserAuthorizedForCompany(final Company company) {

        var isAdmin = this.claims.getUserRole().toLowerCase().contains("admin");
        if (isAdmin) {
            return true;
        }

        return Arrays.stream(this.claims.getUserRegions()).anyMatch(ur -> ur.equals(company.getRegion()));
    }

    /*
     * Return 404 for both not found items and also those that are not authorized
     */
    private ClientError unauthorizedError(final int companyId) {

        var message = String.format("Transactions for company %d were not found for this user", companyId);
        return ErrorFactory.createClientError(
                HttpStatus.NOT_FOUND,
                SampleErrorCodes.COMPANY_NOT_FOUND,
                message);
    }
}
