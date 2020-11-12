package com.monkeyk.sos.config;


import com.monkeyk.sos.domain.oauth.CustomJdbcClientDetailsService;
import com.monkeyk.sos.service.OauthService;
import com.monkeyk.sos.service.UserService;
import com.monkeyk.sos.web.oauth.OauthUserApprovalHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.approval.UserApprovalHandler;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

import javax.sql.DataSource;

/**
 * 2018/2/8
 * <p>
 * <p>
 * OAuth2 config
 *
 * @author Shengzhao Li
 */
@Configuration
public class OAuth2ServerConfiguration {


    /*Fixed,  resource-id */
    public static final String RESOURCE_ID = "sos-resource";


    /**
     * // unity resource
     * UNITY 资源的访问权限配置
     */
    @Configuration
    @EnableResourceServer
    protected static class UnityResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) {
            resources.resourceId(RESOURCE_ID).stateless(false);
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                    // Since we want the protected resources to be accessible in the UI as well we need
                    // session creation to be allowed (it's disabled by default in 2.0.6)
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .and()
                    // 所有以 /unity/  开头的 URL属于此资源
                    .requestMatchers().antMatchers("/unity/**")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/unity/**").access("#oauth2.hasScope('read') and hasRole('UNITY')");

        }

    }


    /**
     * // mobile resource
     * MOBILE 资源的访问权限配置
     */
    @Configuration
    @EnableResourceServer
    protected static class MobileResourceServerConfiguration extends ResourceServerConfigurerAdapter {

        @Override
        public void configure(ResourceServerSecurityConfigurer resources) {
            resources.resourceId(RESOURCE_ID).stateless(false);
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                    // Since we want the protected resources to be accessible in the UI as well we need
                    // session creation to be allowed (it's disabled by default in 2.0.6)
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .and()
                    // 所有以 /m/  开头的 URL属于此资源
                    .requestMatchers().antMatchers("/m/**")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/m/**").access("#oauth2.hasScope('read') and hasRole('MOBILE')");

        }

    }

    @Configuration
    @EnableAuthorizationServer
    protected static class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {


        @Autowired
        private TokenStore tokenStore;

        @Autowired
        private DefaultTokenServices tokenServices;


        @Autowired
        private ClientDetailsService clientDetailsService;


        @Autowired
        private OauthService oauthService;


        @Autowired
        private AuthorizationCodeServices authorizationCodeServices;


        @Autowired
        private UserService userDetailsService;


        @Autowired
        @Qualifier("authenticationManagerBean")
        private AuthenticationManager authenticationManager;


        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {

            clients.withClientDetails(clientDetailsService);
        }


//        /*
//         * JDBC TokenStore
//         */
//        @Bean
//        public TokenStore tokenStore(DataSource dataSource) {
//            return new JdbcTokenStore(dataSource);
//        }

        /*
         * Redis TokenStore (有Redis场景时使用)
         */
//        @Bean
//        public TokenStore tokenStore(RedisConnectionFactory connectionFactory) {
//            final RedisTokenStore redisTokenStore = new RedisTokenStore(connectionFactory);
//            //prefix
//            redisTokenStore.setPrefix(RESOURCE_ID);
//            return redisTokenStore;
//        }


        @Bean
        public ClientDetailsService clientDetailsService(DataSource dataSource) {
            return new CustomJdbcClientDetailsService(dataSource);
        }


        @Bean
        public AuthorizationCodeServices authorizationCodeServices(DataSource dataSource) {
            return new JdbcAuthorizationCodeServices(dataSource);
        }


        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
            endpoints.tokenServices(tokenServices)
                    .tokenStore(tokenStore)
                    .authorizationCodeServices(authorizationCodeServices)
                    .userDetailsService(userDetailsService)
                    .userApprovalHandler(userApprovalHandler())
                    .authenticationManager(authenticationManager);
        }

        @Override
        public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
            // real 值可自定义
            oauthServer
                    .tokenKeyAccess("permitAll()")
                    .checkTokenAccess("permitAll()")
//            oauthServer.realm("spring-oauth-server")
                    // 支持 client_credentials 的配置
                    .allowFormAuthenticationForClients();
        }

        @Bean
        public OAuth2RequestFactory oAuth2RequestFactory() {
            return new DefaultOAuth2RequestFactory(clientDetailsService);
        }


        @Bean
        public UserApprovalHandler userApprovalHandler() {
            OauthUserApprovalHandler userApprovalHandler = new OauthUserApprovalHandler();
            userApprovalHandler.setOauthService(oauthService);
            userApprovalHandler.setTokenStore(tokenStore);
            userApprovalHandler.setClientDetailsService(this.clientDetailsService);
            userApprovalHandler.setRequestFactory(oAuth2RequestFactory());
            return userApprovalHandler;
        }

    }


}
