package com.mycompany.sample.logic.entities;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.mycompany.sample.plumbing.utilities.UnderscorePropertyNamingStrategy;
import lombok.Getter;
import lombok.Setter;

/*
 * A simple company entity
 */
@JsonNaming(UnderscorePropertyNamingStrategy.class)
public class Company {

    @Getter
    @Setter
    private int _id;

    @Getter
    @Setter
    private String _name;

    @Getter
    @Setter
    private String _region;

    @Getter
    @Setter
    private Double _targetUsd;

    @Getter
    @Setter
    private Double _investmentUsd;

    @Getter
    @Setter
    private int _noInvestors;
}
