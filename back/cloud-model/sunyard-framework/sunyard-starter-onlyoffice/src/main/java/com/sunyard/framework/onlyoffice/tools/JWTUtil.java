package com.sunyard.framework.onlyoffice.tools;


import java.util.Map;

import org.primeframework.jwt.Signer;
import org.primeframework.jwt.domain.JWT;
import org.primeframework.jwt.hmac.HMACSigner;


/**
 * @author PJW
 * @BelongsProject: onlyoffice-demo
 * @BelongsPackage: com.oo.onlyoffice.tools
 */
public class JWTUtil {

    /**
     * 创建token
     * @param map map
     * @param secret secret
     * @return Result
     */
    public static String createToken(Map<String,Object> map,String secret){
            Signer signer = HMACSigner.newSHA256Signer(secret);
            JWT jwt = new JWT();
            for (String key : map.keySet()) {
                jwt.addClaim(key, map.get(key));
            }
        String encode = JWT.getEncoder().encode(jwt, signer);
        return encode;
    }
}
