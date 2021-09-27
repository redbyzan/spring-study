package com.example.userservice.controller;

import com.example.userservice.common.GreetingProperties;
import com.example.userservice.domain.UserEntity;
import com.example.userservice.dto.UserDto;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {

    // 설정파일 값을 configurationproperties로 빼서 bean 등록해서 사용하는 것도 아는데
    // 새로운 방법도 있다. 간단하게 한곳에서 사용할거면 이게 더 나을듯?

    //private Environment env; // yml 설정에 값들 사용 가능하도록 해주는 것

//    @Autowired
//    public UserController(Environment env) {
//        this.env = env;
//    }
    private final Environment env; // yml에 있는거 다 쓸수 있음getproperty로
    private final GreetingProperties greetingProperties;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @GetMapping("/health_check")
    // 각 메서드가 호출될때마다 필요한 지표를 수집할 수 있도록 하는 애노테이션
    // value는 그냥 이름
    @Timed(value = "users.status",longTask = true)
    public String status(){
        return "its working in user service port "+ env.getProperty("local.server.port")
                + ", port(yml server port)="+env.getProperty("server.port")
                + ", port(token secret)="+env.getProperty("token.secret")
                + ", port(token time)="+env.getProperty("token.expiration_time")
                ;
    }

    @Timed(value = "users.welcome",longTask = true)
    @GetMapping("/welcome")
    public String welcome(){
        return greetingProperties.getMessage();
    }

    @PostMapping("/users")
    public ResponseEntity createUser(@RequestBody RequestUser user){
        UserDto userDto = modelMapper.map(user, UserDto.class);
        userService.createUser(userDto);
        ResponseUser responseUser = modelMapper.map(userDto, ResponseUser.class);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseUser);
    }

    @GetMapping("/users")
    public ResponseEntity getUsers(){
        List<UserEntity> userByAll = userService.getUserByAll();

        List<ResponseUser> result = new ArrayList<>();

        userByAll.forEach(u ->{
            result.add(modelMapper.map(u,ResponseUser.class));
                });
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity getUser(@PathVariable String userId){
        UserDto userByUserId = userService.getUserByUserId(userId);

        ResponseUser responseUser = modelMapper.map(userByUserId, ResponseUser.class);

        return ResponseEntity.status(HttpStatus.OK).body(responseUser);
    }
}
