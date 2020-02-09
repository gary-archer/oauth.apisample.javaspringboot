package com.mycompany.sample.logic.entities;

import lombok.Getter;
import lombok.Setter;

/*
 * A simple transaction entity
 */
public class Transaction {

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private String investorId;

    @Getter
    @Setter
    private Double amountUsd;
}
