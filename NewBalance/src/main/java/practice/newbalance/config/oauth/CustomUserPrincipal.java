package practice.newbalance.config.oauth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import practice.newbalance.domain.member.Member;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomUserPrincipal implements OAuth2User {
    private final Member member;
    private final Map<String, Object> attributes;
    public CustomUserPrincipal(Member member, Map<String, Object> attributes) {
        this.member = member;
        this.attributes = attributes;
    }
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(member.getRole()));
    }

    @Override
    public String getName() {
        return member.getEmail();
    }

    public Member getMember(){
        return member;
    }

}
