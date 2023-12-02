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
import com.mycompany.sample.logic.claims.SampleClaimsPrincipal;
import com.mycompany.sample.logic.claims.SampleExtraClaims;
import com.mycompany.sample.logic.entities.Company;
import com.mycompany.sample.logic.entities.CompanyTransactions;
import com.mycompany.sample.logic.errors.SampleErrorCodes;
import com.mycompany.sample.logic.repositories.CompanyRepository;
import com.mycompany.sample.plumbing.claims.ClaimsPrincipalHolder;
import com.mycompany.sample.plumbing.errors.ClientError;
import com.mycompany.sample.plumbing.errors.ErrorFactory;

/*
 * The service class applies business authorization
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class CompanyService {

    private final CompanyRepository repository;
    private final ClaimsPrincipalHolder claimsHolder;

    public CompanyService(final CompanyRepository repository, final ClaimsPrincipalHolder claimsHolder) {
        this.repository = repository;
        this.claimsHolder = claimsHolder;
    }

    /*
     * Get a collection and filter on authorized items
     */
    public CompletableFuture<List<Company>> getCompanyList() {

        return this.repository.getCompanyList().thenCompose(data ->
                completedFuture(data.stream()
                    .filter(this::isUserAuthorizedForCompany)
                    .collect(Collectors.toList())));
    }

    /*
     * Get an individual object and deny access to unauthorized items
     */
    public CompletableFuture<CompanyTransactions> getCompanyTransactions(final int companyId) {

        return this.repository.getCompanyTransactions(companyId).thenCompose(data -> {

            if (data == null || !this.isUserAuthorizedForCompany(data.getCompany())) {
                throw this.unauthorizedError(companyId);
            }

            return completedFuture(data);
        });
    }

    /*
     * A simple example of applying domain specific claims to items
     */
    private boolean isUserAuthorizedForCompany(final Company company) {

        var claims = (SampleClaimsPrincipal) this.claimsHolder.getClaims();

        // The admin role is granted access to all resources
        var isAdmin = claims.getRole().equalsIgnoreCase("admin");
        if (isAdmin) {
            return true;
        }

        // Unknown roles are granted no access to resources
        var isUser = claims.getRole().equalsIgnoreCase("user");
        if (!isUser) {
            return false;
        }

        // For the user role, authorize based on a business rule that links the user to regional data
        var extraClaims = (SampleExtraClaims) claims.getExtraClaims();
        return Arrays.stream(extraClaims.getRegions()).anyMatch(ur -> ur.equals(company.getRegion()));
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
