package main.java.Servlet.web.FrontController.v4.controller;

import java.util.Map;

import main.java.Servlet.web.FrontController.v4.ControllerV4;

public class MemberFormControllerV4 implements ControllerV4 {

    @Override
    public String process(Map<String, String> paramMap, Map<String, Object> model) {
        return "new-form";
    }
}
