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

        ● Filter interface
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

        ● LoginCheckFilter - 인증 체크 필터
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

