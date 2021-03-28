package com.mycompany.sample.logic.entities;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.mycompany.sample.plumbing.utilities.UnderscorePropertyNamingStrategy;
import lombok.Getter;
import lombok.Setter;

/*
 * A simple transaction entity
 */
@JsonNaming(UnderscorePropertyNamingStrategy.class)
public class Transaction {

    @Getter
    @Setter
    private int _id;

    @Getter
    @Setter
    private String _investorId;

    @Getter
    @Setter
    private Double _amountUsd;
}
