package main.java.Servlet.web.servlet;

import hello.servlet.domain.member.Member;
import hello.servlet.domain.member.MemberRepository;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "memberSaveServlet", urlPatterns = "/servlet/members/save")
public class MemberSaveServlet extends HttpServlet {

    private MemberRepository memberRepository = MemberRepository.getInstance();

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("MemberSaveServlet.service");

        // form에서 온 데이터를 getParameter로 꺼낸다.
        String username = request.getParameter("username");
        // request.getParameter는 응답 결과는 항상 문자형 타입을 꺼낸다. 그래서 문자타입 형변환 해주어야 된다.
        int age = Integer.parseInt(request.getParameter("age"));

        // 비즈니스 로직 작성하고,
        Member member = new Member(username, age);
        System.out.println("member = " + member);

        // member 객체를 Repository에 저장한다.
        memberRepository.save(member);

        // 결과를 html로 응답한다.
        response.setContentType("text/html");
        response.setCharacterEncoding("utf-8");

        PrintWriter w = response.getWriter();
        // 동적 코드
        w.write("<html>\n" + "<head>\n" + " <meta charset=\"UTF-8\">\n" + "</head>\n" + "<body>\n" + "성공\n" + "<ul>\n"
                + " <li>id=" + member.getId() + "</li>\n" + " <li>username=" + member.getUsername() + "</li>\n"
                + " <li>age=" + member.getAge() + "</li>\n" + "</ul>\n" + "<a href=\"/index.html\">메인</a>\n"
                + "</body>\n" + "</html>");
    }

}
