package com.mycompany.api.basicapi.entities;

import java.util.List;

/*
 * A composite entity for a company's transactions
 */
public class CompanyTransactions {

    public Integer id;

    public Company company;

    public List<Transaction> transactions;
}
