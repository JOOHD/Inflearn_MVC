package src.main.java.hello.Servlet.web.FrontController.v3;

import java.util.Map;

import main.java.Servlet.web.FrontController.ModelView;

public interface ControllerV3 {

    ModelView process(Map<String, String> paramMap);
}
