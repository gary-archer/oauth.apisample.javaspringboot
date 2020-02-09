package com.mycompany.sample.logic.entities;

import lombok.Getter;
import lombok.Setter;

/*
 * A simple company entity
 */
public class Company {

    @Getter
    @Setter
    private int id;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String region;

    @Getter
    @Setter
    private Double targetUsd;

    @Getter
    @Setter
    private Double investmentUsd;

    @Getter
    @Setter
    private int noInvestors;
}
