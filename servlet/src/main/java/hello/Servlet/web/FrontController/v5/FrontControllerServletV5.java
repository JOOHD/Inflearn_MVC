package main.java.Servlet.web.FrontController.v5;

import hello.servlet.web.frontcontroller.ModelView;
import hello.servlet.web.frontcontroller.MyView;
import hello.servlet.web.frontcontroller.v3.controller.MemberFormControllerV3;
import hello.servlet.web.frontcontroller.v3.controller.MemberListControllerV3;
import hello.servlet.web.frontcontroller.v3.controller.MemberSaveControllerV3;
import hello.servlet.web.frontcontroller.v5.adapter.ControllerV3HandlerAdapter;
import hello.servlet.web.frontcontroller.v5.adapter.ControllerV4HandlerAdapter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet(name = "frontControllerServletV5", urlPatterns = "/front-controller/v5/*")
public class FrontControllerServletV5 extends HttpServlet {
    private final Map<String, Object> handlerMappingMap = new HashMap<>();
    private final List<MyHandlerAdapter> handlerAdapters = new ArrayList<>();

    public FrontControllerServletV5() {// 순차적으로 선언해주는 것이 좋다. 아래 Mapping, Adapters 순서에 맞게 맞춰주
        initHandlerMappingMap();// 핸들러 매핑 초기화
        initHandlerAdapters();// 어뎁터 초기화
    }

    private void initHandlerMappingMap() { // Map<String, Object>이기에 모든 객체가 담길 수 있다.
        // 만약 /v5를 빼고, 경로를 적어주면, 404 에러가 발생하게 된다. 그러면
        // application.property, logging.level.org.apache.coyote.http11=debug
        // 요청 url 정보 : [GET /front-controller/v5/v3/members/new-form Http/1.1]
        // "front-controller/v5/*" 하위의 경로들은 다 여기로와.
        handlerMappingMap.put("/front-controller/v5/v3/members/new-form", new MemberFormControllerV3());
        handlerMappingMap.put("/front-controller/v5/v3/members/save", new MemberSaveControllerV3());
        handlerMappingMap.put("/front-controller/v5/v3/members", new MemberListControllerV3());

        // v4 추가
        handlerMappingMap.put("/front-controller/v5/v4/members/new-form", new MemberFormControllerV4());
        handlerMappingMap.put("/front-controller/v5/v4/members/save", new MemberSaveControllerV4());
        handlerMappingMap.put("/front-controller/v5/v4/members", new MemberListControllerV4());

        hadlerAdapters.add(new ControllerV4HandlerAdapter());
    }

    private void initHandlerAdapters() {
        handlerAdapters.add(new ControllerV4HandlerAdapter());
        handlerAdapters.add(new ControllerV4HandlerAdapter());
    }

    // 1.핸들러 매핑 정보 조회
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // String requestURI = request.getRequestURI(); // 요청이 오면,
        // Object handler = handlerMappingMap.get(requestURI);
        // handlerMappingMap에서 requestURI로 보내서 handler를 찾는다.

        // MemberFormControllerV3
        Object handler = getHandler(request); // 1.핸들러 호출
        if (handler == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // ControllerV3HandlerAdapter
        MyHandlerAdapter adapter = getHandlerAdapter(handler); // 3.핸들러 어뎁터 호출

        ModelView mv = adapter.handle(request, response, handler); // 4.handler 호출, ModelView 반환

        // MemberFormControllerV3
        // ControllerV3 controller = (ControllerV3) handler; // handler 호출 후, v3로 캐스팅하고,

        // Map<String, String> paramMap = createParamMap(request);

        // ModelView mv = controller.process(paramMap); // controller의 process 호출,

        // return mv; // 5.ModelView 반환

        MyView view = viewResolver(mv.getViewName()); // 6. ViewResolver 반환
        view.render(mv.getModel(), request, response); // 7. MyView 반환 8.render(model) 호출
    }

    ////////////////////// 리팩토링 //////////////////////////////////////
    private Object getHandler(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return handlerMappingMap.get(requestURI);
    }

    // 2.핸들러 어댑터 목록 조회(핸들러를 처리할 수 있는 핸들러 어뎁터 조회)
    private MyHandlerAdapter getHandlerAdapter(Object handler) { // 반환, controllerV3가 왔으면,
        // MemberFormControllerV3
        for (MyHandlerAdapter adapter : handlerAdapters) { // handlerAdapters.add를 roof를 돌려서 찾으면된다.
            if (adapter.supports(handler)) { // 만약 adapter가 support를 지원하는냐, 만약 for문을 돌려서 adapter에 v3가 있으면,
                                             // support 호출,
                                             // adapter가 v3 처리
                                             // return (handler instanceof ControllerV3) handler;
                return adapter; // 지원하면(true) adapter를 반환해주면 된다.
            }
        }
        // 못찾을 시, 최후의 방법.
        throw new IllegalArgumentException("handler adapter를 찾을 수 없습니다. handler=" + handler);
    }

    private MyView viewResolver(String viewName) {
        return new MyView("/WEB-INF/views/" + viewName + ".jsp");
    }
}
