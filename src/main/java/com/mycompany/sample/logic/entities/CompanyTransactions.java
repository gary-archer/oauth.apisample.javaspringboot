package com.mycompany.sample.logic.entities;

import java.util.List;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.mycompany.sample.plumbing.utilities.UnderscorePropertyNamingStrategy;
import lombok.Getter;
import lombok.Setter;

/*
 * A composite entity for a company's transactions
 */
@JsonNaming(UnderscorePropertyNamingStrategy.class)
public class CompanyTransactions {

    @Getter
    @Setter
    private int _id;

    @Getter
    @Setter
    private Company _company;

    @Getter
    @Setter
    private List<Transaction> _transactions;
}
