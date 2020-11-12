package com.monkeyk.sos.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

/**
 * 2020/6/9
 * <p>
 * <p>
 * JDBC TokenStore config
 *
 * @author Shengzhao Li
 * @since 2.1.0
 */
@Configuration
@ConditionalOnProperty(name = "sos.token.store", havingValue = "jdbc", matchIfMissing = true)
public class JdbcTokenStoreConfiguration {


    /**
     * JDBC TokenStore
     */
    @Bean
    public TokenStore tokenStore(DataSource dataSource) {
        return new JdbcTokenStore(dataSource);
    }


    @Bean
    public DefaultTokenServices tokenServices(TokenStore tokenStore, ClientDetailsService clientDetailsService) {
        DefaultTokenServices tokenServices = new DefaultTokenServices();
        tokenServices.setTokenStore(tokenStore);
        tokenServices.setClientDetailsService(clientDetailsService);
        //support refresh token
        tokenServices.setSupportRefreshToken(true);
        return tokenServices;
    }

}
