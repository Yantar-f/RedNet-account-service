package com.rednet.accountservice.util;

import com.rednet.accountservice.model.SystemTokenClaims;

public interface ApiTokenParser {
    SystemTokenClaims parseApiToken(String token);
}
