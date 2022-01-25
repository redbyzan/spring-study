package com.example.springsecurity.config.security.login.service;

import com.example.springsecurity.config.security.login.dto.MemberPrincipal;
import com.example.springsecurity.config.security.login.dto.OAuthAttributes;
import com.example.springsecurity.member.entity.Member;
import com.example.springsecurity.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class CustomOauth2Service extends DefaultOAuth2UserService  {

    private final MemberRepository memberRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest){
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // 네이버인지 구글인지 확인(google or naver) -> yml에 적은것
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName(); // Oauth2 로그인 진행 시 키가 되는 필드값(primary key) -> 구글의 경우는 sub로 들어오나 naver와 카카오는 지원하지 않음

        OAuthAttributes attributes = OAuthAttributes.of(registrationId,userNameAttributeName,oAuth2User.getAttributes()); // OAuth2UserService를 통해 가져온 Oauth2User의 attribute를 담을 클래스

        Member member = saveOrUpdate(attributes);

        return MemberPrincipal.of(member,attributes.getAttributes());

    }

    private Member saveOrUpdate(OAuthAttributes attributes) {

        Optional<Member> memberOptional = memberRepository.findByEmailAndSocial(attributes.getEmail(), true);
        Member member;
        if (memberOptional.isPresent()){
            member = memberOptional.get();
            member.update(attributes.getName(),attributes.getPicture());
        }
        else{
            member =  memberRepository.save(attributes.toMemberEntity());
        }

        return member;
    }
}
