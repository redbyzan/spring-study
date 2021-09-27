package com.webservicestudy.webservicestudy.modules.study;

import com.webservicestudy.webservicestudy.modules.account.Account;
import com.webservicestudy.webservicestudy.modules.study.study.StudyCreatedEvent;
import com.webservicestudy.webservicestudy.modules.study.study.StudyUpdateEvent;
import com.webservicestudy.webservicestudy.modules.tag.Tag;
import com.webservicestudy.webservicestudy.modules.tag.TagRepository;
import com.webservicestudy.webservicestudy.modules.zone.Zone;
import com.webservicestudy.webservicestudy.modules.study.form.StudyDescriptionForm;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

import static com.webservicestudy.webservicestudy.modules.study.form.StudyForm.VALID_PATH_PATTERN;

@Service
@Transactional
@RequiredArgsConstructor
public class StudyService {

    private final StudyRepository studyRepository;
    private final ModelMapper modelMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final TagRepository tagRepository;

    public Study createNewStudy(Study study, Account account) {
        Study newStudy = studyRepository.save(study);
        newStudy.addManager(account);

        return newStudy;
    }

    public Study getStudy(String path) {
        Study study = studyRepository.findByPath(path);
        checkIfExistingStudy(path, study);

        return study;
    }

    private void checkIfExistingStudy(String path, Study study) {
        if (study == null) {
            throw new IllegalArgumentException(path + "에 해당하는 스터디가 없습니다.");
        }
    }

    public Study getStudyToUpdate(Account account, String path) {
        Study study = getStudy(path);

        checkIfManager(account, study);

        return study;


    }

    private void checkIfManager(Account account, Study study) {
        // account가 study를 보면 안됨 -> 거꾸로 변겅
        if(!study.isManagedBy(account)){
            throw new AccessDeniedException("해당 기능을 사용할 수 없습니다.");
        }
    }

    public void updateStudyDescription(Study study, StudyDescriptionForm studyDescriptionForm) {
        modelMapper.map(studyDescriptionForm,study);
        eventPublisher.publishEvent(new StudyUpdateEvent(study,"스터디 소개를 수정했습니다."));
    }

    public void updateStudyImage(Study study, String image) {
        study.setImage(image);
    }

    public void enableStudyBanner(Study study) {
        study.setUseBanner(true);
    }

    public void disableStudyBanner(Study study) {
        study.setUseBanner(false);
    }

    public Study getStudyToUpdateTags(Account account, String path) {
        Study study = studyRepository.findTagsByPath(path);
        checkIfExistingStudy(path, study);
        checkIfManager(account,study);
        return study;
    }

    public void addTag(Study study, Tag tag) {
        study.getTags().add(tag);
    }

    public void removeTag(Study study, Tag tag) {
        study.getTags().remove(tag);
    }

    public void addZone(Study study, Zone zone) {
        study.getZones().add(zone);
    }

    public Study getStudyToUpdateZone(Account account, String path) {
        Study study = studyRepository.findZoneByPath(path);
        checkIfExistingStudy(path,study);
        checkIfManager(account,study);
        return study;

    }

    public void removeZone(Study study, Zone zone) {
        study.getZones().remove(zone);
    }

    public Study getStudyToUpdateStatus(Account account, String path) {
        Study study = studyRepository.findStatusByPath(path);
        checkIfExistingStudy(path,study);
        checkIfManager(account,study);

        return study;
    }

    public void publish(Study study) {

        study.publish();
        // 여기서 study가 발생했다는 알림을 처리할 건데 서비스에 영향을 주지 않게 비동기 적으로 처리할 것임
        // 일단 이 study를 저장할 다른 dto같은 객체를 만들어 그곳에 study를 저장하고
        // eventpubliser의 publisevent 메서드를 이용해 eventlisner 클래스를 만들어 dto를 받아서 작업한다.
        // 비동기적으로 하길 원함(다른 스레드에서 실행)으로 Async 기능을 사용해야 한다.
        // 비동기적으로 실행되면 eventlistener에서 오류가 발생해도 실제 현재 study는 저장을 할 수 있따.
        eventPublisher.publishEvent(new StudyCreatedEvent(study));
    }

    public void close(Study study) {

        study.close();
        eventPublisher.publishEvent(new StudyUpdateEvent(study,"스터디를 중단했습니다."));
    }

    public void startRecruit(Study study) {

        study.startRecruit();
        eventPublisher.publishEvent(new StudyUpdateEvent(study,"팀원 모집을 시작합니다."));
    }

    public void stopRecruit(Study study) {
        study.stopRecruit();
        eventPublisher.publishEvent(new StudyUpdateEvent(study,"팀원 모집을 중단했습니다."));
    }

    public boolean isValidPath(String newPath) {
        //matches는 정규표현식을 인자로 받음
        if (!newPath.matches(VALID_PATH_PATTERN)){
            return false;
        }
        return !studyRepository.existsByPath(newPath);
    }

    public void updateStudyPath(Study study, String newPath) {
        study.setPath(newPath);
    }

    public void updateStudyTitle(Study study, String newTitle) {
        study.setTitle(newTitle);
    }

    public boolean isValidTitle(String newTitle) {
        return newTitle.length() <= 50;
    }

    public void remove(Study study) {
        if (study.isRemovable()) {
            studyRepository.delete(study); // jpa repository에서 delete 기능 제공
        } else{
            throw new IllegalArgumentException("스터디를 삭제할 수 없습니다.");
        }
    }

    public void addMember(Study study, Account account) {
        study.addMember(account);
    }

    public void removeMember(Study study, Account account) {

        study.removeMember(account);
    }

    public Study getStudyToEnroll(String path) {
        Study study = studyRepository.findStudyToEnrollByPath(path);
        checkIfExistingStudy(path,study);
        return study;
    }

    public void generateTestStudies(Account account) {
        for (int i=0;i<30;i++){
            String random = RandomString.make(5); // 길이만큼 랜덤으로 문자열 만듦
            Study study = Study.builder()
                    .title("테스트 스터디" + random)
                    .path("test-" + random)
                    .shortDescription("테스트용 입니다.")
                    .fullDescription("test")
                    .tags(new HashSet<>()) // builder에서 컬렉션 값 안넣으면 null로 세팅된다.
                    .managers(new HashSet<>()) // new에서 하면 적어준대로 new hashset으로 세팅되는데 빌더는 그게 안됨 반듸시 주의!!
                    .build();

            study.publish();
            Study newStudy = createNewStudy(study, account);
            Tag jpa = tagRepository.findByTitle("JPA");
            newStudy.getTags().add(jpa);

        }
    }
}
