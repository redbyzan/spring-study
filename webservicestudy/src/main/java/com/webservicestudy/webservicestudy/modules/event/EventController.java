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
        // view?????? enrollment size????????? enrollment lazy??? ?????????????????? ?????? ?????? ??????

        return "event/view";
    }



    @GetMapping("events")
    public String viewStudyEvents(@CurrentUser Account account, @PathVariable String path,
                                  Model model){

        Study study = studyService.getStudy(path);
        model.addAttribute(account);
        model.addAttribute(study);
        // study????????? event??? ????????? ???
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
        // ??? study ??? ???????????? ????????? ????????????. ?????? account??? ????????????????????? ???????????? ????????? ?????????
        // ????????? ?????? ?????? status??? ?????????
        Study study = studyService.getStudyToUpdateStatus(account, path);
        Event event = eventRepository.findById(id).orElseThrow();

        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(event); // ??? ?????? ????????? ???????????? ????????? ????????? ?????? ??? ??????
        model.addAttribute(modelMapper.map(event,EventForm.class)); // event ?????? ????????? form?????? ????????? ????????? ???????????? ????????? ??????
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


    // ?????? ?????? ?????????
    // ?????? ????????? ???????????? enrollment ???????????? ??????
    // event ?????? ???????????? ??????
    // study??? path??? ?????????
    @PostMapping("/events/{id}/enroll")
    public String newEnrollment(@CurrentUser Account account, @PathVariable String path,
                                @PathVariable Long id){
        Study study = studyService.getStudyToEnroll(path);
        eventService.newEnrollment(eventRepository.findById(id).orElseThrow(),account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/"+id;

    }

    // ?????? ?????? ?????????
    // account??? event??? ????????? ?????? enrollment ????????????
    // ??????????????? enroll ???????????? ????????????
    // ????????????????????? enroll ??????

    @PostMapping("/events/{id}/disenroll")
    public String cancelEnrollment(@CurrentUser Account account, @PathVariable String path,
                                @PathVariable Long id) {
        Study study = studyService.getStudyToEnroll(path);
        eventService.cancelEnrollment(eventRepository.findById(id).orElseThrow(),account);
        return "redirect:/study/" + study.getEncodedPath() + "/events/"+id;
    }

    // ???????????? ????????? ????????? get????????????
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
