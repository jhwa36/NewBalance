package practice.newbalance.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import practice.newbalance.service.item.FileUtils;
import practice.newbalance.utils.InfoUtils;

@Configuration
public class AppConfig {

    @Bean
    public FileUtils fileUtils(){
        return new FileUtils();
    }

    @Bean
    InfoUtils infoUtils(){
        return new InfoUtils();
    }
}
