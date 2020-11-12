package com.monkeyk.sos.config;

import com.monkeyk.sos.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

/**
 * 2020/6/9
 * <p>
 * <p>
 * JWT TokenStore config
 *
 * @author Shengzhao Li
 * @since 2.1.0
 */
@Configuration
@ConditionalOnProperty(name = "sos.token.store", havingValue = "jwt")
public class JWTTokenStoreConfiguration {


    /**
     * HMAC key, default: IH6S2dhCEMwGr7uE4fBakSuDh9SoIrRa
     * alg: HMACSHA256
     */
    @Value("${sos.token.store.jwt.key:IH6S2dhCEMwGr7uE4fBakSuDh9SoIrRa}")
    private String jwtKey;


    @Bean
    public JwtAccessTokenConverter accessTokenConverter(UserService userService) {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();

        DefaultAccessTokenConverter tokenConverter = new DefaultAccessTokenConverter();
        DefaultUserAuthenticationConverter userAuthenticationConverter = new DefaultUserAuthenticationConverter();
        userAuthenticationConverter.setUserDetailsService(userService);
//        userAuthenticationConverter.setDefaultAuthorities(new String[]{"USER"});
        tokenConverter.setUserTokenConverter(userAuthenticationConverter);

        tokenConverter.setIncludeGrantType(true);
//        tokenConverter.setScopeAttribute("_scope");
        jwtAccessTokenConverter.setAccessTokenConverter(tokenConverter);

        jwtAccessTokenConverter.setSigningKey(this.jwtKey);
        return jwtAccessTokenConverter;
    }

    /**
     * JWT TokenStore
     *
     * @since 2.1.0
     */
    @Bean
    public TokenStore tokenStore(JwtAccessTokenConverter jwtAccessTokenConverter) {
        return new JwtTokenStore(jwtAccessTokenConverter);
    }


    @Bean
    public DefaultTokenServices tokenServices(TokenStore tokenStore, JwtAccessTokenConverter tokenEnhancer, ClientDetailsService clientDetailsService) {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(tokenStore);
        tokenServices.setClientDetailsService(clientDetailsService);
        //support refresh token
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setTokenEnhancer(tokenEnhancer);
        return tokenServices;
    }

}
