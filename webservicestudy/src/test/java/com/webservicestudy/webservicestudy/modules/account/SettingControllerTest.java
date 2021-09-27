package com.webservicestudy.webservicestudy.modules.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.webservicestudy.webservicestudy.infra.AbstractContainerBaseTest;
import com.webservicestudy.webservicestudy.infra.MockMvcTest;
import com.webservicestudy.webservicestudy.modules.tag.Tag;
import com.webservicestudy.webservicestudy.modules.zone.Zone;
import com.webservicestudy.webservicestudy.modules.account.form.TagForm;
import com.webservicestudy.webservicestudy.modules.account.form.ZoneForm;
import com.webservicestudy.webservicestudy.modules.tag.TagRepository;
import com.webservicestudy.webservicestudy.modules.zone.ZoneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class SettingControllerTest extends AbstractContainerBaseTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("테스트시").province("테스트주").build();

    @BeforeEach
    void beforeEach() {
        zoneRepository.save(testZone);
    }



    // 프로필 수정하는 시점에는 securitycontext에 해당 사용자가 있어야한다.
    // 이 과정은 @WithSecurityContext 애노테이션을 커스텀해서 해결할 수 있다.
    // 커스텀한 WithAccount는 계정을 만들고 securitycontext에 넣은 상태에서 테스트가 진행된다.
    @WithAccount("backtony")
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile_with_correct_input() throws Exception{
        String bio = "잛은 소개를 수정하는 경우";
        mockMvc.perform(post(SettingController.SETTINGS_PROFILE_URL)
                .param("bio",bio)
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingController.SETTINGS_PROFILE_URL))
                .andExpect(flash().attributeExists("message"));

        Account backtony = accountRepository.findByNickname("backtony"); // 수정하고 변했는지 확인
        assertEquals(bio,backtony.getBio());


    }

    @WithAccount("backtony")
    @DisplayName("프로필 수정하기 - 입력값 에러")
    @Test
    void updateProfile_with_wrong_input() throws Exception{
        String bio = "너무 길게 소개를 수정하는 경우, 너무 길게 소개를 수정하는 경우, 너무 길게 소개를 수정하는 경우, 너무 길게 소개를 수정하는 경우,";
        mockMvc.perform(post(SettingController.SETTINGS_PROFILE_URL)
                .param("bio",bio)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_PROFILE_VIEW_NAME))
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().hasErrors())
                ;

        Account backtony = accountRepository.findByNickname("backtony"); // 수정하고 변했는지 확인
        assertNull(backtony.getBio());


    }

    @WithAccount("backtony")
    @DisplayName("프로필 수정 폼")
    @Test
    void updateProfileForm() throws Exception{
        mockMvc.perform(get(SettingController.SETTINGS_PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("profile"))
                .andExpect(model().attributeExists("account"))
        ;
    }

    @WithAccount("backtony")
    @DisplayName("패스워드 수정 폼")
    @Test
    void updatePasswordForm() throws Exception{
        mockMvc.perform(get(SettingController.SETTINGS_PASSWORD_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
                .andExpect(view().name(SettingController.SETTINGS_PASSWORD_VIEW_NAME))
        ;
    }
    @WithAccount("backtony")
    @DisplayName("패스워드 수정 정상 입력")
    @Test
    void updatePassword_with_correct_input() throws Exception{
        mockMvc.perform(post(SettingController.SETTINGS_PASSWORD_URL)
                .param("newPassword","123456789")
                .param("newPasswordConfirm","123456789")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(SettingController.SETTINGS_PASSWORD_URL))
                .andExpect(flash().attributeExists("message"))
        ;

        Account backtony = accountRepository.findByNickname("backtony");
        assertTrue(passwordEncoder.matches("123456789", backtony.getPassword()));

    }

    @WithAccount("backtony")
    @DisplayName("패스워드 수정 입력 불일치")
    @Test
    void updatePassword_with_wrong_input() throws Exception{
        mockMvc.perform(post(SettingController.SETTINGS_PASSWORD_URL)
                .param("newPassword","123456789")
                .param("newPasswordConfirm","12345")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name(SettingController.SETTINGS_PASSWORD_VIEW_NAME))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("passwordForm"))
        ;


    }
    @WithAccount("backtony")
    @DisplayName("태그 수정 폼")
    @Test
    void updateTagsForm() throws Exception{
        mockMvc.perform(get(SettingController.SETTINGS_TAGS_URL))
                .andExpect(view().name(SettingController.SETTINGS_TAGS_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("tags"))
                ;
    }
    @WithAccount("backtony")
    @DisplayName("계정 태그 추가")
    @Test
    void addTag() throws Exception{
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle("newTag");

        mockMvc.perform(post(SettingController.SETTINGS_TAGS_URL+"/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf())) // post 요청에서는 로그인이 아니더라도 반드시 테스트시 csrf 토큰 붙여줘야한다!!
                .andExpect(status().isOk())
        ;
        // 태그가 저장됬는지 확인
        Tag newTag = tagRepository.findByTitle(tagForm.getTagTitle());
        assertNotNull(newTag);
        // 읽어오는것 까지 accountRepository.findByNickname("backtony") 까지는
        // transaction 처리됨 리포지토리가 transactional 이 붙어있으니까
        // 하지만 결과적으로 가져온 backtony 객체는 persistence 상태가 아니다.
        // 여기 전체 코드에 트랜잭션이 붙어있진 않음 -> 따라서 backtony는 detached 상태이다
        // 트랜잭션을 유지하려면 전체에 transaction이 붙어있어야한다
        Account backtony = accountRepository.findByNickname("backtony");

        // 결국 꺼내온 것이 detached 상태이고 tags는 manytomany로 lazy로 프록시로 땡겨온다.
        // 그러므로 getTags로 tags를 가져올 수 없게 된다. -> 해결책 -> 트랜잭션 애노테이션 붙이기
        assertTrue(backtony.getTags().contains(newTag));
    }

    @WithAccount("backtony")
    @DisplayName("태그 삭제")
    @Test
    void removeTags() throws Exception{
        Account backtony = accountRepository.findByNickname("backtony");
        Tag newTag = Tag.builder().title("newTag").build();
        tagRepository.save(newTag);
        accountService.addTag(backtony,newTag);


        assertTrue(backtony.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle(newTag.getTitle());

        mockMvc.perform(post(SettingController.SETTINGS_TAGS_URL+"/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf())) // post 요청에서는 로그인이 아니더라도 반드시 테스트시 csrf 토큰 붙여줘야한다!!
                .andExpect(status().isOk())
        ;
        assertFalse(backtony.getTags().contains(newTag));
    }

    @WithAccount("backtony")
    @DisplayName("지역 수정 폼")
    @Test
    void updateZoneForm() throws Exception{
        mockMvc.perform(get(SettingController.SETTINGS_ZONE_URL))
                .andExpect(view().name(SettingController.SETTINGS_ZONE_VIEW_NAME))
                .andExpect(model().attributeExists("account"))
                .andExpect(model().attributeExists("whitelist"))
                .andExpect(model().attributeExists("zones"))
        ;
    }
    @WithAccount("backtony")
    @DisplayName("계정 지역 추가")
    @Test
    void addZone() throws Exception{
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(SettingController.SETTINGS_ZONE_URL+"/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(zoneForm))
                    .with(csrf()))
                    .andExpect(status().isOk())
                ;

        // 계정에 지역 잘 들어갔는지
        Account backtony = accountRepository.findByNickname("backtony");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        assertTrue(backtony.getZones().contains(zone));

    }

    @WithAccount("backtony")
    @DisplayName("계정 지역 삭제")
    @Test
    void removeZone() throws Exception{
        Account backtony = accountRepository.findByNickname("backtony");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(backtony,zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(SettingController.SETTINGS_ZONE_URL+"/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
                .andExpect(status().isOk())
        ;

        assertFalse(backtony.getZones().contains(zone));


    }




}