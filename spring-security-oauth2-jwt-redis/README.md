## spring security with OAuth2, JWT, Redis 

+ 회원가입 - OAuth2 - 카카오, 구글, 네이버
    + 핵심 로직은 CustomOauth2Service.class 참고하세요.
    + 요청은 /oauth2/authorization/google 또는 naver 또는 kakao가 기본값이지만 SecurityConfig.class에서 baseUri를 "/api/oauth2/authorization" 으로 설정했으므로 /api/oauth2/authorization/google , naver, kakao로 요청을 보내야 합니다.
+ 로그인에서 JWT통신을 받고 이후부터는 JWT토큰으로 통신합니다.  
    + AccessToken, RefreshToken 제공합니다.
    + JWT 토큰은 TokenAuthenticationFilter 에서 처리합니다.
    + 소셜 로그인은 CustomOauth2Service.class 에서 처리되고 LoginSuccessHandler 를 통해 response 값에 JWT 토큰이 담겨서 프론트로 응답합니다.
+ OAuth2에서 제공하는 서비스에서 OAuth2User를 반환해야 하는데 UserPrincipal.class에서 이를 구현하여 사용합니다.
+ Logout시 JWT accessToken과 refreshToken을 받아서 redis에 해당 토큰의 남은 시간을 TTL로 세팅하고 저장합니다.
    + JWT 로그인 처리를 진행하는 TokenAuthenticationFilter.class 에서 해당 토큰으로 로그인 시도하는 요청을 막습니다.
+ 인증 실패 시 RestAuthenticationEntryPoint.class 에서 처리합니다.
+ 인가 실패 시 RestAccessDeniedHandler.class 에서 처리합니다.
+ TokenAuthenticationFilter.class 에서 터지는 예외는 TokenAuthenticationErrorFilter에서 처리합니다.

<br>

+ 모든 코드에 대한 테스트 코드가 작성되어 있습니다.
+ Spring rest docs로 문서화되어 있습니다. 
    - build 후 build/docs/asciidoc/index.html에서 확인 가능합니다.
    - 문서화 방법은 [여기](https://backtony.github.io/spring/2021-10-15-spring-test-3/)을 참고하세요.



    
     
