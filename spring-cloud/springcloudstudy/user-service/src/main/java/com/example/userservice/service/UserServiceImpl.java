package com.example.userservice.service;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.domain.UserEntity;
import com.example.userservice.dto.UserDto;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.vo.ResponseOrder;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService{

    private final UserRepository repository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
    private final Environment env;
    private final OrderServiceClient orderServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory; // 의존성 추가되면 자동 빈 등록되어있음

    @Override
    public UserDto createUser(UserDto userDto) {
        userDto.setUserId(UUID.randomUUID().toString());

        UserEntity userEntity = modelMapper.map(userDto, UserEntity.class);
        userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));
        UserEntity savedUser = repository.save(userEntity);
        UserDto savedUserDto = modelMapper.map(savedUser, UserDto.class);
        return savedUserDto;
    }

    @Override
    public UserDto getUserByUserId(String userId) {
        UserEntity userEntity = repository.findByUserId(userId);

        if(userEntity == null){
            throw new UsernameNotFoundException("user not found");
        }

        UserDto userDto = modelMapper.map(userEntity, UserDto.class);
//        List<ResponseOrder> orders = new ArrayList<>();

        // resttemplate 사용하는 방식
        // 마이크로서비스간 통신
        // 그냥 각 서비스에서 필요한 서비스의 url를 resttemplate을 사용해서 호출해서
        // 응답 값을 사용한다.

//        String orderUrl = String.format(env.getProperty("order_service.url"),userId);
//        // url, method, 요청 파라미터, 반환값 받을 형식(그쪽 컨트롤러의 반환값이랑 맞추면 된다)
//        ResponseEntity<List<ResponseOrder>> orderListResponse = restTemplate.exchange(orderUrl, HttpMethod.GET, null,
//                new ParameterizedTypeReference<List<ResponseOrder>>() {
//                });
//        List<ResponseOrder> orderList = orderListResponse.getBody();

        /* feign client 사용 */
        //List<ResponseOrder> order = orderServiceClient.getOrder(userId);

        /* circuitbreaker 사용*/
        log.info("before call order micro service");
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create("circuitbreaker");// 인자는 그냥 서킷브레이커의 이름을 준것
        // run 안에서 동작 수행
        // 뽑으면 ?로 제네릭 표시되는데 feign client 반환값으로 적어주도록하자
        List<ResponseOrder> order = circuitbreaker.run(
                () -> orderServiceClient.getOrder(userId),
                throwable -> new ArrayList<>() // 문제 발생시 빈 배열 반환
        );
        log.info("after call order micro service");

        userDto.setOrders(order);


        return userDto;
    }

    @Override
    public List<UserEntity> getUserByAll() {
        return repository.findAll();
    }

    @Override
    public UserDto getUserDetailsByEmail(String email) {
        UserEntity userEntity = repository.findByEmail(email);

        if (userEntity == null)
            throw new UsernameNotFoundException(email);

        UserDto userDto = modelMapper.map(userEntity, UserDto.class);
        return userDto;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = repository.findByEmail(username);

        if(userEntity == null)
            throw new UsernameNotFoundException(username);

        return new User(userEntity.getEmail(),userEntity.getEncryptedPwd(),
                true,true,true,true,
                new ArrayList<>());
    }
}
