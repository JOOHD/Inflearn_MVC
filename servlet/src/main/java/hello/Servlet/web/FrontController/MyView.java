package main.java.Servlet.web.FrontController;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class MyView {

    private String viewPath;

    public MyView(String viewPath) {
        this.viewPath = viewPath;
    }

    public void render(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response);
    }

    public void render(Map<String, Object> model, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // model.forEach((key, value) -> request.setAttribute(key, value));를
        // modelToRequestAttribute 메소드로 만든다.
        modelToRequestAttribute(model, request);// 메소드 작업이 끝나면, JSP forward가 되면, JSP가 request.getAttribute를 쓴다.
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response);
    }

    // model에 있는 값을 jsp에게 request.getAttribute를 쓰는데,
    // model에 있는 값을 forEach로 꺼내서 람다를 이용해서 setAttribute에 key, value로해서 request값을 담는다.
    private void modelToRequestAttribute(Map<String, Object> model, HttpServletRequest request) {
        model.forEach((key, value) -> request.setAttribute(key, value));
    }
}