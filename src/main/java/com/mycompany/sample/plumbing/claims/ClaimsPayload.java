package com.mycompany.sample.plumbing.claims;

import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;

/*
 * A simple wrapper for the claims in a decoded JWT or introspection / user info response
 */
public final class ClaimsPayload {

    private final Object claims;

    @Getter
    @Setter
    private BiFunction<Object, String, String> stringClaimCallback;

    @Getter
    @Setter
    private Function<Object, Long> expirationClaimCallback;

    public ClaimsPayload(final Object claims) {

        System.out.println(claims.toString());

        this.claims = claims;
        this.stringClaimCallback = null;
        this.expirationClaimCallback = null;
    }

    public String getStringClaim(final String key) {

        if (this.stringClaimCallback == null) {
            throw new RuntimeException("stringClaimCallback is null in the ClaimsPayload class");
        }

        return this.stringClaimCallback.apply(this.claims, key);
    }

    public int getExpirationClaim() {

        if (this.expirationClaimCallback == null) {
            throw new RuntimeException("expirationClaimCallback callback is null in the ClaimsPayload class");
        }

        return this.expirationClaimCallback.apply(this.claims).intValue();
    }
}
