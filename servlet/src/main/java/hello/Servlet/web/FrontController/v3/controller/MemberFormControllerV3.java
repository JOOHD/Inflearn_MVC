package src.main.java.hello.Servlet.web.FrontController.v3.controller;

import src.main.java.hello.Servlet.web.FrontController.v3.ControllerV3;

import java.util.Map;

public class MemberFormControllerV3 implements ControllerV3 {

    @Override
    public main.java.Servlet.web.FrontController.ModelView process(Map<String, String> paramMap) {
        return new main.java.Servlet.web.FrontController.ModelView("new-form");
    }
}