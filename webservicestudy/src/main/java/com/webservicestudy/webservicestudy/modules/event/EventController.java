package com.webservicestudy.webservicestudy.modules.event;

import com.webservicestudy.webservicestudy.modules.account.Account;
import com.webservicestudy.webservicestudy.modules.account.CurrentUser;
import com.webservicestudy.webservicestudy.modules.event.form.EventForm;
import com.webservicestudy.webservicestudy.modules.event.validator.EventValidator;
import com.webservicestudy.webservicestudy.modules.study.Study;
import com.webservicestudy.webservicestudy.modules.study.StudyRepository;
import com.webservicestudy.webservicestudy.modules.study.StudyService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/study/{path}")
public class EventController {

    private final StudyService studyService;
    private final EventService eventService;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;
    private final EventRepository eventRepository;
    private final StudyRepository studyRepository;

    @InitBinder("eventForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(eventValidator);
    }

    @GetMapping("/new-event")
    public String newEventForm(@CurrentUser Account account, @PathVariable String path, Model model){
        Study study = studyService.getStudyToUpdateStatus(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(new EventForm());
        return "event/form";
    }

    @PostMapping("/new-event")
    public String newEventSubmit(@CurrentUser Account account, @PathVariable String path,
                                 @Valid EventForm eventForm, Errors errors, Model model){
        Study study = studyService.getStudyToUpdateStatus(account, path);
        if (errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(study);
            return "/event/form";
        }

        Event event = eventService.createEvent(modelMapper.map(eventForm, Event.class), account, study);

        return "redirect:/study/" + study.getEncodedPath() + "/events/"+event.getId();

    }

    @GetMapping("/events/{id}")
    public String getEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable("id") Event event,
                           Model model) {
        model.addAttribute(account);
        model.addAttribute(event);
        model.addAttribute(studyRepository.findStudyWithManagersByPath(path));
        // view에서 enrollment size때문에 enrollment lazy로 들어와있어서 쿼리 한번 나감

        return "event/view";
    }



    @GetMapping("events")
    public String viewStudyEvents(@CurrentUser Account account, @PathVariable String path,
                                  Model model){

        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);
        // study정보로 event를 찾아야 함
        List<Event> events = eventRepository.findByStudyOrderByStartDateTime(study);
        List<Event> newEvents = new ArrayList<>();
        List<Event> oldEvents = new ArrayList<>();

        for (Event e : events) {
            if (e.getEndDateTime().isBefore(LocalDateTime.now())){
                oldEvents.add(e);
            } else{
                newEvents.add(e);
            }
        }
        model.addAttribute("newEvents",newEvents);
        model.addAttribute("oldEvents",oldEvents);

        return "study/events";

    }


    @GetMapping("/events/{id}/edit")
    public String updateEventForm(@CurrentUser Account account, @PathVariable String path,
                                  @PathVariable Long id,Model model){
        //Study study = studyService.getStudyToUpdate(account, path);
        // 왜 study 다 땡겨와야 하는지 모르겠다. 그냥 account가 수정가능한지만 확인하면 되는거 아닌가
        // 그래서 나는 그냥 status만 땡겼다
        Study study = studyService.getStudyToUpdateStatus(account, path);
        Event event = eventRepository.findById(id).orElseThrow();

        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(event); // 맨 위에 스터디 타이들과 이벤트 타이들 적을 때 필요
        model.addAttribute(modelMapper.map(event,EventForm.class)); // event 현재 정보를 form으로 바꿔서 넘기면 화면에서 그대로 뿌림
        return"event/update-form";
    }


    @PostMapping("/events/{id}/edit")
    public String updateEventSubmit(@CurrentUser Account account, @PathVariable String path
                                    ,@PathVariable Long id, @Valid EventForm eventForm, Model model,Errors errors){

        //Study study = studyService.getStudyToUpdate(account, path);
        Study study = studyService.getStudyToUpdateStatus(account, path);
        Event event = eventRepository.findById(id).orElseThrow();
        event.setEventType(event.getEventType());
        eventValidator.validateUpdateForm(eventForm,event,errors);
        if (errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(study);
            model.addAttribute(event);
            return "event/update-form";
        }

        eventService.updateEvent(event,eventForm);
        return "redirect:/study/" + study.getEncodedPath() +  "/events/" + event.getId();
    }

    @PostMapping("/events/{id}/delete")
    public String cancelEvent(@CurrentUser Account account, @PathVariable String path, @PathVariable Long id){
        Study study = studyService.getStudyToUpdateStatus(account, path);
        Event event = eventRepository.findById(id).orElseThrow();
        eventService.deleteEvent(event);
        return "redirect:/study/" + study.getEncodedPath() + "/events";
    }


    // 참가 신청 누르기
    // 해당 이벤트 찾아와서 enrollment 만들어서 넣기
    // event 조회 페이지로 이동
    // study의 path만 필요함
    @PostMapping("/events/{id}/enroll")
    public String newEnrollment(@CurrentUser Account account, @PathVariable String path,
                                @PathVariable Long id){
        Study study = studyService.getStudyToEnroll(path);
        eventService.newEnrollment(eventRepository.findById(id).orElseThrow(),account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/"+id;

    }

    // 참가 취소 누르기
    // account와 event로 엮어서 해당 enrollment 찾아오기
    // 양방향이라 enroll 양쪽에서 지워주고
    // 리포지토리에서 enroll 삭제

    @PostMapping("/events/{id}/disenroll")
    public String cancelEnrollment(@CurrentUser Account account, @PathVariable String path,
                                @PathVariable Long id) {
        Study study = studyService.getStudyToEnroll(path);
        eventService.cancelEnrollment(eventRepository.findById(id).orElseThrow(),account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/"+id;
    }

    // 프론트상 이쁘게 하려고 get썻다고함
    @GetMapping("events/{eventId}/enrollments/{enrollmentId}/accept")
    public String acceptEnrollment(@CurrentUser Account account, @PathVariable String path,
                                  @PathVariable("eventId") Event event, @PathVariable("enrollmentId")Enrollment enrollment){
        Study study = studyService.getStudyToUpdate(account, path);
        eventService.acceptEnrollment(event,enrollment);
        return "redirect:/study/"+study.getEncodedPath() +"/events/"+event.getId();
    }

    @GetMapping("events/{eventId}/enrollments/{enrollmentId}/reject")
    public String rejectEnrollment(@CurrentUser Account account, @PathVariable String path,
                                   @PathVariable("eventId") Event event, @PathVariable("enrollmentId")Enrollment enrollment){
        Study study = studyService.getStudyToUpdate(account, path);
        eventService.rejectEnrollment(event,enrollment);
        return "redirect:/study/"+study.getEncodedPath() +"/events/"+event.getId();
    }


    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/checkin")
    public String checkInEnrollment(@CurrentUser Account account, @PathVariable String path,
                                    @PathVariable("eventId") Event event, @PathVariable("enrollmentId")Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(account, path);
        eventService.checkInEnrollment(enrollment);
        return "redirect:/study/"+study.getEncodedPath() +"/events/"+event.getId();
    }
    @GetMapping("/events/{eventId}/enrollments/{enrollmentId}/cancel-checkin")
    public String cancelCheckInEnrollment(@CurrentUser Account account, @PathVariable String path,
                                          @PathVariable("eventId") Event event, @PathVariable("enrollmentId") Enrollment enrollment) {
        Study study = studyService.getStudyToUpdate(account, path);
        eventService.cancelCheckInEnrollment(enrollment);
        return "redirect:/study/" + study.getEncodedPath() + "/events/" + event.getId();
    }

}
