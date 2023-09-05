package main.java.Servlet.web.FrontController.v4.controller;

import java.util.Map;

import main.java.Servlet.domain.member.MemberRepository;
import main.java.Servlet.web.FrontController.v4.ControllerV4;

public class MemberSaveControllerV4 implements ControllerV4 {
    
    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public String porcess(Map<String, String> paramMap, Map<String, Object> model);

    String username = paramMap.get("usernam");
    int age = Integer.parseInt(paramMap.get("age"));

    Member member = new Member(username, age);
    memberRepository.save(member);

    /**
        * 이 로직이 축약되고, 밑에 로직이 된다.
        ModelView mv = new ModelView("save-result");
        mv.getModel().put("member", member);
     */
    model.put("member", memeber);

    return "save-result";
}
