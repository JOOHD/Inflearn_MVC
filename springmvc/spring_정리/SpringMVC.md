# 본문

### SpringMVC 전체 구조
![Framework](./springmvc/spring_img/framework.png)

### SpringFramework 전체 구조
![MVC](./springmvc/spring_img/springMVC.png)

### SpringFramework vs SpringMVC
    ● FrontController -> dispatcherServlet
    ● handlerMappingMap -> HandlerMapping
    ● MyHandlerAdapter -> HandlerAdapter
    ● ModelView -> ModelAndView
    ● viewResolver -> ViewResolver
    ● MyView -> view

### dispatcherServlet 구조
    ● 스프링 MVC도 frontController 패턴으로 구현되어 있다.
    ● 스프링 MVC의 frontController가 바로 dispatcherServlet 이다.
    그리고 이 dispatcherServlet이 바로 스프링MVC 핵심이다.

### dispatcherServlet 서블릿 등록  
    ● dispatcherServlet도 부모 클래스에서 HttpServlet을 상속 받아서 사용하고, 서블릿으로 동작한다.
        DispatcherServlet -> FrameworkServlet -> HttpServletBean -> HttpServlet
    ● 스프링 부트는 DispatcherServlet을 서블릿으로 자동으로 등록하면서 모든 경로(urlPatterns="/")에 대해서 매핑한다.
        참고 : 더 자세한 경로가 우선순위가 높다. 그래서 기존에 등록한 서블릿도 함께 동작한다.

### 요청 흐름
    ● 서블릿이 호출되면 HttpServlet이 제공하는 service()가 호출된다.
    ● 스프링MVC는 DispatcherServlet의 부모인 FrameworkServlet에서 service()를 오버라이드 해두었다.
    ● FrameworkServlet.service()를 시작으로 여러 메서드가 호출되면서,
        DispatcherServlet.doDispatcher()가 호출한다.

### DispatcherServlet.doDispatcher()
    protected void doDispatcher(HttpServletRequest request, HttpservletResponse response) throws Exception {
        
        HttpServletRequest processRequest = request;
        HandlerExecutionChain mappedHandler = null;
        ModelAndView mv = null;

        // 1.핸들러 조회 - 핸들러 매핑을 통해 요청 URL에 매핑된 핸들러 조회.
        mappedHandler = getHandler(processedRequest);
        if (mappedHandler == null) {
            noHandlerFound(processRequest, response);
            return;
        }

        // 2. 핸들러 어댑터 조회 - 핸들러를 처리할 수 있는 어댑터.
        HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

        // 3. 핸들러 어댑터 실행 -> 4. 핸들러 어댑터를 통해 핸들러 실행 -> 5. ModelAndView 반환 : 핸들러가 반환 값을 ModelAndView로 변환해서 반환
        mv = ha.handle(processedRequest, response, mappedHandler.getHandler());

        processDispatcherResult(porcessRequest, response, mappedHandler, mv, dispatchException);
    }

    private void processDispatchResult(HttpServletRequest request, HttpServletResponse response, HandlerExecutionChain mappedHandler, ModelAndView mv, Exception exception) throws Exception {

        // 뷰 랜더링 호출
        render(mv, request, response);
    }

    protected void render(ModelAndView mv, HttpServletRequest request, 
    HttpServletResponse response) throws Exception {

        View view;
        String viewName = mv.getVeiwName();

        // 6. 뷰 리졸버를 통해서 뷰 찾기, 7. View 객체 반환(논리 -> 물리 이름으로 바꾼다.)
        view = resolveViewName(viewName, mv.getModelInternal(), locale, request);

        // 8. 뷰 랜더링
        view.render(mv.getModelInternal(), request, response);
    }

### interface 살펴보기
    ● 스프링 MVC의 큰 강점은 DispatcherServlet 코드의 변경 없이, 원하는 기능을 변경하거나 확장할 수 있다는 점이다. 

    ● 인터페이스들만 구현해서 DispatcherServlet에 등록하면 원하는 컨트롤러 생성.

    ● 주요 인터페이스 목록
    핸들러 매핑: org.springframework.web.servlet.HandlerMapping
    핸들러 어댑터: org.springframework.web.servlet.HandlerAdapter
    뷰 리졸버: org.springframework.web.servlet.ViewResolver
    뷰: org.springframework.web.servlet.View

