package com.authsamples.api.logic.services;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import com.authsamples.api.logic.claims.CustomClaimNames;
import com.authsamples.api.logic.claims.SampleExtraClaims;
import com.authsamples.api.logic.entities.Company;
import com.authsamples.api.logic.entities.CompanyTransactions;
import com.authsamples.api.logic.errors.SampleErrorCodes;
import com.authsamples.api.logic.repositories.CompanyRepository;
import com.authsamples.api.plumbing.claims.ClaimsPrincipalHolder;
import com.authsamples.api.plumbing.claims.ClaimsReader;
import com.authsamples.api.plumbing.errors.ClientError;
import com.authsamples.api.plumbing.errors.ErrorFactory;

/*
 * The service class applies business authorization
 */
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@SuppressWarnings(value = "checkstyle:DesignForExtension")
public class CompanyService {

    private final CompanyRepository repository;
    private final ClaimsPrincipalHolder claimsHolder;

    /*
     * The claims holder may be injected into the service before OAuth processing
     * The OAuth filter then runs before any methods are called
     */
    public CompanyService(final CompanyRepository repository, final ClaimsPrincipalHolder claimsHolder) {
        this.repository = repository;
        this.claimsHolder = claimsHolder;
    }

    /*
     * Get a collection and filter on authorized items
     */
    public List<Company> getCompanyList() {

        var companies = this.repository.getCompanyList();
        return companies.stream()
            .filter(this::isUserAuthorizedForCompany)
            .collect(Collectors.toList());
    }

    /*
     * Get an individual object and deny access to unauthorized items
     */
    public CompanyTransactions getCompanyTransactions(final int companyId) {

        var transactions = this.repository.getCompanyTransactions(companyId);

        if (transactions == null || !this.isUserAuthorizedForCompany(transactions.getCompany())) {
            throw this.unauthorizedError(companyId);
        }

        return transactions;
    }

    /*
     * A simple example of applying domain specific claims to items
     */
    private boolean isUserAuthorizedForCompany(final Company company) {

        var claims = this.claimsHolder.getClaims();
        var role = ClaimsReader.getStringClaim(claims.getJwtClaims(), CustomClaimNames.Role).toUpperCase();

        // The admin role is granted access to all resources
        if (role.equals("ADMIN")) {
            return true;
        }

        // Unknown roles are granted no access to resources
        if (!role.equals("USER")) {
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
