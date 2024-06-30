# 본문

### 회원 domain model

    @Getter @Setter
    public class Member {
        private Long id;
        private String username;
        private int age;
    }

### 회원 Repository(싱글톤 패턴 적용)

    최대한 스프링 없이 순수 서블릿 만으로 구현이 목적

    회원 Repository는 싱글톤 패턴 적용,
    (객체의 인스턴스 단 하나만 생성해서 공유, 생성자 private 접근자로 막아둔다.)


### 싱글톤 장점
    최초의 new 연산자를 통해서 고정된 메모리 영역을 사용하기 때문에 추후 해당 객체에 접근할 때 메모리 낭비를 방지 및 다른 클래스 간에 데이터 공유가 편리.

### 싱글톤 단점 
    싱글톤 패턴을 구현하는 코드 자체가 많이 필요,
    정적 메서드에서 객체 생성을 확인하고 생성자를 호출하는 멀티쓰레딩 환경에서 동시성 문제 발생.

##  Member Repository
    public class MemberRepository {

        private static Map<Long, Member> store = new HashMap<(); // static 사용
        private static long sequence = 0L;

        public static MemberRepository getInstance() {
            return instance;
        }

        public Member save(Member member) {
            member.setId(++sequende);
            store.put(member.getId(), member);
            return member;
        }

        public Member findById(Logn id) {
            return store.get(id);
        }

        public List<Member> findAll() {
            return new ArrayList<>(store.values());
        }

        public void clearStore() {
            store.clear();
        }
    }

## 서블릿으로 회원 관리 웹 애플리케이션 만들기
###  MemberFormServlet - 회원 등록 폼    
    @WebServlet(name = "memberFormServlet", urlPatterns = "/servlet/members/new-form")
    public class MemberFormSerlvet extends HttpServlet {

        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.setContentType("text/html");
            response.setCharacterEncoding("utf-8");

            PrintWriter w = response.getWriter();
            w.write("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Title</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<form action=\"/servlet/members/save\" method=\"post\">\n" +
                "    username: <input type=\"text\" name=\"username\" />\n" +
                "    age:      <input type=\"text\" name=\"age\" />\n" +
                "    <button type=\"submit\">전송</button>\n" +
                "</form>\n" +
                "</body>\n" +
                "</html>\n")
        }
    }

    MemberFormServlet은 단순하게 회원 정보를 입력할 수 있는 HTML-Form을 만들어서 응답한다.(자바 코드로 HTML을 제공해야 하므로 쉽지 않다.)

### MemberSaveServlet - 회원 저장
    @WebServlet(name = "memberSaveServlet", urlPatterns = "/servlet/members/save") 
    public class MemberSaveServlet extends HttpServlet {

        private MemberRepository memberRepository = MemberRepository.getInstance();

        @Override
        protected void service(HttpServletRequest request, ServletResponse response) throws ServletException, IOException {

            String username = request.getParameter("username");
            int age = Integer.parseInt(request.getParameter("age"));

            Member member = new Member(username, age);
            memberRepository.save(member);

            response.setContentType("text/html");
            response.setCharacterEncoding("utf-8");

            PrintWriter w = response.getWriter();
        }
    }

### MemberSaveServlet 동작 순서
    1.파라미터를 조회해서 Member 객체를 만든다.
    2.Member 객체를 MemberRepository를 통해서 저장한다.
    3.Member 객체를 사용해서 결과 화면용 HTML을 동적으로 만들어서 응답한다.

### MemberListServlet - 회원 목록    
    @WebServlet(name = "memberListServlet", urlPatterns = "/servlet/members")
    public class MemberListServlet extends HttpServlet {

        private MemberRepository memberRepository = MemberRepository.getInstance();

        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.setContentType("text/html);
            response.setCharacterEncoding("utf-8");

            List<Member> members = memberRepository.findAll();

            for (Member member : members) {
            w.write("    <tr>");
            w.write("        <td>" + member.getId() + "</td>");
            w.write("        <td>" + member.getUsername() + "</td>");
            w.write("        <td>" + member.getAge() + "</td>");
            w.write("    </tr>");
            }
        }
    }

### MemberListServlet 동작 순서
    1.memberRepository.findAll()을 통해 모든 회원을 조회한다.
    2.회원 목록 HTML을 for 루프를 통해서 회원 수 만큼 동적으로 생성하고 응답한다.

