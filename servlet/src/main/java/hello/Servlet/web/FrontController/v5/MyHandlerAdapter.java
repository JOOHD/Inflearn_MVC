package main.java.Servlet.web.FrontController.v5;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import main.java.Servlet.web.FrontController.ModelView;

public interface MyHandlerAdapter {

    boolean supports(Object handler);

    // Object 로 handler를 생성한 이유는, 유연하게 받기 위해서 최상위 클래스이기 때문에.
    ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws ServletException, IOException;
}
