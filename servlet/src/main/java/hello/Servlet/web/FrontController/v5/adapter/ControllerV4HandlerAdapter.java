package main.java.Servlet.web.FrontController.v5.adapter;

import main.java.Servlet.web.FrontController.ModelView;
import main.java.Servlet.web.FrontController.v3.ControllerV3;
import main.java.Servlet.web.FrontController.v5.MyHandlerAdapter;

public class ControllerV4HandlerAdapter implements MyHandlerAdapter {

    @Override
    public boolean supports(Object handler) {
        return (handler instanceof ControllerV3);// ControllerV3만 지원하겠다.
        // 이게 ControllerV3 야? 라고 물어보는, ControllerV3 가 맞으면 true, 아니면 false
    }

    @Override
    public ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        ControllerV3 controller = (ControllerV3) handler;

        Map<String, String> paramMap = createParamMap(request);

        ModelView mv = controller.process(paramMap);
        return mv;
    }

    private Map<String, String> createParamMap(HttpServletRequest request) {
        Map<String, String> paramMap = new HashMap<>();
        request.getParameterNames().asIterator()
                .forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
        return paramMap;
    }
}
