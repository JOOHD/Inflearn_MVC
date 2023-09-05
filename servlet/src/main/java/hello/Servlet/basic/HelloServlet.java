package main.java.Servlet.basic;

import javax.serlvet.annotation.WebServlet;
import javax.serlvet.http.HttpServlet;

//servlet 요청 응답
@WebServlet(name = "helloServlet", urlPatterns = "/hello")
public class HelloServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException {

        System.out.println("helloServlet.service");
        System.out.println("request = " + request);
        System.out.println("response = " + response);

        //localhost:8080/hello 아무것도 없는 빈 화면이 나온다.
        //localhost:8080/hello?username=joo (query parameter)

        String username = request.getParamter("username");
        System.out.println("username = " + username);

        response.setContentType("text/plain");  //단순문자
        response.setCharacterEncoding("utf-8"); //인코딩, 한글 패치 httpHeader 부분에 들어간다.
        response.getWriter().write("hello" + username); //write httpBody 메시지에 들어간다.

    }
}