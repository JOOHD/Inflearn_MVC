package main.java.Servlet.web.servletmvc;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "mvcMemberSaveServlet", urlPatterns = "/servlet-mvc/members/save")
public class MvcMemberSaveServlet extends HttpServlet {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));

        Member member = new Member(username, age);
        memberRepository.save(member);// 비즈니스 로직 호출

        // Model에 데이터 보관
        request.setAttribute("member", member);// request내부 저장소에 map형태의 member라는 name을 가진 객체가 저장이된다.

        String viewPath = "/WEB-INF/views/save-reuslt.jsp";
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPaht);
        dispatcher.forward(request, response);
    }
}