## 템플릿 엔진으로
    지금까지 서블릿과 자바 코드만으로 HTML을 구현하였다.

    1.서블릿 덕분에 동적인 HTML 구현이 가능했다.
    (아직 HTML 문서의 화면이 달라지는 회원의 저장 결과라던가, 회원 목록 같은 동적인 HTML을 만드는 일은 불가능)

    2.자바 코드로 HTML을 만들어 내는 것 보다 차라리 HTML 문서에 동적으로 변경해야 하는 부분만 자바 코드를 넣을 수 있다면 더 편리할 것이다.

    그래서 HTML 문서에 동적인 자바 코드를 넣은 기술이 템플릿 엔진이다.
    (JSP, Thymeleaf, Freemaker, Velocity등이 있다.)

    JSP는 성능과 기능에서 좋은 효율을 보여주지 못하면서 점점 사장되어 가는 추세이다. 추후에는 스프링과 잘 통합되는 Thymeleaf를 사용할 것이다.

### MemberSave - 회원 저장 (JSP)
    <%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%
    // request, response 사용 가능

        MemberRepository memberRepository = MemberRepository.getInstance();

        String username = request.getParameter("username");
        int age = Integer.parseInt(request.getParameter("age"));

        Member memebr = new Member(username, age);
        memberRepository.save(member);
    %>
        <html>
        <head>
            <meta charset="UTF-8">
        </head>
        <body>
        성공
        <ul>
            <li>id=<%=member.getId()%></li>
            <li>username=<%=member.getUsername()%></li>
            <li>age=<%=member.getAge()%></li>
        </ul>
        <a href="/index.html">메인</a>
        </body>
        </html>
### 정리
    JSP를 보면, 회원 저장 서블릿 코드와 같다.
    다른 점이 있다면, HTML을 중심으로 하고, 자바코드를 입력해주었다.

### MemberList - 회원 목록 (JSP)
    MemberRepository memberRepository = MemberRepository.getInstance();
    
    List<Member> members = memberRepository.findAll();

    <table>
        <thead>
        <th>id</th>
        <th>username</th>
        <th>age</th>
        </thead>
        <tbody>
    <%
        for (Member member : members) {
            out.write("     <tr>");    
            out.write("        <td>" + member.getId() + "</ted>");
            out.write("        <td>" + member.getUsername() + "</td>");
            out.write("        <td>" + member.getAge() + "</td>");
            out.write("     </tr>");
        }
    %>
        </tbody>
    </table>

### JSP/Servlet 정리    
    서블릿으로 개발할 때는 view 화면을 위한 HTML을 만드는 작업이 자바 코드에 섞여서 지저분하다.
    JSP를 사용한 덕분에 view를 생성하는 HTML 작업을 깔끔하게 가져가고, 중간중간 동적으로 변경이 필요한 부분에만 자바 코드를 적용.

### JSP/Servlet 한계
    Serlvet에 JSP를 적용하여 깔끔한 코드를 구현했지만, 몇가지 고민이 있다.

    1.MemberSave의 jsp를 보면, 코드의 절반은 회원을 위한 비즈니스 로직이고, 나머지 절반만 결과를 HTML로 보여주기 위한 view 영역이다.

    2.JSP가 너무 많은 역할을 한다. 만약 코드가 수천줄이 넘어가면 JSP는 너무 복잡하고 방대한 양을 개발자가 부담하게 되는 문제점이 발생한다.

## MVC 패턴의 등장
    비즈니스 로직은 서블릿 처럼 다른곳에서 처리하고, JSP는 목적에 맞게 HTML로 view를 그리는 방향으로 설정.

### MVC 개요
    JSP의 많은 양의 유지보수를 보완하기 위해 JSP 같은 view 템플릿은 화면을 랜더링만 담당하도록 구현하면 된다.

### Model View Controller 분담 
    Controller : HTTP 요처을 받아서 파라미터를 검증하고, 비즈니스 로직을 실행.
    그리고 view에 전달할 결과 데이터를 조회해서 모델에 담는다.
    (controller에도 비즈니스 로직을 구현 가능하나, 너무 많은 역할을 담당하기에 service에 별도로 만들어 처리한다.)

    Model : view에 출력할 데이터를 담아둔다.
    view가 필요한 데이터를 모두 model에 담아서 전달해주는 역할.
    HttpServletReqeust 객체를 사용하고, request는 내부에 데이터 저장소를 가지고 있는데, request.set/getAttribute()로 데이터를 조회, 보관.

    View : model에 담겨있는 데이터를 사용해서 화면을 그리는 일에 집중.(HTML 생성.)

