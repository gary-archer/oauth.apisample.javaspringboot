package com.mycompany.sample.logic.errors;

/*
 * Error codes specific to the sample API
 */
@SuppressWarnings("PMD.ClassNamingConventions")
public final class SampleErrorCodes {

    public static final String INVALID_COMPANY_ID = "invalid_company_id";

    public static final String COMPANY_NOT_FOUND = "company_not_found";

    public static final String FILE_READ_ERROR = "file_read_error";

    private SampleErrorCodes() {
    }
}
