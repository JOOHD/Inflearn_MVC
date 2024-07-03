## 본문

### Exception - 예외
    ● 자바 직접 실행
    자바의 메인 메서드를 직접 실행하는 경우 main이라는 이름의 쓰레드가 실행된다. 실행 도중에 예외를 잡지 못하고 처음 실행한 main() 메서드를 넘어서 예외가 던져지면, 예외 정보를 남기고 해당 쓰레드는 종료된다.

    ● 웹 애플리케이션
    웹 애플리케이션은 사용자 요청별로 별도의 쓰레드가 할당되고, 서블릿 컨테이너 안에서 실행된다. 애플리케이션에서 예외가 발생했는데, 어디선가 try ~ catch로 예외를 잡아서 처리하면 아무런 문제가 없다.
    그런데 만약에 애플리케이션에서 예외를 잡지 못하고, 서블릿 밖으로 까지 예외가 전달되면 어떻게 동작할까?

        ● 사용 기준
        1) 예외 발생 가능성 : 예외가 발생할 가능성이 있는 코드에서 사용
        예를 들어, 파일 입출력, 데이터베이스 연결, 네트워크 통신, 형변환, 배열 인덱스 접근 등..
        2) 비즈니스 로직의 일관성 유지 : 예외가 발생해도 비즈니스 로직이 올바르게 처리되어야 할 때 사용된다. 예외를 처리하여 로직의 흐름을 유지하고, 데이터 일관성을 보장한다.
        3) 사용자 친화적인 에러 처리 : 사용자에게 친화적인 에러 메시지를 제공하거나, 특정 예외 상황에서 적절한 대응.
        4) 자원 해제 : 예외가 발생해도 자원을 적절히 해제해야 할 때 사용된다.
        예를 들어, 파일, 데이터베이스 연결 네트워크 소켓 등..
        5) 로깅 및 디버깅 : 예외 정보를 로깅하여 문제를 진단하고, 디버깅에 활용할 때 사용된다.
     
        ● 예제 코드 (파일 입출력 예제)
        public void readFile(String filePath) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(filePath));
                String line;
                while ((line = reader.readLine()) != null) {
                    system.out.println(line);
                }
            } catch (FileNotFoundException e) {
                System.err.println("File not found : " + filePath);
            } catch (IOException e) {
                System.err.println("Error reading file : " + e.getMessage());
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    System.err.println("Error closing reader : " + e.getMessage());
                }
            }
        }

        ● 에제 코드 (데이터베이스 연결)
        public void fetchData() {
            Connection conn = null;
            PreparedStatement stmt = null;
            Result rs = null;
            try {
                conn = DriverManager.getConnection(DB_URL, USER, PASS);
                stmt = conn.prepareStatement("SELECT * FROM my_table");
                rs = stmt.executeQuery();
                while (rs.next()) {
                    System.out.println("Data : " + rs.getString("data_column"))'
                }
            } catch (SQLException e) {
                System.out.println("Database error : " + e.getMessage());
            } finally {
                try {
                    if (rs != null) rs.close();
                    if (stmt != null) stmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException e) {
                    System.out.println(Error closing resources : " + e.getMessage());
                }
            }
        }

        ● 사용자 입력 예제
        public void parseInput(String input) {
            try {
                int number = Integer.parseInt(input);
                System.out.println("Parsed number : " + number);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number format : " + input);
            }
        }

    ● 예외 발생 흐름
    - WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)

        - 결국 톰캣 같은 WAS 까지 예외가 전달된다. WAS는 예외가 올라오면 어떻게 처리해야 할까?
        - server.error.whitelabel.enabled=false
        
        @Slf4j
        @Controller
        public class ServletExController {
            @GetMapping("/error-ex")
            public void errorEx() {
                throw new RuntimeException("예외 발생");
            }
        }
        - 실행 시, HTTP Status 500 - Internal Server Error
            Exception의 경우 서버 내부에서 처리할 수 없는 오류가 발생한 것으로 생각해서 HTTP 상태 코드 500을 반환.

    ● response.sendError(HTTP 상태 코드, 오류 메시지)            
    - 오류가 발생했을 때 HttpServletResponse가 제공하는 sendError라는 메서드를 사용해도 된다. 이것을 호출한다고 당장 예외가 발생하는 것은 아니지만, 서블릿 컨테이너에게 오류가 발생했다는 점을 전달.
    이 메서드를 사용하면 HTTP 상태 코드와 오류 메시지도 추가할 수 있다.
        - response.sendError(HTTP 상태 코드)
        - response.sendError(HTTP 상태 코드, 오류 메시지)

    ● ServletExController - 추가
        @GetMapping("/error-404")
        publi void error404(HttpServletResponse response) throws IOException {
            response.sendError(404, "404 오류");
        }

        @GetMapping("/error-500")
        publi void error404(HttpServletResponse response) throws IOException {
            response.sendError(500, "500 오류");
        }

    ● sendError 흐름
    - WAS(sendError 호출 기록 확인) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(response.sendError())

    - response.sendError()를 호출하면 response 내부에는 오류가 발생했다는 상태를 저장해둔다. 그리고 서블릿 컨테이너는 고객에게 응답 전에 response에 sendError()가 호출되었는지 확인한다.
    그리고 호출되었다면 설정한 오류 코드에 맞추어 기본 오류 페이지를 보여준다.

    ● 정리
    서블릿 컨테이너가 제공하는 기본 예외 처리 화면은 사용자가 보기에 불편하다. 의미 있는 오류 화면을 제공해보자.

    ● web.xml
    <error-page>
        <error-code>404</error-code>
        <location>/error-page/404.html</location>
    </error-page>    

    @Component
    public class WebServerCustomizer implements
    WebServerFactoryCoustmizer(ConfigurableWebServerFactory factory) {
        ErrorPage errorPage404 = new ErrorPage(HttpStatus.NOT_FOUND, "/error-page/404");
    }

    - 오류 패이지는 예외를 다룰 때 해당 예외와 그 자식 타입의 오류를 함께 처리한다. 예를 들어서 위의 경우 RuntimeException은 물론이고 RuntimeException의 자식도 함께 처리한다.
    - 오류가 발생했을 때 처리할 수 있는 컨트롤러가 필요하다. 예를 들어서 RuntimeException 예외가 발생하면 errorPageEx에서 지정한 /error-page/500이 호출된다.

    @Controller
    public class ErrorPageController {
        @RequestMapping("/error-page/404")
        public String errorPage404(HttpServletRequest request, HttpServletResponse response) {
            return "error-page/404";
        }
    }

    WAS는 해당 예외를 처리하는 오류 페이지 정보를 확인한다.
    new ErrorPage(RuntimeException.class, "/error-page/500")
    예를 들어서 RuntimeException 예외가 WAS까지 전달되면, WAS는 오류 페이지 정보를 확인한다. 확인해보면 RuntimeException의 오류 페이지로 /error-page/500이 저장되어 있다. WAS는 오류 페이지를 출력하기 위해 /error-page/500을 다시 요청한다.
      
    ● 오류 페이지 요청 흐름
    WAS '/error-page/500' 다시 요청 -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러(/error-page/500) -> view

    중요한 점은 웹 브라우저(클라이언트)는 서버 내부에서 이런 일이 일어나는지 전혀 모른다. 오직 서버 내부에서 오류 페이지를 찾기 위해 추가적인 호출을 한다.

    ● 정리 
    1) 예외가 발생해서 WAS까지 전파된다.
    2) WAS는 오류 페이지 경로를 찾아서 내부에서 오류페이지를 호출한다. 이때 오류 페이지 경로로 필터, 서블릿, 인터셉터, 컨트롤러가 모두 다시 호출된다.

    - WAS는 오류 페이지를 단순히 다시 요청만 하는 것이 아니라, 오류 정보를 request의 attribute에 추가해서 넘겨준다.
    필요하면 오류 페이지에서 이렇게 전달된 오류 정보를 사용할 수 있다.

        - // RequestDispatcher 상수로 정의 
        public static final String ERROR_EXCEPTION = "javax.servlet.error.exception";
        public static final String ERROR_MESSAGE = "javax.servlet.error.message";
        public static final String ERROR_REQUEST_URI = "javax.servlet.error.request_uri";

