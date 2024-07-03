## 본문

### Servlet Filter 소개
    요구사항을 보면 로그인 한 사용자만 상품 관리 페이지에 들어갈 수 있어야 한다. 문제는 로그인 하지 않은 사용자도 다음 URL을 직접 호출하면 상품 관리 화면에 들어갈 수 있다는 점이다.

    상품 관리 컨트롤러에서 로그인 여부를 체크하는 로직을 하나하나 작성하면 되겠지만, 등록, 수정, 삭제, 조회 등등 상품관리의 모든 컨트롤러 로직에 공통으로 로그인 여부를 확인해야 한다. 더 큰 문제는 향후 로그인과 관련된 로직이 변경될 때 이다. 작성한 모든 로직을 다 수정해야 할 수 있다.

    이렇게 애플리케이션 여러 로직에서 공통으로 관심이 있는 것을 공통 관심사(cross-cutting concern)이라고 한다. 이러한 공통 관심사는 스프링의 AOP로도 해결할 수 있지만,, 웹과 관련된 공통 관심사는 지금부터 설명할 서블릿 필터 또는 스프링 인터셉터를 사용하는 것이 좋다. 웹과 관련된 공통 관심사를 처리할 때는 HTTP의 헤더나 URL의 정보들이 필요한데, 서블릿 필터나 스프링 인터셉터는 HttpServletRequest를 제공한다.

    ● 필터 흐름
    HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 컨트롤러 // 로그인 사용자
    HTTP 요청 -> WAS -> 필터(적저하지 않은 요청이라 판단, 서블릿 호출X) // 비 로그인 사용자
    - 필터에서 적절하지 않은 요청이라고 판단하면 거기에서 끝을 낼 수도 있다. 그래서 로그인 여부를 체크하기에 딱 좋다.
    
    ● 필터 체인 
    HTTP 요청 -> WAS -> 필터1 -> 필터2 -> 필터3 -> 서블릿 -> 컨트롤러
    - 필터는 체인으로 구성되는데, 중간에 필터를 자유롭게 추가할 수 있다.
    예를 들어서 로그를 남기는 필터를 먼저 적용하고, 그 다음에 로그인 여부를 체크하는 필터를 만들 수 있다.

