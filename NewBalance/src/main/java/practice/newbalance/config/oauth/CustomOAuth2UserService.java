package practice.newbalance.config.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import practice.newbalance.domain.member.Member;
import practice.newbalance.repository.MemberRepository;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");

        Member member = memberRepository.findByEmail(email)
                .orElseGet(()-> {
                   Member newmember = new Member();
                    newmember.setEmail(email);
                    newmember.setRole("ROLE_USER");
                    newmember.setProvider(userRequest.getClientRegistration().getRegistrationId());
                    return memberRepository.save(newmember);
                });

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(member.getRole())),
                oAuth2User.getAttributes(),
                "email" // 주된 사용자 식별 key
        );
    }
}
