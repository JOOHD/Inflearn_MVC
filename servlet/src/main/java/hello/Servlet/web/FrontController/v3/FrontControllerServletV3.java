package src.main.java.hello.Servlet.web.FrontController.v3;

import hello.servlet.web.frontcontroller.ModelView;
import hello.servlet.web.frontcontroller.MyView;
import hello.servlet.web.frontcontroller.v3.controller.MemberFormControllerV3;
import hello.servlet.web.frontcontroller.v3.controller.MemberListControllerV3;
import hello.servlet.web.frontcontroller.v3.controller.MemberSaveControllerV3;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "frontControllerServletV3", urlPatterns = "/front-controller/v3/*") // servlet호출
public class FrontControllerServletV3 extends HttpServlet {
    private Map<String, ControllerV3> controllerMap = new HashMap<>();

    // 1.컨트롤러 조회
    public FrontControllerServletV3() {
        controllerMap.put("/front-controller/v3/members/new-form", new MemberFormControllerV3());
        controllerMap.put("/front-controller/v3/members/save", new MemberSaveControllerV3());
        controllerMap.put("/front-controller/v3/members", new MemberListControllerV3());
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 2.컨트롤러 호출
        ControllerV3 controller = controllerMap.get(requestURI);// /front-controller/v3/members/new-form
        if (controller == null) {// get(requestURI) = null 이면
            response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404
            return;
        }

        // 3.ModelView 반환
        // createParamMap, porcess 는 메서드로 기능을 분리해 필요한 객체에 성격을 부여하는것 처럼 호출하는 것인듯.
        Map<String, String> paramMap = createParamMap(request);
        ModelView mv = controller.process(paramMap);

        // 4.viewResolver를 호출
        String viewName = mv.getViewName(); // 논리이름 new-form
        MyView view = viewResolver(viewName); // MyView 반환

        // 6.render(model) 호출, model을 같이 넘겨줘야 된다.
        view.render(mv.getModel(), request, response); // MyView view.render이니까 MyView 클래스에서 render 메소드를 만들어야한다.
    }
    // detail한 로직들은 메소드로 뽑는게 좋다. (리펙토링)

    // 5.MyView를 반환
    private MyView viewResolver(String viewName) {// 그리고 실제 물리 경로가 있는 MyView 객체를 반환한다.
        return new MyView("/WEB-INF/views/" + viewName + ".jsp"); // "WEB-INF/views/new-form.jsp"
    }

    // paramMap
    private Map<String, String> createParamMap(HttpServletRequest request) {
        Map<String, String> paramMap = new HashMap<>();// Map 생성
        request.getParameterNames().asIterator()// request의 paramNames 모두를 가져온다.
                .forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
        return paramMap;// request.getParamNames의 모든 Names를 꺼내서 paramMap에 데이터를 담는다.
    }
}