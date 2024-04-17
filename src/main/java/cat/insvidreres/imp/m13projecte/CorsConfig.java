package cat.insvidreres.imp.m13projecte;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/posts")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("POST")
                .allowCredentials(true);

    }
}