### HandlerMapping
    0 = RequestMappingHandlerMapping : 어노테이션 기반의 컨트롤러인 @RequestMapping에서 사용.
    1 = BeanNameUrlHandlerMapping : 스프링 빈의 이름으로 핸들러를 찾는다.

    ● 핸들러 매핑으로 핸들러 조회
    -HandlerMapping을 순서대로 실행해서, 핸들러를 찾는다.
    -이 경우 빈 이름으로 핸들러를 찾아야 하기 때문에, 이름 그대로 빈 이름으로 핸들러를 찾아주는 BeanNameUrlHandlerMapping가 실행에 성공하고, OldController를 반환.

### HandlerAdapter
    0 = RequestMappingHandlerAdapter : @RequestMapping에서 사용.
    1 = HttpRequestHandlerAdapter : HttpRequestHandler 처리
    2 = SimpleControllerHandlerAdapter : Controller 인터페이스(어노테이션x, 과거에 사용 처리)

    ● 핸들러 어댑터 조회
    -HandlerAdapter의 supports()를 순서대로 호출.
    -SimpleControllerHandlerAdapter가 Controller 인터페이스를 지원

    ● 핸들러 어댑터 실행
    -dispatcherServlet이 조회한 SimpleControllerHandlerAdapter를 실행하면서 핸들러 정보도 함께 넘겨준다.
    -SimpleControllerHandlerAdapter는 핸들러인 OldController를 내부에서 실행하고, 그 결과를 반환.

### 정리 - OldController 핸들러매핑, 어댑터
    OldController를 실행하면서 사용된 객체는 다음과 같다.
    HandlerMapping = BeanNameUrlHandlerMapping
    HandlerAdapter = SimpleControllerHandlerAdapter

### HttpRequestHandler(서블릿과 가장 유사한 핸들러)
    public interface HttpServletHandler {
        void handlerRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
    }

### MyHttpRequestHandler
    @Component("/springmvc/request-handler")
    public class MyHttpRequestHandler implements HttpRequestHandler {

        @Override
        public void handleRequest(HttpServletRequest request, HttpSerlvetResponse response) throws ServletException, IOException {}
    }    

### handlerMapping/adapter 동작 순서
    1.핸들러 매핑으로 핸들러 조회
    -HandlerMapping을 순서대로 실행해서, 핸들러를 찾는다.
    -이 경우 bean 이름으로 핸들러를 찾아야 된다.(baenNameUrlHandlerMapping)을 실행하여, MyHttpRequestHandler를 반환.  

    2.핸들러 어댑터 조회
    -HandlerAdapter의 supports()를 순서대로 호출한다.
    -HttpRequestHandlerAdapter가 HttpRequestHandler 인터페이스를 지원하는 대상이 된다.

    3.핸들러 어댑터 실행
    -디스패처 서블릿이 조회한 HttpRequestHandlerAdapter를 실행하면서 핸들러 정보도 함께 넘겨준다.
    -HttpRequestHandlerAdapter는 핸들러인 MyHttpRequestHandler를 내부에서 실행하고, 그 결과를 반환한다.

### viewResolver
    @Component("/springmvc/old-controller")     
    public class OldController implements Controller {
        @Override
        public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
            return new ModelAndView("new-form"); // view 사용하기 위한
        }
    }
    ● 문제 발생
    https://localhost:8080/springmvc/old-controller 실행을 하면,
    Whitelabel Error Page 오류가 발생한다.

    ● 해결 방안
    application.properties에 코드 추가.
    spring.mvc.view.prefix=/WEB-INF/views/
    spring.mvc.view.suffix=.jsp

### spring boot가 자동 등록하는 viewResolver
    BeanNameViewResolver : 빈 이름으로 뷰를 찾아서 반환한다.
    InternalResourceViewResolver : JSP를 처리할 수 있는 뷰를 반환한다.

    1.핸들러 어댑터 호출
    -핸들러 어댑터를 통해 new-form이라는 논리 뷰 이름을 획득한다.

    2.ViewResolver 호출
    -new-form이라는 뷰 이름으로 viewResolver를 순서대로 호출한다.
    -BeanNameViewResolver는 new-form이라는 이름의 스프링 빈으로 등록된 뷰를 찾아야 하는데 없다.

    3.InternalResourceViewResolver
    이 뷰 리졸버는 InternalResourceView를 반환한다.

    4.view - InternalResourceView
    InternalResourceView는 JSP처럼 포워드 forward()를 호출해서 처리할 수 있는 경우에 사용한다.

    5.view.render()
    view.render()가 호출되고 InternalResourceView는 forward()를 사용해서 JSP를 실행한다.

### 스프링 MVC - 시작하기
    ● @RequestMapping
      스프링은 어노테이션을 활용에 매우 유연하고, 실용적인 컨트롤러를 만들었는데 이것이 @RequestMapping 어노테이션을 사용하는 컨트롤러이다.

