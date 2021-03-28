package com.mycompany.sample.plumbing.claims;

import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.Getter;
import lombok.Setter;

/*
 * A simple wrapper for the claims in a decoded JWT or introspection / user info response
 */
public final class ClaimsPayload {

    private final Object _claims;

    @Getter
    @Setter
    private BiFunction<Object, String, String> _stringClaimCallback;

    @Getter
    @Setter
    private BiFunction<Object, String, String[]> _stringArrayClaimCallback;

    @Getter
    @Setter
    private Function<Object, Long> _expirationClaimCallback;

    public ClaimsPayload(final Object claims) {

        this._claims = claims;
        this._stringClaimCallback = null;
        this._expirationClaimCallback = null;
    }

    public String getStringClaim(final String key) {

        if (this._stringClaimCallback == null) {
            throw new RuntimeException("stringClaimCallback is null in the ClaimsPayload class");
        }

        return this._stringClaimCallback.apply(this._claims, key);
    }

    public String[] getStringArrayClaim(final String key) {

        if (this._stringArrayClaimCallback == null) {
            throw new RuntimeException("stringArrayClaimCallback is null in the ClaimsPayload class");
        }

        return this._stringArrayClaimCallback.apply(this._claims, key);
    }

    public int getExpirationClaim() {

        if (this._expirationClaimCallback == null) {
            throw new RuntimeException("expirationClaimCallback callback is null in the ClaimsPayload class");
        }

        return this._expirationClaimCallback.apply(this._claims).intValue();
    }
}
