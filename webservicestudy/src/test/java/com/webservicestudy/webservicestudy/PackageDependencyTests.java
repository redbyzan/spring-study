package com.webservicestudy.webservicestudy;


import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

//archunit 아키텍처 테스트 유틸리티 디펜덴시 추가
// WebServiceStudyApplication에 들어있는 패키지들을 검사하겠다는 뜻
// account에서는 tag랑 zone만 참조하고 study랑 event를 참조하면 안됬다.
// 그런데 그걸 다 눈으로 보면서 하기 힘들기에 툴을 사용하는 것이다.
@AnalyzeClasses(packagesOf = WebServiceStudyApplication.class)
public class PackageDependencyTests {

    private static final String STUDY = "..modules.study..";
    private static final String EVENT = "..modules.event..";
    private static final String ACCOUNT = "..modules.account..";
    private static final String TAG = "..modules.tag..";
    private static final String ZONE = "..modules.zone..";

    @ArchTest
    //
    ArchRule modulesPackageRule = classes().that().resideInAPackage("com.webservicestudy.webservicestudy.module(*)..")
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage("com.webservicestudy.webservicestudy.module(*)..");

    @ArchTest
    // study 패키지 안에 있는 클래스들은 스터디와 event에서만 접근이 가능해야한다는 의미로 작성
    ArchRule studyPackageRule = classes().that().resideInAPackage(STUDY)
            .should().onlyBeAccessed().byClassesThat()
            .resideInAnyPackage(STUDY,EVENT);

    @ArchTest
    // event 패키지는 study event account를 참조한다
    ArchRule eventPackageRule = classes().that().resideInAPackage(EVENT)
            .should().accessClassesThat().resideInAnyPackage(STUDY,EVENT,ACCOUNT);

    @ArchTest
    // event 패키지는 study event account를 참조한다
    ArchRule accountPackageRule = classes().that().resideInAPackage(ACCOUNT)
            .should().accessClassesThat().resideInAnyPackage(ZONE,TAG,ACCOUNT);

    @ArchTest
    // 모듈에 있는 것들을 조각내서 그들 간의 순환참조가 있으면 안된다.
    ArchRule cycleCheck = slices().matching("com.webservicestudy.webservicestudy.module(*)..")
            .should().beFreeOfCycles();


}
