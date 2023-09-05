package main.java.Servlet.web.FrontController.v3.controller;

import java.util.List;
import java.util.Map;

import main.java.Servlet.domain.member.MemberRepository;
import main.java.Servlet.web.FrontController.ModelView;
import main.java.Servlet.web.FrontController.v4.ControllerV3;

public class MemberListControllerV4 implements ControllerV4 {
    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    public ModelView process(Map<String, String> paramMap) {
        List<Member> members = memberRepository.findAll();

        // ModelView mv = new ModelView("members");
        // mv.getModel().put("members", members);

        // return mv;
        model.put("members", members);
        return "members";
    }
}
