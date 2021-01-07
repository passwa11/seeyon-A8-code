package com.monkeyk.sos.config;

import com.monkeyk.sos.handler.CustomLoginSuccessHandler;
import com.monkeyk.sos.handler.CustomLogoutSuccessHandler;
import com.monkeyk.sos.service.UserService;
import com.monkeyk.sos.util.OaPasswordEncode;
import com.monkeyk.sos.web.context.SOSContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 2016/4/3
 * <p/>
 * Replace security.xml
 *
 * @author Shengzhao Li
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {


    @Autowired
    private UserService userService;


    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        //Ignore, public
        web.ignoring().antMatchers("/public/**", "/static/**");
    }

    //退出成功处理类
    @Autowired
    private CustomLogoutSuccessHandler customLogoutSuccessHandler;

    //登陆成功处理类
    @Autowired
    private CustomLoginSuccessHandler customLoginSuccessHandler;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().ignoringAntMatchers("/oauth/authorize", "/oauth/token", "/oauth/rest_token");

        http.authorizeRequests()
                // permitAll() 的URL路径属于公开访问，不需要权限
                .antMatchers("/public/**").permitAll()
                .antMatchers("/static/**").permitAll()
                .antMatchers("/oauth/rest_token*").permitAll()
                .antMatchers("/login*").permitAll()
//                .antMatchers("/logout").permitAll()
//                .antMatchers("/myLogout").permitAll()

                // /user/ 开头的URL需要 ADMIN 权限
//                .antMatchers("/user/**").hasAnyRole("ADMIN")
                .antMatchers("/user/**").permitAll()

                .antMatchers(HttpMethod.GET, "/login*").anonymous()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .loginProcessingUrl("/signin")
                .failureUrl("/login?error=1")
                .usernameParameter("oidc_user")
                .passwordParameter("oidcPwd")
                //zhou:登陆成功处理类
//                .successHandler(customLoginSuccessHandler)
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .logoutSuccessHandler(customLogoutSuccessHandler)
                .deleteCookies("JSESSIONID")

//                //无效会话
//                .invalidateHttpSession(true)
//                // 清除身份验证
//                .clearAuthentication(true)
//                .permitAll()
                //zhou
//                .logoutUrl("/signout")
//                .addLogoutHandler(new MyLogoutHandler())
//                .logoutSuccessUrl("/")
                .and()
                .exceptionHandling()
                // 基于token，所以不需要session
//                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                //禁用csrf
                .and().csrf().disable();

        http.authenticationProvider(authenticationProvider());
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }


    /**
     * BCrypt  加密
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        //return new BCryptPasswordEncoder();
        return new OaPasswordEncode();
    }


    /**
     * SOSContextHolder bean
     *
     * @return SOSContextHolder bean
     * @since 2.0.1
     */
    @Bean
    public SOSContextHolder sosContextHolder() {
        return new SOSContextHolder();
    }
}