### Filter interface
    punlic interface Filter {
        public default void init(FilterConfig filterConfig) throws ServletException {}

        public default void destroy() {}
    }

    - 필터 인터페이스를 구현하고 등록하면 서블릿 컨테이너가 필터를 싱글톤 객체로 생성하고, 관리한다.
    - init() : 필터 초기화 메서드, 서블릿 컨테이너가 생성될 때 호출된다.
    - doFilter() : 고객의 요청이 올 때 마다 해당 메서드가 호출된다. 필터의 로직을 구현하면 된다
    - destory() : 필터 종료 메서드, 서블릿 컨테이너가 종료될 때 호출된다. 
    
    ● Servlet Filter - 요청 로그
    @Slf4j
    publi class LogFilter implements Filter {

        @Override 
        public void init(FilterConfig filterConfig) throws ServletException{}

        @Override
        public void doFilter(SerlvetRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String requestURI = httpRequest.getRequestURI();

            String uuid = UUID.randomUUID().toString();

            try {
                log.info("REQUEST [{}][{}]", uuid, requestURI);
                chain.doFilter(request, response);
            } catch (Exception e) {
                throw e;
            } finally {
                log.info("REQUEST [{}][{}]", uuid, requestURI);
            }
        }

        @Override
        public void destroy() {
            log.info("log filter destroy");
        }
    }
    - public class LogFilter implements Filter {}
      - 필터를 사용하려면 필터 인터페이스를 구현해야 한다.
    - doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      - HTTP 요청이 오면 doFilter가 호출된다.
      - ServletRequest request는 HTTP 요청이 아닌 경우까지 고려해서 만든 인터페이스이다. HTTP를 사용하면 HttpServletRequest httpRequest = (HttpServletRequest) request; 와 같이 다운 캐스팅 하면 된다.
    -  String uuid = UUID.randomUUID().toString();
       -  HTTP 요청을 구분하기 위해 요청당 임의의 uuid를 생성해둔다.
    -  chain.doFilter(request, response);
       -  이 부분이 가장 중요하다. 다음 필터가 있으면 필터를 호출하고, 필터가 없으면 서블릿을 호출한다. 만약 이 로직을 호출하지 않으면 다음 단계로 진행되지 않는다.
              
    ● 참고 - 서블릿의 역할
    서블릿은 java 웹 어플리케이션에서 클라이언트의 요청을 처리하고 응답을 생성하는 서버 측 컴포넌트이다. 서블릿은 HTTP 요청을 받아 필요한 비즈니스 로직을 수행하고, 결과를 HTML, XML, JSON 등의 형식으로 응답한다.

    ● 서블릿 작동원리
    1) 요청 수시 :클라이언트(웹 브라우저)가 서버에 HTTP 요청을 보낸다. 이 요청은 웹 서버(Apache Tomcat)에 의해 수신된다.
    2) 필터 체인 처리 : 웹 서버는 요청을 서블릿 컨테이너로 전달하고, 서블릿 컨테이너는 요청에 대해 설정된 필터 체인을 처리한다. 각 필터는 'chain.doFilter(request, response)'를 호출하여 다음 필터 또는 서블릿으로 요청을 전달한다.
    3) 서블릿 호출 : 모든 필터가 처리된 후, 필터 체인의 마지막에서 서블릿이 호출된다.
    4) 요청 처리 : 서블릿은 클라이언트의 요청을 처리한다. 여기에는 요청 매개변수를 읽고 비즈니스 로직을 수행하며, 필요한 데이터를 db에서 조회하거나 수정하는 작업이 포함될 수 있다.
    5) 응답 생성 : 서블릿은 요청 처리 결과를 기반으로 응답을 생성한다. 이 응답은 HTML, XML, JSON 등의 형식일 수 있다.
    6) 응답 전송 : 생성된 응답을 클라이언트에게 돌려준다.

    @WebServlet("/hello")
    public class HelloServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");

            // 요청 처리 로직 (예: DB 조회 등)
            String name = request.getParameter("name");
            if (name == null) {
                name = "world";
            }

            // 응답 생성
            response.getWriter().write("<html><body>");
        }
    }
    - 필터 정의 : 필터는 'Filter' 인터페이스를 구현하여 정의한다. 필터는 요청 전/후에 특정 작업을 수행할 수 있다.

    public class LoggingFiler implements Filter {
        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            // 필터 초기화 로직
        }

        @Override
        pulic void doFilter(ServletRequeset request, SerlvetResponse response, FilterChain chain) {
            // 요청 전 로직
            sysout("Request received");
            // 다음 필터 또는 서블릿으로 요청 전달
            chain.doFilter(request, response);
            // 응답 후 로직
            sysout("Response sent");
        }

        @Override
        public void destroy() {
            // 필터 종료 로직
        }
    }

    필터 체인 처리 : 클라이언트 요청이 서버에 도착하면, 필터 체인이 먼저 처리된다. 각 필터는 'chain.doFilter'를 호출하여 다음 필터 또는 서블릿으로 요청을 전달.

    서블릿 호출 : 모든 필터가 처리된 후, 서블릿이 호출되어 실제 요청을 처리하고 응답을 생성.

    ● 작동 순서
    1) 클라이언트가 HTTP 요청을 보낸다.
    2) 웹 서버가 요청을 수신하고 서블릿 컨테이너에 전달
    3) 서블릿 컨테이너는 필터 체인을 실행
        - 각 필터는 요청 전/후에 특정 작업을 수행하고, 'chain.doFilter'를 호출하여 다음 필터 또는 서블릿으로 요청을 전달한다.
    4) 필터 체인의 마지막에서 서블릿이 호출
    5) 서블릿은 요청을 처리하고 응답을 생성한다.
    6) 생성된 응답은 클라이언트로 전달된다.

    ● 정리 
    서블릿은 클라이언트 요청을 최종적으로 처리하고 생성하는 역할을 한다. 필터는 이 과정에서 부가적인 작업을 수행하며, 서블릿 호출을 통해 비즈니스 로직이 실행된다.   

    ● WebConfig - 필터 설정
    @Configuration
    public class WebConfig {

        @Bean
        public FilterRegistrationBean logFilter() {
            FilterRegistrationBean<Filter> filterRegistrationBean = new Filter~<>();
            filterRegistrationBean.setFilter(new LogFilter());
            filterRegistrationBean.setOrder(1);
            filterRegistration.addUrlPatterns("/*");
            return filterRegistrationBean;
        }
    }
    - 필터를 등록하는 방법은 여러가지가 있지만, 스프링 부트를 사용한다면 FilterRegistrationBean을 사용해서 등록하면 된다.
        - setFilter(new LogFilter()) : 등록할 필터를 지정한다.
        - setOrder(1) : 필터를 체인으로 동작한다. 따라서 순서가 필요하다. 낮을 수록 먼저 동작한다.
        - addUrlPatterns("/*") : 필터를 적용할 URL 패턴을 지정한다. 한번에 여러 패턴을 지정할 수 있다.

    ● URL 패턴에 대한 룰은 필터도 서블릿과 동일하다.   
    ● @ServletComponentScan @WebFilter(filterName = "logFilter", urlPatterns = "/*)로 필터 등록이 가능하지만 필터 순서 조절이 안된다.
    따라서 FilterRegistrationBean을 사용하자

### LoginCheckFilter - 인증 체크 필터
    @Slf4j
    public class LoginCheckFilter implements Filter {

        private static final String[] whitelist = {"/", "/members/add", "/login", "/logout", "/css/*"};

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String requestURI = httpRequest.getRequestURI();

            HttpServletResponse httpResponse = (HttpServletResponse) response;

            tru {
                log.info("인증 체크 필터 시작", requestURI);

                if (isLoginCheckPath(requestURI)) {
                    log.info("인증 체크 로직 실행 {}", requestURI);
                    HttpSession session = httpRequest.getSession(false);
                    if (session == null) || session.getAttribute(SessionConst.LOGIN_MEMBER) == null {
                        log.info("미인증 사용자 요청", requestURI);
                        // 로그인으로 redirect
                        httpResponse.sendRedirect("/login?redirectURL=" + requestURI);

                        return; // 여기가 중요, 미인증 사용자는 다음으로 진행하지 않고 끝!
                    }
                }
                chain.doFilter(request, response);
            } catch (Exception e) {
                throw e; // 예외 로깅 기능 하지만, 톰캣까지 예외를 보내주어야 함
            } finally {
                log.info("인증 체크 필터 종료", requestURI);
            }

            /**
                * 화이트 리스트 경우 인증 체크X
                */
            private boolean isLoginCheckPath(String requestURI) {
                return !PatternMachUtils.simpleMatch(whitelist, requestURI);
            }
        }
    }   
    - whitelist = {"/", "/members/add", "/login", "logout", "/css/*"};
      - 인증 필터를 적용해도 홈, 회원가입, 로그인 화면, css같은 리소스에는 접근할 수 있어야 한다. 이렇게 화이트 리스트 경로는 인증과 무관하게 항상 허용 된다. 화이트 리스트를 제외한 나머지 모늗 경로에는 인증 체크 로직을 적용한다.
    - isLoginCheckPath(requestURI)
      - 화이트 리스트를 제외한 모든 경우에 인증 체크 로직을 적용한다.
    - httpResponse.sendRedirect("/login?redirectURL=" + requestURI);
      - 미인증 사용자는 로그인 화면으로 리다이렉트 한다. 그런데 로그인 이후에 다시 홈으로 이동해버리면, 원하는 경로를 다시 찾아가야 하는 불편함이 있따. 예를 들어서 상품 관리 화면으로 보려고 들어갔다가 로그인 화면으로 이동하면, 로그인 이후에 다시 상품 관리 화면으로 들어가는 것이 좋다. 이런 부분이 개발자 입장에서는 좀 귀찮을 수 있어도 사용자 입자으로 보면 편리한 기능이다. 이러한 기능을 위해 현재 요청한 경로인 requestURI를 /login에 쿼리 파라미터로 함께 전달한다. 물론 /login 컨트롤렁서 로그인 성공시 해당 결로로 이동하는 기능은 추가로 개발해야 한다.
    -  return; 여기가 중요하다. 필터를 진행하지 않는다. 이후 필터는 서블릿, 컨트롤러가 더는 호출되지 않는다. 앞서 redirect를 사용했기 떄문에 redirect가 응답으로 적용되고 요청이 끝난다.

    ● WebConfig - loginCheckFilter() 추가
    @Bean
    public FilterRegistrationBean loginCheckFilter() {
        FilterRegistrationBean<Filter> filterRegistrationBean = new ~
        filterRegistrationBean.setFilter(new LoginCheckFilter());
    }

