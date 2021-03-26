package com.mycompany.sample.plumbing.claims;

import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;

/*
 * A simple container for a claims payload, until we are ready to read results
 */
public class ClaimsPayload {

    private final Object claims;

    @Getter
    @Setter
    private BiFunction<Object, String, String> stringClaimCallback;

    @Getter
    @Setter
    private Function<Object, Long> expirationClaimCallback;

    public ClaimsPayload(Object claims) {

        this.claims = claims;
        this.stringClaimCallback = null;
        this.expirationClaimCallback = null;
    }

    public String getStringClaim(String key) {

        if (this.stringClaimCallback != null) {
            return this.stringClaimCallback.apply(this.claims, key);
        }

        throw new RuntimeException("stringClaimCallback is null in the ClaimsPayload class");
    }

    public int getExpirationClaim() {

        if (this.expirationClaimCallback != null) {
            return this.expirationClaimCallback.apply(this.claims).intValue();
        }

        throw new RuntimeException("expirationClaimCallback callback is null in the ClaimsPayload class");
    }
}
