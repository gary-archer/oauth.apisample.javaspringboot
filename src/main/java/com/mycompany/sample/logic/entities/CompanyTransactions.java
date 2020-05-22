package com.mycompany.sample.logic.entities;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/*
 * A composite entity for a company's transactions
 */
public class CompanyTransactions {

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private Company company;

    @Getter
    @Setter
    private List<Transaction> transactions;
}