### LoginController - loginV4()
    /**
     * 로긍니 이후 redirect 처리
     */
    @PostMapping("/login")     
    public String loginV4(
        @Valid @ModelAttribute LoginForm form, BindingResult bindingResult, @RequestParam(defaultValue = "/") String redirectURL, HttpServletRequest request) {
            
        if (bindingResult.hasErrors()) {
            return "login/loginForm";
        }

        Member loginMember = loginService.login(form.getLongId(),
        form.getPassword());

        if (loginMember == null) {
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "login/loginForm";
        }

        // 로그인 성공 처리
        // 세션이 있으면 있는 세션 반환, 없으면 신규 세션 생성
        HttpSession session = request.getSession();
        // 세션에 로그인 회원 정보 보관
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);

        // redirectURL 적용
        return "redirect:" + redirectURL;
    }
    - 로그인 체크 필터에서, 미인증 사용자는 요청 경로를 포함해서 /login에 redirectURL 요청 파라미터를 추가해서 요청했다. 이 값을 사용해서 로그인 성공시 해당 경로로 고객을 redrirect한다. 
    
    ● 정리 
    서블릿 필터를 잘 사용한 덕분에 로그인 하지 않은 사용자는 나머지 경로에 들어갈 수 없게 되었다. 공통 관심사를 서블릿 필터를 사용해서 해결한 덕분에 향후 로그인 관련 정책이 변경되어도 이 부분만 변경하면 된다.

    ● 참고 
    필터에는 다음과 설명할 스프링 인터셉터는 제공하지 않는, 아주 강력한 기능이 있는데 chain.doFilter(request, response); 를 호출해서 다음 필터 또는 서블릿을 호출할 때 request, response를 다른 객체로 바꿀 수 있다.
    ServletRequest, ServletResponse를 구현한 다른 객체를 만들어서 넘기면 해당 객체가 다음 필터 또는 서블릿에서 사용된다. 

