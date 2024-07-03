package src.main.java.hello.Servlet.web.FrontController.v3.controller;

import src.main.java.hello.Servlet.web.FrontController.v3.ControllerV3;

import java.util.Map;

public class MemberSaveControllerV3 implements ControllerV3 {

    private main.java.Servlet.domain.member.MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public main.java.Servlet.web.FrontController.ModelView process(Map<String, String> paramMap) {

        String username = paramMap.get("username");
        int age = Integer.parseInt(paramMap.get("age"));

        Member member = new Member(username, age);
        memberRepository.save(member);

        main.java.Servlet.web.FrontController.ModelView mv = new main.java.Servlet.web.FrontController.ModelView("save-result");
        mv.getModel().put("member", member);

        return mv;
    }

}
