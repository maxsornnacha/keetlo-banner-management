package com.keetlo.banner_management.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class Authentication {
    @Value("${jtw.secret.key}")
    private String JWT_SECRET_KEY;

    //Constructor
    public Authentication(){}

    public String createToken(String userCode) {
        Date expirationTime = new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24);
        String createdToken = JWT.create()
                .withSubject(userCode)
                .withIssuedAt(new Date())
                .withExpiresAt(expirationTime)
                .withClaim("loginTime", System.currentTimeMillis())
                .sign(Algorithm.HMAC256(JWT_SECRET_KEY));
        return  createdToken;
    }

    public  String verifyToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(JWT_SECRET_KEY)).build();
        DecodedJWT decodedJWT = verifier.verify(token);
        if(decodedJWT.getSubject() == null){
            return  null;
        }
        return decodedJWT.getSubject();
    }
}
