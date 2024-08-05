package practice.newbalance;

import jakarta.servlet.http.HttpSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;
import java.util.UUID;

@EnableJpaAuditing
@SpringBootApplication
public class NewBalanceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewBalanceApplication.class, args);
    }

    @Bean
    public AuditorAware<String> auditorProvider(HttpSession session){
        return () -> Optional.of(session.getAttribute("principal").toString());
//        return () -> Optional.of(UUID.randomUUID().toString());
    }
}
