package com.example.userservice.config;

import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestLogin;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;


public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    // 이게 filter가 빈이 아니라서 autowired로 주입이 안돼 -> 직접 생성자로 주입받아야함
    private final UserService userService;
    private final Environment env;


    public AuthenticationFilter(AuthenticationManager authenticationManager,
                                UserService userService,
                                Environment env) {
        super(authenticationManager);
        this.userService = userService;
        this.env = env;
    }

    // 이거 재정의해야하는 이유
    // 시큐리티는 기존의 formLogin으로 세팅되어있어서 이렇게 안해도 된다.
    // 하지만 api 통신은 json통신이므로 내가 받은 json을 커스텀해서 토큰으로 넣어줘야한다.
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        try {
            // http request의 body로 넘어온 내용은 getinputstream으로 읽을 수 있음, objectmapper로 jason 읽기
            RequestLogin creds = new ObjectMapper().readValue(request.getInputStream(), RequestLogin.class);

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            creds.getEmail(),
                            creds.getPassword(),
                            new ArrayList<>()
                    )
            );
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }


    // 화면단을 가지지 않는 rest api 개발
    // 로그인 페이지를 응답할 필요가 없으므로 재정의가 필요함
    // 여기서 userentity에서 왜 id뿐만 아니라 uuid를 사용한 userid를 추가한 이유가 나옴
    // uuid의 목적은 고유한 식별자를 갖기 위함
    // 분산 환경이나 동시성이 중요되는 작업시에 다음과 같은 상황이 발생한다고 가정
    // db에서 각 레코드들의 ID를 한곳에서 관리하지 않음, 여러 구성 요소 및 서비스들이 고유하지 않은 식별자를 독립적으로 생성할 가능성
    // 여기서 고유한 키를 뽑아내는 가장 쉬운 것이 uuid
    // 물론 DB를 하나만 사용하는 경우라면 uuid를 사용할 필요없이 id나, 고유한 email값등을 사용해도 된다.
    // 하지만 여러 DB를 사용하는 분산된 환경에서 자동 증가된 id컬럼으로 구분하게 되면
    // 서로 다른 레코드에서 같은 id를 사용할 확률이 발생 -> 이런 경우 자동 증가 컬럼 이외의 id생성 로직이 필요
    // 즉, 모든 레코드들이 고유한 id를 위해서 email과 같은 고유한 컬럼을 갖지는 않음
    // 지금 경우에는 user을 하나의 db에서 관리하므로 id로만 해도 무관하지만 일반적인 상황을 고려했을때는
    // uuid로 따로 id를 만들어주는게 좋음
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        // 토큰에서 principle로 박혀있은 user은 username이랑 비번뿐이 없다.
        // userid로 토큰 만들꺼니까 db에서 유저 다시 찾아와야함
        String username = ((User) authResult.getPrincipal()).getUsername();
        UserDto userDto = userService.getUserDetailsByEmail(username);

        String token = Jwts.builder()
                .setSubject(userDto.getUserId()) // userid로 토큰 만듬
                .setExpiration(new Date(System.currentTimeMillis()+
                        Long.parseLong(env.getProperty("token.expiration_time")))) // yml은 문자로 가져오므로 파싱
                .signWith(SignatureAlgorithm.HS512,env.getProperty("token.secret")) // 암호화알고리즘 방식 + 키조합
                .compact();

        response.addHeader("token",token); // 헤더에 토큰 추가
        // 토큰이 정상적으로 만들어졌는지 나중에 decode할때 비교하기 위해서 userid 추가
        response.addHeader("userId",userDto.getUserId());
    }
}






















