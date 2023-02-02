package com.example.sbb.service.user;

import com.example.sbb.dto.OAuthAttributes;
import com.example.sbb.dto.UserSessionDto;
import com.example.sbb.entity.user.SiteUser;
import com.example.sbb.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Security UserDetailsService == OAuth OAuth2UserService
 * */
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final HttpSession session;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        /* OAuth2 서비스 id 구분코드 (구글, 카카오, 네이버) */
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        /* OAuth2 로그인 진행시 키가 되는 필드 값 (PK) (구글의 기본 코드는 "sub") */
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        /* OAuth2UserService */
        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        SiteUser siteUser = saveOrUpdate(attributes);

        /* 세션 정보를 저장하는 직렬화된 dto 클래스 */
        session.setAttribute("siteUser", new UserSessionDto(siteUser));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(siteUser.getRoleValue())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey());
    }

    /* 소셜로그인시 기존 회원이 존재하면 수정날짜 정보만 업데이트해 기존의 데이터는 그대로 보존 */
    private SiteUser saveOrUpdate(OAuthAttributes attributes) {
        SiteUser siteUser = userRepository.findByEmail(attributes.getEmail())
                .map(SiteUser::updateModifiedDate)
                .orElse(attributes.toEntity());
        // TODO : 이메일이 같은 소셜 로그인 진행시 에러 처리해야함 이 방법으로 안됨
        if (!siteUser.getProvider().equals(attributes.getProvider())) {
            throw new OAuth2AuthenticationException("이미 있는 계정이거나 다른 소셜로그인이 진행되었습니다. 다른 방식으로 로그인 해주세요.");
        }
        return userRepository.save(siteUser);
    }
}