### 서블릿 예외 처리 - 필터
    ● 목표
    예외 처리에 따른 필터와 인터셉터 그리고 서블릿이 제공하는 DispatchType 이해하기

    오류가 발생하면 오류 페이지를 출력하기 위해 WAS 내부에서 다시 한번 호출이 발생한다. 이때 필터, 서블릿, 인터셉터도 모두 다시 호출된다. 그런데 로그인 인증 체크 같은 경우를 생각해보면, 이미 한번 필터나, 인터셉터에서 로그인 체크를 완료했다. 따라서 서버 내부에서 오류 페이지를 호출한다고 해서 해당 필터나 인터셉터가 한번 더 호출되는 것은 매우 비효율적이다.
    결국 클라이언트로 부터 발생한 정상 요청인지, 아니면 오류 페이지를 출력하기 위한 내부 요청인지 구분할 수 있어야 한다. 서블릿은 이런 문제를 해결하기 위해 DispatherType이라는 추가 정보를 제공한다.

    ● DispatcherType
    클라이언트로 부터 발생한 정상 or 오류 페이지 출력인지 구분해주는 기능을 필터에서 제공해주는 기능이 DispatcherTypes 옵션이다.

        public enum DispatcherType {
            FORWARD, // 클라이언트 요청
            INCLUDE, // 서블릿에서 다른 서블릿이나 JSP의 결과를 포함할 때
            REQUEST, // 클라이언트 요청
            ASYNC,   // 서블릿 비동기 호출
            ERROR    // 오류 요청
        }
    
    ● LogFilter - Dispatcher 로그 추가
    @Slf4j
    public class LogFilter implements Filter {

        @Override
        pulic void init(FilterConfig filterConfig) throws ServletException {
            log.info("log filter init");
        }
        
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            String requestURI = httpRequest.getRequestURI();

            String uuid = UUID.randomUUID().toString();

            tyr {
                log.info("REQUEST", uuid, request.getDispatcherType(), requestURI);
                chain.doFilter(request, response);
            } catch (Exception e) {
                throw e;
            } finally {
                log.info("RESPONSE", uuid, request.getDispatcherType(), requestURI);
            }
        }

        @Override
        public void destroy() {
            log.info("log filter destroy");
        }
    }    

    ● WebConfig
    @Configuration
    public class WebConfig implements WebMvcConfigurer {
        @Bean
        public FilterRegistrationBean logFilter() {
            FilterRegistrationBean<Filter> filterRegistrationBean = new ~~<>();
            filterRegistrationBean.setFilter(new LogFilter());
            filterRegistrationBean.setOrder(1);
            filterRegistrationBean.addUrlPatterns("/*");
            filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR);
            return filterRegistrationBean;
        }
    }

    filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR);   
    - 두 가지를 모두 넣으면 클라이언트 요청은 물론이고, 오류 페이지 요청에도 필터가 호출된다. 아무것도 넣지 않으면 기본 값은 DispatcherType.REQUEST이다. 즉 클라이언트의 요청이 있는 경우에만 필터가 적용된다.

