package com.example.sbb.dto;

import com.example.sbb.entity.user.SiteUser;
import com.example.sbb.entity.user.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Builder
@Getter
public class OAuthAttributes {
    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String username;
    private String email;
    private UserRole userRole;
    private String providerId;
    private String provider;

    public static OAuthAttributes of(String registrationId,
                                     String userNameAttributeName,
                                     Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return ofNaver("id", attributes, registrationId);
        }
        if ("Kakao".equals(registrationId)) {
            return ofKakao("id", attributes, registrationId);
        }
        /* 구글인지 네이버인지 카카오인지 구분하기 위한 메소드 (ofNaver, ofKaKao) */
        return ofGoogle(userNameAttributeName, attributes, registrationId);
    }

    private static OAuthAttributes ofNaver(String usernameAttributeName, Map<String, Object> attributes, String registrationId) {
        /* JSON 형태이기 때문에 Map을 통해 데이터를 가져온다. */
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        log.info("naver response : " + response);

        return OAuthAttributes.builder()
                .username((String) response.get("nickname"))
                .email((String) response.get("email"))
                .providerId((String) response.get("id"))
                .provider(registrationId)
                .attributes(response)
                .nameAttributeKey(usernameAttributeName)
                .build();
    }

    private static OAuthAttributes ofKakao(String usernameAttributeName, Map<String, Object> attributes, String registrationId) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");

        return OAuthAttributes.builder()
                .username((String) kakaoProfile.get("nickname"))
                .email((String) kakaoAccount.get("email"))
                .providerId((String) kakaoAccount.get("id"))
                .provider(registrationId)
                .attributes(attributes)
                .nameAttributeKey(usernameAttributeName)
                .build();
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName,
                                            Map<String, Object> attributes,
                                            String registrationId) {
        return OAuthAttributes.builder()
                .username((String) attributes.get("name"))
                .email((String) attributes.get("email"))
                .providerId((String) attributes.get("sub"))
                .provider(registrationId)
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public SiteUser toEntity() {
        return SiteUser.builder()
                .username(username)
                .email(email)
                .providerId(providerId)
                .provider(provider)
                .role(UserRole.SOCIAL)
                .build();

    }
}
