package com.apistudy.restapi.config;


import com.apistudy.restapi.accounts.AccountService;
import com.apistudy.restapi.common.AppProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@EnableAuthorizationServer // auth 서버 설정
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    PasswordEncoder passwordEncoder;

    // 빈설정에서 다른 곳에서 사용할 수 있도록 빈으로 등록해놨음
    // AuthenticationManager는 유저인증 정보를 가지고 있음
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    AccountService accountService;

    // 이전 시큐리티 config에 빈 등록 해줬음 가져온것
    @Autowired
    TokenStore tokenStore;

    @Autowired
    AppProperties appProperties;

    // configure 3개만 override 하면 된다.

    // 시큐리티에서는 패스워드 인코더 설정
    // 클라이언트의 시크릿를 검증할때 패스워드 인코더 사용
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.passwordEncoder(passwordEncoder);
    }

    // 클라이언트 설정
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        // 이건 인메모리도 유저를 등록한거나 마찬가지인데
        // 이거 말고 jdbc를 사용해서 db에 저장하는게 좋다
        clients.inMemory()
                .withClient(appProperties.getClientId())
                // 이 인증 서버가 지원할 grant 타입 설정
                // refresh token은 oauth 토큰을 발급받을 때 refresh 토큰도 같이 발급해주는데
                // 이걸 가지고 새로운 access 토큰을 발급받는 그런 타입이다.
                .authorizedGrantTypes("password","refresh_token")
                .scopes("read","write") // 앱에서 정의하기 나름,
                .secret(passwordEncoder.encode(appProperties.getClientSecret())) // 이 앱의 시크릿
                .accessTokenValiditySeconds(10*60) // 토큰 유효 시간
                .refreshTokenValiditySeconds(6*10*60);
    }

    // endpoint는 authenticationmanager, token store, userdetails를 설정할 수 있따.
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(authenticationManager) // 우리의 인증정보를 알고있는 authenticationManager로 설정
                .userDetailsService(accountService) // 우리가 만든 서비스로 설정
                .tokenStore(tokenStore);
    }
}