### 서블릿 예외 처리 - Interceptor (중복 호출 제거)
    @Slf4j
    public class LogInterceptor implementes HandlerInterceptor {

        public static final LOG_ID = "logId";

        @Override
        public boolean preHandle(HttpServletRequest requet, HttpServletResponse response, Object Handler) throws Exception {

            String requestURI = request.getRequestURI();

            String uuid = UUID.randomUUID().toString();
            request.setAttribute(LOG_ID, uuid);

            return true;
        }

        @Override
        public void postHandle(HttpServletRequest request, HttpServletResponse response,, Object handler, ModelAndView mav) throws Exception {}

        @Override
        public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception ex {
            String requestURI = request.getRequestURI();
            String logId = (String)request.getAttribute(LOG_ID);

            if (ex != null) {
                log.info("afterCompletion error!!", ex);
            }
        }
    }
    - 앞서 필터의 경우에는 필터를 등록할 때 어떤 DispatcherType인 경우에 필터를 적용할 지 선택할 수 있었다. 그런데 인터셉터는 서블릿이 제공하는 기능이 아니다. 따라서 DispatcherType과 무관하게 항상 호출된다.
  
    대신에 인터셉터는 다음과 같이 요청 경로에 따라서 추가하거나 제외하기 쉽게 되어 있기 때문에 이러한 설정을 사용해서 오루 페이지 경로를 excludePathPatterns를 사용해서 빼주면 된다.

        @Override
        public void addInterceptors(InterceptorRegistry registry) {
            registry.addInterceptor(new LogInterceptor()) {
                    .order(1)
                    .addPathPatterns("/**")
                    .excludePathPatterns(
                        "/css/**", "/*.ico",
                        "/error", "/error-page/**" // 오류 페이지 경로
                    )
            }
        }

    ● 정리
        - 흐름
        WAS(hello, dispatchType=REQUEST) -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러 -> View

        - 중복 제거
        filter : DispatchType으로 중복 제거(dispatchType=REQUEST)
        interceptor : 경로 정보로 중복 호출 제거(excludePathPatterns("/error-page/**"))
            1) WAS(/error-ex, dispatchType=REQUEST) -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러
            2) WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)
            3) WAS 오류 페이지 확인
            4) WAS(/error-page/500, dipatchType=ERROR) -> 필터(x) -> 서블릿 -> 인터셉터(x) -> 컨트롤러(/error-page/500) -> View

