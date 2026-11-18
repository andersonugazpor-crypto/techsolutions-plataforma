package pe.techsolutions.plataforma.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import pe.techsolutions.plataforma.seguridad.InterceptorAutenticacion;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final InterceptorAutenticacion interceptorAutenticacion;

    public WebConfig(InterceptorAutenticacion interceptorAutenticacion) {
        this.interceptorAutenticacion = interceptorAutenticacion;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptorAutenticacion)
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/css/**", "/js/**", "/img/**", "/h2-console/**", "/error", "/favicon.ico");
    }
}
