package main.java.Servlet.web.FrontController.v5.adapter;

import java.util.HashMap;
import java.util.Map;

import main.java.Servlet.web.FrontController.ModelView;
import main.java.Servlet.web.FrontController.v4.ControllerV4;
import main.java.Servlet.web.FrontController.v5.MyHandlerAdapter;

public class ControllerV3HandlerAdapter implements MyHandlerAdapter {

    @Override
    public boolean supports(Object handler) {
        return (handler instanceof ControllerV4);
    }

    @Override
    public ModelView handle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        ControllerV4 controller = (ControllerV4) handler;

        Map<String, String> paramMap = createParamMap(request);
        Map<String, Object> model = new HashMap<>();

        String viewname = controller.process(paramMap, model);

        // viewname을 return받으면 error, 이제 adapter의 110vf를 220v로 맞춰주는 역할이 나온다.
        ModelView mv = new ModelView(viewname);
        mv.setModel(model);

        return mv;
    }

    private Map<String, String> createParamMap(HttpServletRequest request) {
        Map<String, String> paramMap = new HashMap<>();
        request.getParameterNames().asIterator()
                .forEachRemaining(paramName -> paramMap.put(paramName, request.getParameter(paramName)));
        return paramMap;
    }
}
