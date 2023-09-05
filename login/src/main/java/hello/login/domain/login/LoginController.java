package hello.login.domain.login;

import hello.login.domain.member.Member;
import hello.login.web.SessionConst;
import hello.login.web.login.LoginForm;
import hello.login.web.session.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.*;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final SessionManager sessionManager;

    // GET = 조회, POST = 생성, 수정
    @GetMapping("/login") //첫 로그인 페이지,
    public String loginForm(@ModelAttribute("loginForm") LoginForm form) {
        return "login/loginForm";
    }

    //@PostMapping("/login")
    public String login(@Validated @ModelAttribute LoginForm form, BindingResult bindingResult, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return "lgoin/loginForm"; //로그인 페이지 return
        }

        Member loginMember = loginService.login(form.getLoginId(), form.getPassword()); //memberRepository call up

        if (loginMember == null) { //로그인 실패 시,
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "login/loginForm";
        }

        //로그인 성공 처리
        // type 이 Long 이여서 String.valueOf
        // getId = sequence 값
        Cookie idCookie = new Cookie("memberId", String.valueOf(loginMember.getId())); 
        response.addCookie(idCookie);
        return "redirect:/";
    }

    //@PostMapping("/login")
    public String loginV2(@Validated @ModelAttribute LoginForm form, BindingResult bindingResult, HttpServletResponse response) {
        if (bindingResult.hasErrors()) {
            return "lgoin/loginForm"; //로그인 페이지 return
        }

        Member loginMember = loginService.login(form.getLoginId(), form.getPassword()); //memberRepository call up

        if (loginMember == null) { //로그인 실패 시,
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "login/loginForm";
        }
        
        //로그인 성공 처리
        //세션 관리자를 통해 세션을 생성하고, 회원 데이터 보관
        sessionManager.createSession(loginMember, response);
        return "redirect:/";
    }

    @PostMapping("/login")
    public String loginV3(@Validated @ModelAttribute LoginForm form, BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            return "lgoin/loginForm"; //로그인 페이지 return
        }

        Member loginMember = loginService.login(form.getLoginId(), form.getPassword()); //memberRepository call up

        if (loginMember == null) { //로그인 실패 시,
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "login/loginForm";
        }

        //로그인 성공 처리
        //세션이 있으면 있는 세션 반환, 없으면 신규 세션을 생성
        HttpSession session = request.getSession(true); //세션 생성을 원하면, true(default) 생략해도 됨.
        //세션에 로그인 화원 정보 보관
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);

        //세션 관리자를 통해 세션을 생성하고, 회원 데이터 보관
//        sessionManager.createSession(loginMember, response);
        return "redirect:/";
    }

    //@PostMapping("/logout") //세션 쿠키이므로 웹 브라우저 종료시
    public String logout(HttpServletResponse response) {
        expireCookie(response, "memberId");
        return "redirect:/";
    }
    //@PostMapping("/logout") //세션 쿠키이므로 웹 브라우저 종료시
    public String logoutV2(HttpServletRequest request) {
        sessionManager.expire(request);
        return "redirect:/";
    }

    @PostMapping("/logout") //세션 없애는게 목적이라, false
    public String logoutV3(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

    private void expireCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0); //서버에서 해당 쿠키의 날짜를 0으로 지정
        response.addCookie(cookie);
    }
}