### Spring Interceptor - 소개
    서블릿 피터와 같이 웹과 관련된 공통 관심 사항을 효과적으로 해결할 수 있는 기술이다. 서블릿 필터가 서블릿이 제공하는 기술이라면, 스프링 인터셉터는 스프링 MVC가 제공하는 기술이다. 둘다 웹 관련된 공통 관심 사항을 처리하지만, 적용되는 순서와 범위, 그리고 사용방법이 다르다.

    ● 스프링 인터셉터 흐름
        HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 스프링 인터셉터 -> 컨트롤러
    ● 필터 체인 흐름 
        HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 컨트롤러

    - 스프링 인터셉터는 디스패처 서블릿과 컨트롤러 사이에서 컨트롤러 호출 직전에 호출 된다.
    - 스프링 인터셉터는 스프링 MVC가 제공하는 기능이기 때문에 결국 디스패처 서블릿 이후에 등장하게 된다. 스프링 MVC의 시작점이 디스패처 서블릿이라고 생각해보면 이해가 될 것이다.
    - 스프링 인터셉터에도 URL 패턴을 적용할 수 있는데, 서블릿 URL 패턴과는 다르고 매우, 정밀하게 설정할 수 있따.
    
    ● 스프링 인터셉터 제한
        HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 스프링 인터셉터 -> 컨트롤러 // 로그인 사용자
        HTTP 요청 -> WAS -> 서블릿 -> 스프링 인터셉터(적절하지 않은 요청이라 판단, 컨트롤러 호출X) // 비로그인 사용자

        - 인터셉터에서 적절하지 않은 요청이라고 판단하면 거기에서 끝을 낼 수도 있다. 그래서 로그인 여부를 체크하기에 딱 좋다.
    
    ● 스프링 인터셉터 체인
        HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 인터셉터1 -> 인터셉터2 -> 컨트롤러

        - 스프링 인터셉터는 체인으로 구성되는데 중간에 인터셉터를 자유롭게 추가할 수 있다.예를 들어서 로그를 남기는 인터셉터를 먼저 적용하고, 그 다음에 로그인 여부를 체크하는 인터셉터를 만들 수 있다.
        - 지금까지 내용을 보념 서블릿 필터와 호출 되는 순서만 다르고, 제공하는 기능은 비슷해 보인다. 앞으로 설명하겠지만, 스프링 인터셉터는 서블릿 필터보다 편리하고, 더 정교하고 다양한 기능을 제공한다.

        ● Spring Interceptor interface
        public interface HandlerInterceptor {
            default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {}

            default void postHandle(HttpSerlvetRequest requset, HttpServletResponse response, Object handler, @Nullable ModelAndView) throws Exception {}

            default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {}
        }

        - 서블릿 필넡의 경우 단순하게 doFilter() 하나만 제공된다. 인터셉터는 컨트롤러 호출 전(preHandle), 호출 후(postHandle), 요청 완료 이후(afterCompletion)와 같이 단계적으로 잘 세분화 되어 있다.
        - 서블릿 필터의 경우 단순히 request, response 만 제공했지만, 인터셉터는 어떤 컨트롤러(handler)가 호출되는지 호출 정보도 받을 수 있다. 그리고 어떤 modelAndView가 반환되는지 응답 정보도 받을 수 있따.

