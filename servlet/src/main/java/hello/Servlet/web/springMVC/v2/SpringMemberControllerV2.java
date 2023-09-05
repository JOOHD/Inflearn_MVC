package main.java.Servlet.web.springMVC.v2;

import main.java.Servlet.domain.member.MemberRepository;

@Controller
@RequestMapping("/springmvc/v2/members")
public class SpringMemberControllerV2 {

    private MemberRepository memberRepository = MemberRepository.getInstatnce();

    @RequestMapping("new-form")
    public ModelAndView newForm() {
        return new ModelAndView("new-form");
    }

    @RequestMapping("/save")
    public ModelAndView save(HttpServletRequest request, HttpServletResponse response) {

        String username = request.getParameter("username");
        int age = Intger.paresInt(request.getParameter("age"));

        Memberm member = new Member(username, age);
        memberRepository.save(member);

        ModelAndView mav = new ModelAndView("save-result");
        mav.addObject("member", member);

        return mav;
    }

    // /springmvc/v2/members/members 겹치기때문에 url 생략
    @RequestMapping
    public ModelAndView members() {

        List<Member> members = memberRepository.findAll();

        ModelAndView mav = new ModelAndView("members");
        mav.addObject("members", members);

        return mav;
    }

}