### SpringMemberFormControllerV1 - 회원 등록(@RequestMapping 적용)      
    @Controller
    public class SpringMemberFormControllerV1 {

        @RequestMapping("/springmvc/v1/members/new-form")
        public ModelAndView process() {
            return new ModelAndView("new-form");
        }
    }

    ● @Controller
        -스프링이 자동으로 스프링 빈으로 등록한다.
        (내부에 @Component 어노테이션이 있어서 컴포넌트 스캔의 대상이 된다.)
        - 스프링 MVC에서 어노테이션 기반 컨트롤러로 인식한다.
         
    ● @RequestMapping : 요청 정보를 매핑한다. 해당 URL이 호출되면 이 메서드가 호출된다. (메서드의 이름은 임의로 지으면 된다.)

        기존 매핑 url 
        @WebServlet(name = "memberFormServlet", urlPatterns = "/servlet/members/new-form")

        @RequestMapping 사용 시
        @RequestMapping("/springmvc/v1/members/new-form")

    ● ModelAndView : 모델과 뷰 정보를 담아서 반환한다.

    ● addObject() : 스프링이 제공하는 ModelAndView를 통해 Model 데이터를 추가할 때는 addObject()를 사용하면 된다. 이 데이터는 이후 뷰를 렌더링 할 때 사용된다.
    
### SpringMemberControllerV2
    /**
     * 클래스 단위
     * @RequestMapping 클래스 레벨과 메서드 레벨 조합
     */
    @Controller
    @RequestMapping("/springmvc/v2/members")
    public class SpringMemberControllerV2 {

        private MemberRepository memberRepository = MemberRepository.getInstance();

        @RequestMapping("/new-form")    // 회원 등록 폼
        public ModelAndView newForm() {
            return new ModelAndView("new-form");
        }

        @RequestMapping("/save")
        public ModelAndView save(HttpServletReqeust request, HttpServletResponse response) {

            String username = request.getParameter("username");
            int age = Integer.parseInt(request.getParameter("age"));

            Member member = new Member(username, age);
            memberRepository.save(member);

            ModelAndView mav = new ModelAndView("save-result");
            mav.addObject("member", member)'
            return mav;
        }

        @RequestMapping
        public ModelAndView members() {

            List<Member> members = memberRepository.findAll();

            ModelAndView mav = new ModelAndView("members");
            mav.addObject("members", member);
            return mav;
        }
    }

### 컨트롤러 클래스 조합
    /springmvc/v2/members 라는 부분에 중복이 있다.
    @RequestMapping("/springmvc/v2/members/new-form")
    @RequestMapping("/springmvc/v2/members")
    @RequestMapping("/springmvc/v2/members/save")

    위에 코드를 클래스 레벨에 다음과 같이 @RequestMapping을 두면 조합 가능.
    클래스 레벨 @RequestMapping("/springmvc/v2/members")
    /springmvc/v2/members/new-form
    /springmvc/v2/members/save
    /springmvc/v2/members

###  SpringMemberControllerV3
    /*
     * v3
     * model 도입
     * ViewName 직접 반환
     * @RequestParam 사용
     * @RequestMapping -> @GetMapping, @PostMapping
     */
    @Controller
    @RequestMapping("/springmvc/v3/members")
    public class SpringMemberControllerV3 {

        private MemberRepository memberRepository = MemberRepository.getInstance();

        @GetMapping("/new-form")
        public String newForm() {
            return "new-form";
        }

        @PostMapping("/save")
        public String save(
            @RequestParam("username") String username,
            @RequestParam("age") int age,
            Model model) {

            Member memebr = new Member(username, age);
            memberRepository.save(member);

            model.addAttribute("member", member);
            return "save-result";
            }

        @GetMapping
        public String members(Model model) {
            List<Member> members = memberRepository.findAll();
            model.addAttribute("members", members);
            return "members";
        }
    } 

    ● Model 파라미터
    save(), members()를 보면 Model을 파라미터로 받는 것을 확인할 수 있다.

    ● viewName 직접 반환
    뷰의 논리 이름을 반환할 수 있다.

    ● @RequestParam 사용
    스프링 HTTP 요청 파라미터를 @RequestParam으로 받을 수 있다.
    @RequestParam("username")은 request.getParameter("username")와 거의 같ㅌ은 코드라 생각하면 된다.

    ● @RequestMapping -> @GetMapping, @PostMapping
    @RequestMapping은 URL만 매칭하는 것이 아니라, HTTP Methos도 함께 구분할 수 있다. 
    ex) @RequestMapping(value = "/new-form", method = RequestMethod.GET)
        위에 코드를 @GetMapping(value = "/new-form")으로 축약 가능하다.