### RequestURI() 메서드 종류
    1) 로그 및 모니터링
    @Override
    public boolean preHandle(HttpServletRequest requset, HttpServletResponse response, Object handle) throws Exception {
        String requestURI = request.getRequestURI();
        system.out.println("Request URI : " + requestURI);

    }

    2) 인증 및 권한 부여
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handle) throw Exception {
        String requestURI = request.getRequestURI();
        if (!hasPermission(requestURI)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Your don't haver permission to access this resource.")'
        }
        return true;
    }

    private boolean hasPermission(String requestURI) {
        return true;
    }

    3) 비지니스 로직 처리
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handle) throw Exception {
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/admin")) {
            // Perform specific logic for admin pages
        }  else {
            // Perform logic for other pages
        }   
        return true;
    }

    4) 리다이렉트 및 포워딩
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handle) throws Exception {
        String requestURI = request.getRequestURI();
        if ("/old-page".equals(requestURI)) {
            response.sendRedirect("/new-page");
            return false
        }
        return true;
    }

    - getRequestURI() 메서드를 통해 URI를 가져오는 작업은 웹 애플리케이션에서 매우 중요한 역할을 한다.
    
    ● WAS의 역할 (참고)
    - WAS는 동적인 웹 콘텐츠를 생성하고 제공하는 서버를 뜻한다.
    1) 서블릿 실행 : 웹 애플리케이션에서 서블릿을 실행하고, 서블릿의 생명주기를 관리
    2) JSP 처리 : JSP 파일을 컴파일하고 실행하여 동적인 웹 페이지를 생성한다.
    3) 요청/응답 처리 : 클라이언트로부터의 HTTP 요청을 받아 적절한 서블릿이나 JSP로 전달하고, 생성된 응답을 클라이언트에게 전송합니다.
    4) 세션 관리 : 사용자의 세션을 관리하여 지속적인 사용자 상태 정보를 유지.
    5) 보안 : 인증 및 권한 부여 등을 통해 웹 애플리케이션의 보안을 관리.
    6) 트랜잭션 관리 : 트랜잭션을 관리하여 데이터베이스 연사의 일관성과 무결성을 유지.
    
    ● 예외 처리 흐름
    - 애플리케이션 레벨 : 웹 애플리케이션 코드 내에서 예외가 발생하면 이를 처리하기 위해 try ~ catch 블록을 사용하거나, 예외 처리기를 구현할 수 있다.
    - 서블릿 컨테이너 레벨 : 서블릿이나 JSP에서 예외가 발생하면 서블릿 컨테이너(WAS)가 이를 감지하고, 설정된 오류 페이지나 기본 오류 페이지로 리다이렉션 할 수 있다.
    - WAS 레벨 : 서블릿 컨테이너에서 처리되지 않은 예외가 발생하면 WAS는 이를 HTTP 상태 코드 500 (Internal Server Error)으로 처리하여 클라이언트에 반환.

    ● 서블릿 코드에서의 예외 처리
    @WebServlet("/example")
    public class ExampleServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throw Exception {
            try {
                // 비즈니스 로직 실행
            } catch (Exception e) {
                // 예외 처리
                request.setAttribute("errorMessage", e.getMessage());
                request.getRequestDispatcher("/errorPage.jsp").forward(Request, response);
            }
        }
    }

    ● web.xml에서의 예외 처리 설정
    <error-page>
        <error-code>500</>
        <location>/serverError.jsp</>
    </>
    위 예시는 웹 애플리케이션에서 예외가 발생했을 때 이를 처리하는 방법을 보여준다. 예외가 애플리케이션 코드 내에서 처리되지 않으면 WAS가 예외를 감지하고, 설정된 오류 페이지로 리다이렉션하거나 기본적으로 HTTP 상태 코드 500을 반환하여 클라이언트에게 알린다.    