### MVC패턴2
![MVC2_pattern](./servlet_img/mvc2.png)  

### MvcMemberFormServlet (mvc패턴)
    public class MvcMemberFormServlet extends HttpServlet {

        @Override
        protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            String viewPath = "/WEB-INF/views/new-form.jsp";
            RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);

            dispatcher.forward(request, response);
        }
    }

    dispatcher.forward() : 다른 서블릿이나 JSP로 이동할 수 있는 기능.
    /WEN-INF : 이 경로안에 JSP가 있으면 외부에서 직접 JSP를 호출할 수 있다.

    Redirect vs forward
    리다이렉트는 실제 클라이언트에 응답이 나갔다가, 클라이언트가 redirect 경로로 다시 요청. 
    따라서 클라이언트가 인지할 수 있고, URL 경로도 실제로 변경.

    반면에 포워드는 서버 내부에서 일어나는 호출이기 때문에 클라이언트가 전혀 인지하지 못한다.

    <!-- 상대경로 사용, [현재 URL이 속한 계층 경로 + /save] -->
    <form action="save" method="post">
        username: <input type="text" name="username" />
        age:      <input type="text" name="age" />
        <button type="submit">전송</button>
    </form>

    위에 코드에서 form의 action을 보면 절대 경로(/로 시작)가 아니라,
    상대경로(/로 시작x)인 것을 볼 수 있다.
    이렇게 상대경로를 사용하면 form 전송시 현재 URL이 속한 계층 경로 +save가 호출된다.

    현재경로 : /servlet-mvc/members/
    결과 : /servlet-mvc/members/save

### MvcMemberSaveServlet - 회원 저장(mvc패턴)
    String username = request.getParameter("username");
    int age = Integer.parseInt(request.getParameter("age"));
        
    Member member = new Member(username, age);
    memberRepository.save(member);

    //Model에 데이터를 set(보관), get(꺼냄)Attribute()
    request.setAttribute("member", member);

    String viewPath = "/WEB-INF/views/save-request.jsp";
    RequestDispatcher dispatcher = request.getReqeustDispatcher(viewPath);
    dispatcher.forward(request, response);

    <ul>
        <li>id=${member.id}</li>
        <li>username=${member.username}</li>
        <li>age=${member.age}</li>
    </ul>

    JSP의 ${} 문법을 제공하는데, 이 문법을 사요하여 request의 attribute에 담긴 데이터를 편리하게 조회할 수 있다.

### MvcMemberListServlet - 회원 목록(mvc패턴)    
    List<Member> members = memberRepository.findAll();
       
    request.setAttribute("members", members);

    String viewPath = "/WEB-INF/views/members.jsp";
    RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);

    dispatcher.forward(request, response);

     <table>
    <thead>
        <th>id</th>
        <th>username</th>
        <th>age</th>
    </thead>
    <tbody>
        <c:forEach var="item" items="${members}">
            <tr>
                <td>${item.id}</td>
                <td>${item.username}</td>
                <td>${item.age}</td>
            </tr>
        </c:forEach>
        </tbody>
    </table>

### MVC 패턴 없이, JSP를 사용 했을 경우
    <%
        for (Member member : members) {
                out.write("    <tr>");
                out.write("        <td>" + member.getId() + "</td>");
                out.write("        <td>" + member.getUsername() + "</td>");
                out.write("        <td>" + member.getAge() + "</td>");
                out.write("    </tr>");
        }
    %>

### MVC 패턴 - 한계
    MVC 패턴을 이용해 Controller / View 랜더링의 역할을 명확하게 구분했지만, Controller의 중복되는 코드, 불필요한 코드가 많이 보인다.

    1) forward 중복
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewPath);
        dispatcher.forward(request, response); 
     
    2) ViewPath 중복
        String viewPath = "/WEB-INF/views/new-form.jsp";

### 문제점 정리 
    공통으로 처리해야 하는 부분이 점점 더 많이 증가할 것이다.
    이 문제를 해결하려면 컨트롤러 호출 전에 먼저 공통 기능을 처리.

### 해결 방안
    Front Controller 패턴을 도입.            