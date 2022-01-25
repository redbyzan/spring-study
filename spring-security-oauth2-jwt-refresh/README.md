## spring security with OAuth2, JWT, native Login

+ 회원가입
    + OAuth2 - 카카오, 구글, 네이버
        + 핵심 로직은 CustomOauth2Service.class 참고
    + 일반 회원
        + 핵심 로직은 CustomUserDetailsService.class 참고
+ 로그인 과정은 백엔드 서버와 JWT 토큰으로 통신  
    + AccessToken, RefreshToken 제공
    + JWT 토큰은 TokenAuthenticationFilter 에서 처리
    + 일반 로그인은 LoginFilter.class, 소셜 로그인은 CustomOauth2Service.class 에서 처리되고 둘다 LoginSuccessHandler 를 통해 response 값에 JWT 토큰이 담겨서 프론트로 응답    
+ 일반 security와 OAuth2에서 제공하는 각각의 서비스에서는 UserDetails와 OAuth2User를 반환해야 하는데 MemberPrincipal.class에서 이를 구현하여 하나로 통일하여 사용
    
    

    
     