### 스프링 부트 - 오류 페이지1
        - 지금까지 예외 처리 페이지를 만들기 위해서 다음과 같은 복잡한 과정을 거쳤다.
          - WebServerCustomizer를 만들고
          - 예외 종류에 따라서 ErrorPage를 추가하고
          - 예외 처리용 컨트롤러 ErrorPageController를 만듬
          
        - 스프링 부트는 이런 과정을 모두 기본으로 제공한다.
          - ErrorPage를 자동으로 등록한다. 이때 /error라는 경로로 기본 오류 페이지를 설정한다.
            - new ErrorPage("/error"), 상태코드와 예외를 설정하지 않으면 기본 오류 페이지로 사용된다.
            - 서블릿 밖으로 예외가 발생하거나, response.sendError()가 호출되면 모든 오류는 /error를 호출하게 된다.
          - BasicErrorController라는 스프링 컨트롤러를 자동으로 등록한다.    
            - ErrorPage에서 등록한 /error를 매핑해서 처리하는 컨트롤러다.

        ● 참고
        ErrorMvcAutoConfiguration이라는 클래스가 오류 페이지를 자동으로 등록하는 역할을 한다.

        ● 주의
        스프링 부트가 제공하는 기본 오루 메커니즘을 사용하도록 WebServerCustomizer에 있는 @Component를 주석 처리하자. 

        이제 오류가 발생했을 경우 오류 페이지로 /error를 기본 요청한다.
        스프링 부트가 자동 등록한 BasicErrorController는 이 경로를 기본으로 받는다.

          - 개발자는 오류 페이지만 등록   
          BasicErrorController는 기본적인 로직이 모두 개발되어 있다.
          개발자는 오류 페이지 화면만 BasicErrorController가 제공하는 룰과 우선순위에 따라서 등록하면 된다. 정적 HTML이면 정적 리소스, 뷰 템플릿을 사용해서 동적으로 오류 화면을 만들고 싶으면 뷰 템플릿 경로에 오류 페이지 파일을 만들어서 넣어두기만 하면 된다.

            - 뷰 템플릿
              - resources/templates/error/500.html
            - 정적 리소스(static, public)
              - resources/static/error/400.html
            - 적용 대상이 없을 때 뷰 이름(error)
              - resources/templates/error.html    

### 스프링 부트 - 오류 페이지2
        - BasicErrorController 컨트롤러는 다음 정보를 model에 담아서 뷰에 전달한다. 뷰 템플릿은 이 값을 활용해서 출력할 수 있다.
          - timestamp : Fri Feb 05 00:00:00 KST 2024
          - status : 400
          - error : Bad Request
          - exception : org.springframework.validation.BindingException
          - trace : 예외 trace
          - message : Validation failed for object='data'. Error count : 1
          - errors :Errors(BindingResult)
          - path : 클라이언트 요청 경로 ('/hello')

        오류 관련 내부 정보들을 고객에게 노출하는 것은 좋지 않다. 고객이 정보를 읽어도 혼란만 더해지고, 보안상 문제가 될 수도 있다.
        그래서 BasicErrorController 오류 컨트롤러에서 다음 오류 정보를 model에 포함할지 여부를 선택할 수 있다.

            application.properties
            - server.error.include-exception=false : exception 포함여부(true, false)
            - server.error.include-message=never : message 포함여부
            - server.error.include-stacktrace=never : trace 포함여부
            - server.error.include-binding-errors=never : errors 포함여부
        
        하지만 실무에서는 이것들을 노출해서는 안된다. 사용자에게는 이쁜 오류 화면과 고객이 이해할 수 있는 간단한 오류 메시지를 보여주고 오류는 서버에 로그로 남겨서 로그로 확인해야 한다.

        ● 스프링 부트 오류 관련 옵션
        - server.error.whitelabel.enable=true : 오류 처리 화면을 못 찾을 시, 스프링 whitelabel 오류 페이지 적용
        - server.error.path=/error : 오류 페이지 경로, 스프링이 자동 등록하는 서블릿 글로벌 오류 페이지 경로와 BasicErrorController 오류 컨트롤러 경로에 함께 사용된다.
      
        ● 확장 포인트
        - 에러 공통 처리 컨트롤러의 기능을 변경하고 싶으면 ErrorController 인터페이스를 상속 받아서 구현하거나 BasicErrorController 상속 받아서 기능을 추가하면 된다.