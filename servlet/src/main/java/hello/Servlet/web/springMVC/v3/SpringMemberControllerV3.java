package main.java.Servlet.web.springMVC.v3;

import hello.servlet.domain.member.Member;
import hello.servlet.domain.member.MemberRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

/**
 * v3 Model 도입 ViewName 직접 반환
 * 
 * @RequestParam 사용
 * @RequestMapping -> @GetMapping, @PostMapping
 */
@Controller
@RequestMapping("/springmvc/v3/members")
public class SpringMemberControllerV3 {
    private MemberRepository memberRepository = MemberRepository.getInstance();

    @GetMapping("/new-form") // RequestMethod.GET
    public String newForm() {

        // return new ModelAndView("new-form");
        return "new-form";
    }

    @PostMapping("/save") // RequestMethod.POST
    // ModelAndView save(HttpServletRequest request, HttpServletResponse response)
    public String save(@RequestParam("username") String username, @RequestParam("age") int age, Model model) {

        Member member = new Member(username, age);
        memberRepository.save(member);

        // ModelAndView mv = new ModelAndView("save-result");
        model.addAttribute("member", member);

        return "save-result";
    }

    @GetMapping // RequestMethod.GET
    public String members(Model model) {

        List<Member> members = memberRepository.findAll();

        model.addAttribute("members", members);

        return "members";
    }
}