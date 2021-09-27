package com.webservicestudy.webservicestudy.modules.account;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webservicestudy.webservicestudy.modules.account.form.*;
import com.webservicestudy.webservicestudy.modules.tag.Tag;
import com.webservicestudy.webservicestudy.modules.zone.Zone;
import com.webservicestudy.webservicestudy.modules.account.validator.NicknameValidator;
import com.webservicestudy.webservicestudy.modules.account.validator.PasswordFormValidator;
import com.webservicestudy.webservicestudy.modules.tag.TagRepository;
import com.webservicestudy.webservicestudy.modules.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.modelmapper.ModelMapper;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class SettingController {

    // test에서도 사용하려고 private 제거함
    static final String SETTINGS_PROFILE_VIEW_NAME = "settings/profile";
    static final String SETTINGS_PROFILE_URL = "/settings/profile";
    static final String SETTINGS_PASSWORD_VIEW_NAME = "settings/password";
    static final String SETTINGS_PASSWORD_URL = "/settings/password";
    static final String SETTINGS_NOTIFICATION_VIEW_NAME = "settings/notifications";
    static final String SETTINGS_NOTIFICATION_URL = "/settings/notifications";
    static final String SETTINGS_ACCOUNT_VIEW_NAME = "settings/account";
    static final String SETTINGS_ACCOUNT_URL = "/settings/account";
    static final String SETTINGS_TAGS_VIEW_NAME = "settings/tags";
    static final String SETTINGS_TAGS_URL = "/settings/tags";
    static final String SETTINGS_ZONE_VIEW_NAME = "settings/zones";
    static final String SETTINGS_ZONE_URL = "/settings/zones";

    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final NicknameValidator nicknameValidator;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;
    private final ZoneRepository zoneRepository;

    @InitBinder("passwordForm")
    public void initBinderPassword(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void initBinderNickname(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameValidator); // 빈등록 되어있어서 new 쓰면 안됨
    }

    // 프로필 수정 폼 띄우기
    // 어떤 유저인지 url을 명시할 필요가 없는게 자기 자신의 프로필 수정은 자신 뿐이 못함
    // 현재 유저를 넣어주고 그 유저에 대한 정보를 컨트롤하면 된다.
    @GetMapping(SETTINGS_PROFILE_URL)
    public String profileUpdateForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Profile.class));

        return SETTINGS_PROFILE_VIEW_NAME;
    }

    // 프로필 수정 폼 작성
    @PostMapping(SETTINGS_PROFILE_URL)
    public String updateProfile(@CurrentUser Account account, @Valid Profile profile,
                                Errors errors, Model model, RedirectAttributes redirectAttributes) {
        if (errors.hasErrors()) {
            // error가 발생하면 model에는 profile을 채웠던 정보와 error가 자동으로 들어간다.
            // 따라서 계정 사용자만 담아주면 된다.
            model.addAttribute(account);

            return SETTINGS_PROFILE_VIEW_NAME;
        }

        // 현재 account는 detached 상태 -> transactional으로 데이터 변경해도 변경되지 않음
        accountService.updateProfile(account, profile);

        // redirect될 때 수정화면에서 뭔가 표시가 필요함 -> message 필요
        // 리다이렉트 과정에서 데이터를 보내려면 get방식에 파라미터로 노출시켜야 하는데
        // 이런 문제점을 막기 위해 사용하는 것이 RedirectAttributes
        // RedirectAttributes는 redirect 시 데이터를 편리하게 전송하는 기능을 하는 것이다.
        // 리다이렉트된 이후에는 자동으로 model에 들어가 있다.
        // flash는 리다이렉트로 전송하고 나서 딱 한번만 사용하고 소멸되는 방식이다.
        redirectAttributes.addFlashAttribute("message", "프로필이 수정되었습니다.");

        return "redirect:" + SETTINGS_PROFILE_URL;

    }

    @GetMapping(SETTINGS_PASSWORD_URL)
    public String passwordUpdateForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return SETTINGS_PASSWORD_VIEW_NAME;
    }

    @PostMapping(SETTINGS_PASSWORD_URL)
    public String updatePassword(@CurrentUser Account account, @Valid PasswordForm passwordForm,
                                 Errors errors, Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_PASSWORD_VIEW_NAME;
        }
        accountService.passwordUpdate(account, passwordForm.getNewPasswordConfirm());

        attributes.addFlashAttribute("message", "비밀번호가 변경되었습니다.");
        return "redirect:" + SETTINGS_PASSWORD_URL;
    }

    @GetMapping(SETTINGS_NOTIFICATION_URL)
    public String updateNotificationForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Notifications.class));
        return SETTINGS_NOTIFICATION_VIEW_NAME;

    }

    @PostMapping(SETTINGS_NOTIFICATION_URL)
    // valid는 특정한 애노테이션이 아니더라도 타입값이 달라도 걸러준다.
    public String updateNotifications(@CurrentUser Account account, @Valid Notifications notifications, Errors errors,
                                      Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_NOTIFICATION_VIEW_NAME;
        }

        accountService.updateNotifications(account, notifications);
        attributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return "redirect:" + SETTINGS_NOTIFICATION_URL;
    }

    @GetMapping(SETTINGS_ACCOUNT_URL)
    public String updateAccount(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new NicknameForm());
        return SETTINGS_ACCOUNT_URL;
    }

    @PostMapping(SETTINGS_ACCOUNT_URL)
    public String updateNickname(@CurrentUser Account account, @Valid NicknameForm nickNameForm,
                                 Errors errors, Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS_ACCOUNT_VIEW_NAME;
        }

        accountService.updateNickname(account, nickNameForm);
        attributes.addFlashAttribute("message", "닉네임을 수정했습니다.");

        return "redirect:" + SETTINGS_ACCOUNT_URL;
    }

    @GetMapping(SETTINGS_TAGS_URL)
    public String updateTags(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Tag> tags = accountService.getTags(account);
        // set에서 꺼내서 리스트로 변환
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        // 자동완성 기능을 위해 다 꺼내서 보내줘야함
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return SETTINGS_TAGS_VIEW_NAME;
    }

    @PostMapping(SETTINGS_TAGS_URL + "/add")
    @ResponseBody
    // 요청 자체가 ajax 요청으로 json으로 들어옴 // 비동기
    // 응답 자체가 json으로 넘겨줘야함
    public ResponseEntity addTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if (tag == null) {
            tag = tagRepository.save(Tag.builder().title(title).build());
        }


        accountService.addTag(account, tag);
        return ResponseEntity.ok().build();

    }

    @PostMapping(SETTINGS_TAGS_URL + "/remove")
    @ResponseBody
    // 요청 자체가 ajax 요청으로 json으로 들어옴 // 비동기
    // 응답 자체가 json으로 넘겨줘야함
    public ResponseEntity removeTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }
        accountService.removeTag(account, tag);

        return ResponseEntity.ok().build();

    }


    @GetMapping(SETTINGS_ZONE_URL)
    public String updateZoneForm(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        // 계정에 대한 존 정보 가져오기
        Set<Zone> zones = accountService.getZones(account);
        model.addAttribute("zones",zones.stream().map(Zone::toString).collect(Collectors.toList()));

        // 자동완선을 위해 전체 zone 땡겨오고, whitelist가 json으로 작동하므로 json으로 변환해서 넣음
        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist",objectMapper.writeValueAsString(allZones));

        return SETTINGS_ZONE_VIEW_NAME;
    }

    // 이 프론트 자체가 ajax 통신이므로 json으로 통신 form으로 입력 받아서 zone 저장
    @PostMapping(SETTINGS_ZONE_URL+"/add")
    @ResponseBody
    public ResponseEntity updateZone(@CurrentUser Account account, @RequestBody ZoneForm zoneForm, Model model){
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        // zone은 주어진 값중에서만 가능함
        if (zone == null){
            return ResponseEntity.badRequest().build();
        }
        accountService.addZone(account,zone);
        return ResponseEntity.ok().build();
    }

    // 이 프론트 자체가 ajax 통신이므로 json으로 통신 form으로 입력 받아서 zone 저장
    @PostMapping(SETTINGS_ZONE_URL+"/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentUser Account account, @RequestBody ZoneForm zoneForm, Model model){
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        // zone은 주어진 값중에서만 가능함
        if (zone == null){
            return ResponseEntity.badRequest().build();
        }
        accountService.removeZone(account,zone);
        return ResponseEntity.ok().build();
    }


}







