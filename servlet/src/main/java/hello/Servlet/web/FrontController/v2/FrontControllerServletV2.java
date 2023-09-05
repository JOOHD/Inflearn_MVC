package main.java.Servlet.web.FrontController.v1;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import main.java.Servlet.web.FrontController.v1.ControllerV1;
import main.java.Servlet.web.FrontController.v2.controller.MemberFormControllerV2;
import main.java.Servlet.web.FrontController.v2.controller.MemberListControllerV2;
import main.java.Servlet.web.FrontController.v2.controller.MemberSaveControllerV2;

// urlPattern = "/front-controller/v1/*" /*뒤에는 v1폴더 하위의 경로의 모든 폴더 폼함해서 불러올 수 있다.
@WebServlet(name = "frontControllerServletV2", urlPatterns = "/front-controller/v2/*")
public class FrontControllerServletV2 extends HttpServlet {

    private Map<String, ControllerV2> controllerMap = new HashMap<>();

    public FrontControllerServletV2() {
        controllerMap.put("/front-controller/v2/members/new-form", new MemberFormControllerV2());
        controllerMap.put("/front-controller/v2/members/save", new MemberSaveControllerV2());
        controllerMap.put("/front-controller/v2/members/", new MemberListControllerV2());
    }

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // /front-controller/v2/members
        String requestURI = request.getRequestURI();

        // ControllerV1 controller = new MemberListControllerV1();
        ControllerV2 controller = controllerMap.get(requestURI);
        if (controller == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        //new MyView("/WEB-INF/views/new-form.jsp");
        MyView view = controller.process(request, response);
        view.render(request, response);-
    }
}
