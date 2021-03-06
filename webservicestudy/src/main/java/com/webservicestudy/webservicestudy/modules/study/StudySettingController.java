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
        // service?????? find??? ???????????? study??? persistence?????? -> dirty checking???????????? ??????
        Study study = studyService.getStudyToUpdate(account, path);
        if (errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(study);
            return "study/settings/description";
        }


        studyService.updateStudyDescription(study,studyDescriptionForm);
        attributes.addFlashAttribute("message","????????? ????????? ??????????????????.");
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
        attributes.addFlashAttribute("message","????????? ???????????? ??????????????????.");
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
        // study??? ?????? account??? ??????????????? ??????
        Study study = studyService.getStudyToUpdate(account, path);

        model.addAttribute(account);
        model.addAttribute(study);
        // study??? ????????? ?????? ?????? ????????? ??????
        model.addAttribute("tags",study.getTags().stream().map(Tag::getTitle).collect(Collectors.toList()));

        // ???????????? ??? ????????????
        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        // whitelist??? json?????? ????????? ??????
        model.addAttribute("whitelist",objectMapper.writeValueAsString(allTags));

        return "study/settings/tags";
    }


    @PostMapping("/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Account account, @PathVariable String path
                                    ,@RequestBody TagForm tagForm){
        // getStudyToUpdate??? study ???????????? ????????? ??? ????????? ?????? ?????????
        // ?????? tag????????? study??? ??? ????????? ?????? ?????? -> tag??? ???????????? ???
        // ????????????????????? ???????????? ???????????? ????????????
        Study study = studyService.getStudyToUpdateTags(account, path);
        // ????????? ????????? ????????? db?????? ?????? ????????? ??????????????????
        // ????????? db??? ???????????? ???????????? ??????.
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
    // account ??????????????? ????????????study ????????????
    // zone????????????????????? ??????
    // ????????? ??????
    // ????????? study??? ???????????? ??????
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

    // ????????? ??????????????? study ????????????
    // ??????????????? publish??? study ????????? ??????

    @PostMapping("/study/publish")
    public String publishStudy(@CurrentUser Account account, @PathVariable String path,
                               RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.publish(study);
        attributes.addFlashAttribute("message","???????????? ??????????????????.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/study/close")
    public String closeStudy(@CurrentUser Account account, @PathVariable String path,
                             RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.close(study);
        attributes.addFlashAttribute("message", "???????????? ??????????????????.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    // study ????????????
    // ???????????? ???????????? ????????? ??????????????? ???????????? ?????????

    @PostMapping("/recruit/start")
    public String startRecruit(@CurrentUser Account account, @PathVariable String path,
                               RedirectAttributes attributes){
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if(!study.canUpdateRecruiting() ){
            attributes.addFlashAttribute("message","1?????? ?????? ?????? ?????? ????????? ????????? ????????? ??? ????????????.");
            return "redirect:/study/" + getPath(path) + "/settings/study";
        }

        studyService.startRecruit(study);
        attributes.addFlashAttribute("message", "?????? ????????? ???????????????.");

        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/recruit/stop")
    public String stopRecruit(@CurrentUser Account account, @PathVariable String path, Model model,
                              RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdate(account, path);
        if (!study.canUpdateRecruiting()) {
            attributes.addFlashAttribute("message", "1?????? ?????? ?????? ?????? ????????? ????????? ????????? ??? ????????????.");
            return "redirect:/study/" + getPath(path) + "/settings/study";
        }

        studyService.stopRecruit(study);
        attributes.addFlashAttribute("message", "?????? ????????? ???????????????.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/study/path")
    public String updateStudyPath(@CurrentUser Account account, @PathVariable String path, String newPath,
                                  Model model, RedirectAttributes attributes) {
        // ????????? form?????? ????????? ????????? ?????? ????????? ?????????
        // ??????????????? html form???????????? object??? ?????? ?????? ?????? name??? ?????? newPath??? ?????? -> newPath??? ????????? ?????????
        // ?????? ?????????????????? valid ??????????????? ?????? ??????
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (!studyService.isValidPath(newPath)) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyPathError", "?????? ????????? ????????? ????????? ??? ????????????. ?????? ?????? ???????????????.");
            return "study/settings/study";
        }

        studyService.updateStudyPath(study, newPath);
        attributes.addFlashAttribute("message", "????????? ????????? ??????????????????.");
        return "redirect:/study/" + getPath(newPath) + "/settings/study";
    }

    @PostMapping("/study/title")
    public String updateStudyTitle(@CurrentUser Account account, @PathVariable String path, String newTitle,
                                   Model model, RedirectAttributes attributes) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (!studyService.isValidTitle(newTitle)) {
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute("studyTitleError", "????????? ????????? ?????? ???????????????.");
            return "study/settings/study";
        }

        studyService.updateStudyTitle(study, newTitle);
        attributes.addFlashAttribute("message", "????????? ????????? ??????????????????.");
        return "redirect:/study/" + getPath(path) + "/settings/study";
    }

    @PostMapping("/study/remove")
    public String removeStudy(@CurrentUser Account account, @PathVariable String path, Model model) {
        Study study = studyService.getStudyToUpdateStatus(account, path);
        studyService.remove(study);
        return "redirect:/";
    }



}