![interceptor](../login_img/filter&interceptor.png)

        ● 정상 흐름
        - preHandle : 컨트롤러 호출 전에 호출된다.(메서드가 순차적으로 실행.)
          - preHandler의 응답값이 true이면 컨트롤러 메서드가 실행되고, false이면 더는 진행하지 않는다. false인 경우 나머지 인터셉터는 물론이고, 헨들러 어댑터도 호출되지 않는다. 그림에서 1번에서 끝이 나버린다.
          -  인터셉터는 클라이언트에서 데이터를 가지고 서버 컨트롤러에 진입전에 작동되는 기능인 듯 하다.(개인적 소견)
        - postHandle : 모든 인터셉터의 'postHandle' 메서드가 역순으로 실행.
        - afterCompletion : 모든 인터셉터의 'afterCompletion' 메서드가 역순으로 실행.  

        1) HTTP 요청
        2) preHandle : 컨트롤러 호출, preHandel (true진행 or false 진행x) 
           ex) 인증이나 권한 검사를 수행. 
        3) handle(handler)
        4) ModelAndView 반환
        5) postHandle : 컨트롤러 호출된 후, 뷰가 렌더링되기 전에 호출.
           ex) 모델 데이터에 추가 정보를 넣을 수 있다. 
        6) render(model)호출
        7) afterCompletion : 뷰가 렌더링 된 이후에 호출된다.
           ex) 리소스를 정리하거나 로그를 기록하는 등의 후처리를 수행.
        8) HTML 응답
        
