package com.webservicestudy.webservicestudy.modules.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webservicestudy.webservicestudy.modules.account.CurrentUser;
import com.webservicestudy.webservicestudy.modules.account.Account;
import com.webservicestudy.webservicestudy.modules.tag.Tag;
import com.webservicestudy.webservicestudy.modules.zone.Zone;
import com.webservicestudy.webservicestudy.modules.account.form.TagForm;
import com.webservicestudy.webservicestudy.modules.account.form.ZoneForm;
import com.webservicestudy.webservicestudy.modules.study.form.StudyDescriptionForm;
import com.webservicestudy.webservicestudy.modules.tag.TagRepository;
import com.webservicestudy.webservicestudy.modules.tag.TagService;
import com.webservicestudy.webservicestudy.modules.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingController {

    private final StudyService studyService;
    private final ModelMapper modelMapper;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;
    private final TagService tagService;
    private final ZoneRepository zoneRepository;

    @GetMapping("/description")
    public String viewStudySetting(@CurrentUser Account account, @PathVariable String path, Model model){

        Study study = studyService.getStudyToUpdate(account,path);

        model.addAttribute(account);
        model.addAttribute(modelMapper.map(study,StudyDescriptionForm.class));
        model.addAttribute(study);

        return "/study/settings/description";
    }


    @PostMapping("/description")
    public String updateStudyInfo(@CurrentUser Account account, @PathVariable String path,
                                  @Valid StudyDescriptionForm studyDescriptionForm,
                                  Errors errors, RedirectAttributes attributes,Model model){
        // service에서 find로 찾아오니 study는 persistence상태 -> dirty checking사용하면 된다
        Study study = studyService.getStudyToUpdate(account, path);
        if (errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(study);
            return "study/settings/description";
        }


        studyService.updateStudyDescription(study,studyDescriptionForm);
        attributes.addFlashAttribute("message","스터디 소개를 수정했습니다.");
        return "redirect:/study/"+getPath(path)+"/settings/description";

    }

    private String getPath(String path) {
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }


    @GetMapping("/banner")
    public String studyImageForm(@CurrentUser Account account, @PathVariable String path, Model model){
        Study study = studyService.getStudyToUpdate(account,path);

        model.addAttribute(study);
        model.addAttribute(account);
        return "study/settings/banner";
    }


    @PostMapping("/banner")
    public String studyImageSubmit(@CurrentUser Account account,@PathVariable String path,
                                   String image, RedirectAttributes attributes, Model model ){

        Study study = studyService.getStudyToUpdate(account, path);
        studyService.updateStudyImage(study,image);
        attributes.addFlashAttribute("message","스터디 이미지를 수정했습니다.");
        return "redirect:/study/"+getPath(path)+"/settings/banner";
    }


    @PostMapping("/banner/enable")
    public String enableStudyBanner(@CurrentUser Account account, @PathVariable String path){
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.enableStudyBanner(study);
        return "redirect:/study/"+getPath(path)+"/settings/banner";
    }

    @PostMapping("/banner/disable")
    public String disableStudyBanner(@CurrentUser Account account, @PathVariable String path){
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.disableStudyBanner(study);
        return "redirect:/study/"+getPath(path)+"/settings/banner";
    }


    @GetMapping("/tags")
    public String studyTagsForm(@CurrentUser Account account, @PathVariable String path, Model model) throws JsonProcessingException {
        // study를 찾고 account가 매니저인지 확인
        Study study = studyService.getStudyToUpdate(account, path);

        model.addAttribute(account);
        model.addAttribute(study);
        // study가 가지고 있는 태그 모델이 담기
        model.addAttribute("tags",study.getTags().stream().map(Tag::getTitle).collect(Collectors.toList()));

        // 있는태그 다 뽑아오기
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        // whitelist는 json으로 넘겨야 인식
        model.addAttribute("whitelist",objectMapper.writeValueAsString(allTags));

        return "study/settings/tags";
    }


    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Account account, @PathVariable String path
                                    ,@RequestBody TagForm tagForm){
        // getStudyToUpdate는 study 찾아올때 한번에 다 땡기는 쿼리 사용함
        // 근데 tag에서는 study를 다 땡겨올 필요 없음 -> tag만 땡겨오면 됨
        // 리포지토리에서 사용하는 쿼리문을 바꿔야함
        Study study = studyService.getStudyToUpdateTags(account, path);
        // 들어온 태그가 있으면 db에서 태그 찾아서 박아주면되고
        // 없으면 db에 저장하고 박아주면 된다.
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        studyService.addTag(study,tag);

        return ResponseEntity.ok().build();
    }


    @PostMapping("/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Account account, @PathVariable String path,
                                    @RequestBody TagForm tagForm){
        Study study = studyService.getStudyToUpdateTags(account, path);
        Tag tag = tagRepository.findByTitle(tagForm.getTagTitle());
        if (tag == null){
            return ResponseEntity.badRequest().build();
        }
        studyService.removeTag(study,tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/zones")
    public String studyZonesForm(@CurrentUser Account account, @PathVariable String path,
                                 Model model) throws JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute("zones",study.getZones().stream().map(Zone::toString)
                    .collect(Collectors.toList()));
        List<String> zones = zoneRepository.findAll().stream().map(Zone::toString)
                .collect(Collectors.toList());

        model.addAttribute("whitelist",objectMapper.writeValueAsString(zones));
        return "study/settings/zones";
    }

    // add
    // account 매니저인지 확인하고study 가져오기
    // zone리포지토리에서 찾아
    // 없으면 예외
    // 있으면 study에 저장하고 리턴
    @PostMapping("/zones/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentUser Account account, @PathVariable String path,
                                  @RequestBody ZoneForm zoneForm) {
        Study study = studyService.getStudyToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.addZone(study, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/zones/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentUser Account account, @PathVariable String path,
                                     @RequestBody ZoneForm zoneForm) {
        Study study = studyService.getStudyToUpdateZone(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        studyService.removeZone(study, zone);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/study")
    public String studySettingForm(@CurrentUser Account account,@PathVariable String path, Model model){
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        return "study/settings/study";
    }

    // 사용자 매니저인지 study 가져오고
    // 서비스에서 publish로 study 엔티티 수정

    @PostMapping("/study/publish")
    public String publishStudy(@CurrentUser Account account, @PathVariable String path,
                               RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.publish(study);
        attributes.addFlashAttribute("message","스터디를 공개했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/study/close")
    public String closeStudy(@CurrentUser Account account, @PathVariable String path,
                             RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.close(study);
        attributes.addFlashAttribute("message", "스터디를 종료했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    // study 가져와서
    // 리쿠르팅 서비스로 넘기고 도메인으로 리우크팅 넘기기

    @PostMapping("/recruit/start")
    public String startRecruit(@CurrentUser Account account, @PathVariable String path,
                               RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if(!study.canUpdateRecruiting() ){
            attributes.addFlashAttribute("message","1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/study/" + getPath(path) + "/settings/study";
        }

        studyService.startRecruit(study);
        attributes.addFlashAttribute("message", "인원 모집을 시작합니다.");

        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentUser Account account, @PathVariable String path, Model model,
                              RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdate(account, path);
        if (!study.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1시간 안에 인원 모집 설정을 여러번 변경할 수 없습니다.");
            return "redirect:/study/" + getPath(path) + "/settings/study";
        }

        studyService.stopRecruit(study);
        attributes.addFlashAttribute("message", "인원 모집을 종료합니다.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/study/path")
    public String updateStudyPath(@CurrentUser Account account, @PathVariable String path, String newPath,
                                  Model model, RedirectAttributes attributes) {
        // 원래는 form으로 받아서 일관성 있게 하는게 좋은데
        // 연습이니까 html form태그에서 object를 주지 않고 그냥 name을 줘서 newPath로 줬음 -> newPath로 요청이 날라옴
        // 단일 데이터이므로 valid 애노테이션 사용 불가
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (!studyService.isValidPath(newPath)) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyPathError", "해당 스터디 경로는 사용할 수 없습니다. 다른 값을 입력하세요.");
            return "study/settings/study";
        }

        studyService.updateStudyPath(study, newPath);
        attributes.addFlashAttribute("message", "스터디 경로를 수정했습니다.");
        return "redirect:/study/" + getPath(newPath) + "/settings/study";
    }

    @PostMapping("/study/title")
    public String updateStudyTitle(@CurrentUser Account account, @PathVariable String path, String newTitle,
                                   Model model, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (!studyService.isValidTitle(newTitle)) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyTitleError", "스터디 이름을 다시 입력하세요.");
            return "study/settings/study";
        }

        studyService.updateStudyTitle(study, newTitle);
        attributes.addFlashAttribute("message", "스터디 이름을 수정했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/study/remove")
    public String removeStudy(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.remove(study);
        return "redirect:/";
    }



}
