package src.Servlet;

import main.java.Servlet.web.springMVC.v1.SpringMemberFormControllerV1;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.ServletComponentScan;
import org.springframework.context.annotation.Bean;

@ServletComponentScan // 서블릿 자동 등록
@SpringBootApplication
public class ServletApplication {

    public static void public static void main(String[] args) {
        SpringApplication.run(ServletApplication.class, args);
    }

    @Bean // @Controller 사용 안하고, @Component 일때,
    SpringMemberFormControllerV1 springMemberFormControllerV1() {
        return new SpringMemberFormControllerV1();
    }
}