![interceptor_excep](../login_img/filter&interceptor2.png)

        ● 예외가 발생시
        - preHandle : 컨트롤러 호출 전에 호출된다.
        - postHandle : 컨트롤러에서 예외가 발생하면 postHandle은 호출되지 않는다.
        - afterCompletion : 항상 호출된다. 이 경우 예외(ex)를 파라미터로 받아서 어떤 예외가 발생했는지 로그로 출력할 수 있다.
        
        ● afterComletion은 예외가 발생해도 호출된다.
        - 예외가 발생하면 postHandle()는 호출되지 않으므로 예외와 무관하게 공통 처리를 하려면 afterCompletion()을 사용해야 한다.
        - 예외가 발생하면 afterCompletion()에 예외 정보(ex)를 포함해서 호출된다.
      
        ● 정리 
        인터셉터는 스프링MVC 구조에 특화된 필터 기능을 제공한다고 이해하면 된다. 스프링 MVC를 사용하고, 특별히 필터를 꼭 사용해야 하는 상황이 아니라면 인터셉터를 사용하는 것이 더 편리하다.

### LoginInterceptor - 요청 로그 인터셉터
    @Slf4j
    pulic class LoginInterceptor implements HandlerInterceptor {

        publicl static final String Log_ID = "logId";

        @Override (메서드 순차적 진행)
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
             
            String requestURI = request.getRequestURI();

            String uuid = UUID.randomUUID().toString();
            request.setAttribute(LOG_ID, uuid);

            // @RequestMapping : HandlerMethod
            // 정적 리소스 : ResourceHttpRequestHandler
            if (handler instanceof HandlerMethod) {
            HandleMethod hm = (HandlerMethod) handler; // 호출된 컨트롤러 메서드의 모든 정보가 포함되어 있다.
            }

            return true; // false 진행x
        }

        @Override (모든 인터셉터 메서드 역순 진행)
        public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView) throws Exception {}

        @Override
        public void afterComoletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
            String requestURI = requset.getRequestURI();
            String logId = (String)request.getAttribute(LOG_ID);
            if (ex != null) {
                log.error("afterCompletion error!!", ex);
            }
        }
    }
    - request.setAttribute(LOG_ID, uuid)
       -  서블릿 필터의 경우 지역변수로 해결이 가능하지만, 스프링 인터셉터는 호출 시점이 완전히 분리되어 있다. 따라서 preHandle에서 지정한 값을 postHandle, afterComletion에서 함께 사용하려면 어딘가에 담아두어야 한다. LogInterceptor도 싱글톤 처럼 사용되기 때문에 멤버변수를 사용하면 위험하다.
       
    ● HandlerMethod
    핸들러 정보는 어떤 핸들러 매핑을 사용하는가에 따라 달라진다. 스프링을 사용하면 일반적으로 @Controller, @RequestMapping을 활용한 핸들러 매핑을 사용하는데, 이 경우 핸들러 정보로 HandlerMethod가 넘어온다.

    ● ResourceHttpRequestHandler 
    @Controller가 아니라 /resources/static와 같은 정적 리소스가 호출 되는 경우 ResourceHttpRequesetHandler가 핸들러 정보로 넘어오기 떄문에 타입에 따라서 처리가 필요하다.

    ● postHandle, afterComletion
    종료 로그를 postHandle이 아니라 afterCompletion에서 실행한 이유는, 예외가 발생한 경우 postHandle가 호출되지 않기 때문이다. afterCompletion은 예외가 발생해도 호출 되는 것을 보장한다.

    ● WebConfig - 인터셉터 등록
    @Configuration
    public class WebConfig implementes WebMvcConfigurer {
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new LogInterceptor())
                    .order(1)
                    .addPathPatterns("/**")
                    .excludePathPatterns("/css/**", "/*.ico", "/error");
        }
    }
    - regitry.addInterceptor(new ~) : 인터셉터 등록
    - order(1) : 인터셉터의 호출 순서를 지정, 낮을 수록 먼저 호출
    - addPathPatterns("/**") : 인터셉터를 적용할 URL 패턴을 지정한다.
    - excludePathPatterns("/css/**", "/*.ico", "/error") : 인터셉터에서 제외할 패턴을 지정한다.

### Spring Interceptor - 인증 체크
    @Slf4j
    public class LoginCheckInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

            String requestURI = request.getRequestURI();

            HttpSession session = reqeust.getSession(false);

            if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER == null)) {

                response.sendRedirect("/login>redirectURL=" + requestURI);
                return false;
            }

            return true;
        }
    }    
    - 서블릿 필터와 비교해서 코드가 매우 간결하다. 인증이라는 것은 컨트롤러 호출 전에만 호출되면 된다. 따라서 preHandle만 구현하면 된다.

    ● WebConfig2 - 등록 추가
    @Configuration
    public class WebConfig implements WebMvcConfigurer {
        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new LogInterceptor())
                    .order(1)
                    .addPathPatterns("/**")
                    .excludePathPatterns("/css/**", "/*.ico", "/error");
            registry.addInterceptor(new LoginCheckInterceptor())
                    .order(2)
                    .addPathPatterns("/**")
                    .excludePathPatterns("/", "/members/add", "/login", "/logout", "/css/**", "/*.ico", "/error");
        }
    }
    - 인터셉터를 적용하거나 하지 않을 부분은 addPathPatterns와 excludePathPatterns에 작성하면 된다. 기본적으로 모든 경로에 해당 인터셉터를 적용하되 (/**), 홈(/), 회원가입(/members/add), 로그인(/login), 리소스 조회(/css/**), 오류(/error)와 같은 부분은 로그인 체크 인터셉터를 적용하지 않는다. 서블릿 필터와 비교해보면 매우 편리한 것을 알 수 있다.

    ● 정리
    서블릿 필터와 스프링 인터셉터는 웹과 관련된 공통 관심사를 해결하기 위한 기술이다. 서블릿 필터와 비교해서 스프링 인터셉터가 개발자 입장에서 훨씬 편리하다는 것을 코드로 이해.

### ArgumentResolver 활용
    스프링 MVC - 기본 기능 -> 요청 매핑 핸들러 어뎁터 구조에서 ArgumentResolver를 로그인 회원에 적용하여 조금 더 편리하게 찾아보자

    ● HomeController - 추가
    @GetMapping("/")    
    public String homeLoginV3ArgumentResolver(@Login Member loginMember, Model model) {

        // 세션에 회원 데이터가 없으면 home
        if (loginMember == null) {
            return "home";
        }

        // 세션이 유지되면 로그인으로 이동
        model.addAttribute("member", loginMember);
        return "loginHome";
    }

### LoginMemberArgumentResolver - 생성
    @Slf4j
    public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {

            boolean hasLoginAnnotation = parameter.hasParameterAnnotation(Login.class);
            boolean hasMemberType = Member.class.isAssignableFrom(parameter.getParameterType());

            return hasLoginAnnotaion && hasMemberType;
        }
         @Override
        public Object resolveArgument(
                    MethodParameter parameter, 
                    ModelAndViewContainer mavContainer, 
                    NativeWebRequest webRequest, 
                    WebDataBinderFactory binderFactory) throws Exception {

            HttpServletRequest request = (HttpServletRequest)webRequest.getNativeRequest();
            HttpSession session = request.getSession(false);
            if (session == null) {
                return null;
            }            

            return session.getAttribute(SessionConst.LOGIN_MEMBER);
        }
    }    
    - supportsParameter() : @Login 애노테이션이 있으면서 Member 타입이면 해당 ArgumentResolver가 사용된다.
    - resolverArgument() : 컨트롤러 호출 직전에 호출 되어서 필요한 파라미터 정보를 생성해준다. 여기서는 세션에 있는 로그인 회원 정보인 member 객체를 찾아서 반환해준다. 이후 스프링 MVC는 컨트롤러의 메서드를 호출하면서 여기에서 반환된 member 객체를 파라미터에 전달해준다.
    
    ● WebMvcConfigurer - 설정 추가
    @Configuration
    public class WebConfig implements WebConfigurer {
        @Override
        pulic void addArgumentResolver(List<HandlerMethodArgumentResolver> resolver) {
            resolvers.add(new LoginMemberArgumentsResolver());
        }
    }

### Interceptor - 전처리와 후처리 
    ● 전처리
    1) 인증 및 권한 검사
        - 사용자가 요청을 수행할 권한이 있는지 확인
          예를 들어, 로그인 여부나 특정 리소스에 대한 접근 권한 검사
    2) 로그 기록
        - 요청에 대한 로그를 기록하여 이후에 분석하거나 문제 발생 시 추적할 수 있다. 
          예를 들어, 요청의 시작 시간, 요청한 URL, 클라이언트 IP 주소 등을 기록할 수 있다.
    3) 데이터 검증 및 변환
        - 요청 데이터를 검증하거나 필요한 형식으로 변환
          예를 들어, 특정 헤더를 투가하거나 세션 초기화를 할 수 있다.

    ● 후처리
    1) 응답 변환 및 가공
        - 컨트롤러에서 반환한 데이터를 가공하거나 변환한다. 
          예를 들어, JSON 응답의 구조를 변경하거나 데이터를 암호화할 수 있다.
    2) 로그 기록
        - 요청 처리 중 발생한 예외를 처리하고 사용자에게 적절한 응답을 제공.
        예를 들어, 특정 예외가 발생했을 때 사용자에게 친화적인 오류 메시지를 보여줄 수 있다.
    3) 예외 처리
        - 요청 처리 중 발생한 예외를 처리하고 사용자에게 적절한 응답을 제공한다. 
        예를 들어, 특정 예외가 발생했을 때 사용자에게 친화적인 오류 메시지를 보여줄 수 있다.
    4) 자원 해제
        - 요청 처리 중 사용한 자원을 해제
        예를 들어, 데이터베이스 연결을 닫거나 파일 핸들을 해제.      

### interceptor - 실제 적용 
    Interceptor는 'preHandle'에서 시작 시간을 기록하고, 'afterCompletion'에서 요청 처리 시간을 계산하여 로깅한다.

    1) Spring Boot (build.gradle)    
        dependencies {
            implementation'orgspringframeworkboot:spring-boot-starter-web'
            testImplementation'orgspringframeworkboot:spring-boot-starter-test' 
        }

    2) Interceptor 구현 (전/후처리 적용)
    @Component
    public class LoginInterceptor implements HandlerInterceptor {
        long startTime = System.currentTimeMillis();
        request.setAttribute("startTime", startTime);

        // 전처리 : 인증 및 권한 검사
        if (!isUerAuthenticated(request)) {
            response.sendRedirect("/login");
            return false;
        }

        // 전처리 : 요청 로그 기록
        System.out.println("Request URL : " + request.getRequestURL());

        return true; // 다음 단계로 진행
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 후처리 : 추가적인 데이터 변환이나 가공
        if (modelAndView != null) {
            modelAndView.addObject("additionalData", "This is some extra data");
        }
    }

    @Overried
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        long startTime = (Long) request.getAttribute("startTime");
        long endTime = System.currentTimeMillis();
        long executeTime = endTime - startTime;

        // 후처리 : 요청 처리 시간 로깅
        System.out.println("[" + handler + "] executeTime : " + executeTime + "ms");

        // 후처리 : 자원 해제
        // exampleResource.close();
    }

    private boolean isUserAuthenticated(HttpServletRequest request) {
        // 인증 로직 구현
        return request.getSession().getAttribute("user") != null;
    }

    3) Interceptor 등록
    @Configuration
    public class Webconfig implements WebMvcConfigurer {
        @Autowired
        private LogInterceptor logInterceptor;

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(logInterceptor)
                    .addPathPatterns("/"") // 모든 경로에 대해 Interceptor를 적용
                    .excludePathPatterns("/resources/**", "/static/**", "public/**") // 특정 경로 제외
        }
    }                 
