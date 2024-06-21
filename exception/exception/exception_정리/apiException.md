## 본문

### 목표
    HTML 페이지의 경우 지금까지 설명했던 것 처럼 4xx, 5xx와 같은 오류 페이지만 있으면 대부분의 문제를 해결할 수 있다.
    그런데 API의 경우에는 생각할 내용이 더 많다. 오류 페이지는 단순히 고객에게 오류 화면을 보여주고 끝이지만, API는 각 오류 상황에 맞는 오류 응답 스펙을 정하고, JSON으로 데이터를 내려주어야 한다.

### APIExceptionController - API 예외 컨트롤러
    @Slf4j
    @RestController
    public class ApiExceptionController {

        @GetMapping("/api/members/{id}")
        public MemberDto getMember(@PathVariable("id") String id) {
            if (id.equals("ex")) {
                throw new RuntimeException("잘못된 사용자");
            }

            return new MemberDto(id, "hello") + id;
        }

        @Data
        @AllArgsConstructor
        static class MemberDto {
            private String memberId;
            private String name;
        }
    }    
    - API를 요청했는데, 정상의 경우 API로 JSON 형식으로 데이티가 정상 반환된다. 그런데 오류가 발생하면 우리가 미리 만들어둔 오류 페이지 HTML이 반환된다. 이것은 기대하는 바가 아니다. 클라이언트는 정상 요청이든 오류 요청이든 JSON이 반환되기를 기대한다. 웹 브라우저가 아닌 이상 HTML을 직접 받아서 할 수 있는 것은 별로 없다.
  
    문제를 해결하려면 오류 페이지 컨트롤러도 JSON 응답을 할 수 있도록 수정해야 한다.

    ● ErrorPageController - API 응답 추가
    @RequestMapping(value = "/error-page/500", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> errorPage500Api(HttpServletRequest request, HttpServletResponse response) {
        log.info("API errorPage 500");

        Map<String, Object> result = new HashMap<>();
        Exception ex = (Exception) request.getAttribute(ERROR_EXCEPTION);
        result.put("status", request.getAttribute(ERROR_STATUS_CODE));
        result.put("message", ex.getMessage());

        Integer statusCode = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        return new ResponseEntity(result, HttpStatus.valueOf(statusCode));
    }
    - produces = MediaType.APPLICATION_JSON_VALUE의 뜻은 클라이언트가 요청하는 HTTP Header의 Accept의 값이 application/json일 때 해당 메서드가 호출된다는 것이다. 결국 클라이언트가 받고 싶은 미디어타입이 json이면 이 컨트롤러의 메서드가 호출된다.

    - 응답 데이터를 위해서 Map을 만들고 status, message 키에 값을 할당했다. 
    Jackson 라이브러리는 Map을 JSON 구조로 변환할 수 있다.
    ResponseEntity를 사용해서 응답하기 때문에 메시지 컨버터가 동작하면서 클라이언트에 JSON이 반환된다.

        ex) postman test
        - http://localhost:8080/api/members/ex
            {
                "message" : "잘못된 사용자",
                "status" : 500
            }

            - "message" : "잘못된 사용자"; 
            result.put("message", ex.getMessage());
                if (id.equals("ex")) {
                    throw new RuntimeException("잘못된 사용자");
                }

            - "status" : 500 
            result.put("status", request.getAttribute(ERROR_STATUS_CODE));

### API 예외처리 - 스프링 부트 기본 오류 처리
    API 예외 처리도 스프링 부트가 제공하는 기본 오류 방식을 사용할 수 있다.

    ● BasicErrorController 코드
    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)    
    public ModelAndView errorHtml(HtttpServletRequest request, HttpServletResponse response) {}

    @RequestMapping
    public ResponseEntity<Map<String, Object>> error(HttpServletRequest request) {}

    - /error 동일한 경로를 처리하는 errorHtml(), error() 두 메서드를 확인
    - errorHtml() : produces = MediaType.TEXT_HTML_VALUE : 클라이언트 요청의 Accept 헤더 값이 text/html인 경우에는 errorHtml()을 호출해서 view를 제공한다.
    - error() : 그외 경우에 호출되고 ResponseEntity로 HTTP Body에 JSON 데이터를 반환한다.

    ● 스프링 부트의 예외 처리
    앞서 학습했듯이 스프링 부트 기본 설정은 오류 발생시 /error를 오류 페이지로 요청한다. BasicErrorController는 이 경로를 기본으로 받는다.
    (server.error.path로 수정 가능, 기본 경로 /error)

    {
        "timestamp": "2021-04-28T00:00:00.000+00:00",
        "status": 500,
        "error": "Internal Server Error",
        "exception": "java.lang.RuntimeException",
        "trace": "java.lang.RuntimeException: 잘못된 사용자\n\tat 
        .java:19...,
        "message": "잘못된 사용자",
        "path": "/api/members/ex"
    }
    - 스프링 부트는 BasicErrorController가 제공하는 기본 정보들을 활용해서 오류 API를 생성해준다.
    
    ● 다음 옵션들을 설정하면 더 자세한 오류 정보를 추가할 수 있다.
    - server.error.include-binding-errors=always
    - server.error.include-exception = always
    - server.error.include-message = always
    - server.error.include-stacktrace = alwyas
    물론 오류 베시지는 이렇게 막 추가하면 보안상 위험할 수 있다.
    간결한 메시지만 노출하고, 로그를 통해서 확인하자.

### Html 페이지 vs API 오류
    BasicErrorController를 확장하면 JSON 메시지도 변경할 수 있다. 그런데 API 오류는 조금 뒤에 설명할 @ExceptionHandler가 제공하는 기능을 사용하는 것이 더 나은 방법이므로 지금은 BasicErrorController를 확장해서 JSON 오류 메시지를 변경할 수 있다 정도로만 이해해 두자.

    스프링 부트가 제공하는 BasicErrorController는 HTML 페이지를 제공한느 경우에는 매우 편리하다. 4xx, 5xx 등등 모두 잘 처리해준다. 그런데 API 오류 처리는 다은 차원의 이야기다. API 마다, 각각의 컨트롤러나 예외마다 서로 다능 응답 결과를 출력해야 할 수도 있다. 예를 들어서 회원과 관련되 API에서 예외가 발생할 때 응답과, 상품과 관련된 API에서 발생하는 예외에 따라 그 결과가 달라질 수 있다. 결과적으로 매우 세밀하고 복잡하다. 따라서 이 방법은 HTML 화면을 처리할 때 사용하고, API 오류 처리는 @ExceptionHandler를 사용하자.

### API 예외 처리 - HandlerExceptionResolver 시작
    ● 목표
    예외가 발생해서 서블릿을 넘어 WAS까지 예외가 전달되면 HTTP 상태코드가 500으로 처리된다. 발생하는 예외에 따라서 400, 404 등등 다른 상태코드로 처리하고 싶다. 오류 메시지, 형식들을 API마다 다르게 처리하고 싶다.        

    ● 상태코드 반환
    예를 들어서 IllegalArgumentExcepton을 처리하지 못해서 컨트롤러 밖으로 넘어가는 일이 발생하면 HTTP 상태코드를 400으로 처리하고 싶다. 어떻게 해야할까?

    APIExceptionController - 수정
    @GetMapping("/api/members/{id}")
    public MemberDto getMember(@PathVariale("id") String id) {

        if (id.equals("ex")) {
            throw new RuntimeException("잘못된 사용자");
        }

        if (id.equals("bad")) {
            thrwo new IllegalArgumentException("잘못된 입력 값");
        }
        return new MemberDto(id, "hello " + id);
    }

    - http://localhost:8080/api/members/bad라고 호출하면 IllegalArgumentsException이 발생하도록 했다.

        - 실행 코드
        {
            "status": 500,
            "error": "Internal Server Error",
            "exception" : "java.lang.IllegalArgumentException",
            "path" : "/api/members/bad"
        }

    ● HandlerExceptionResolver
    스프링 MVC는 컨트롤러(핸들러) 밖으로 예외가 던저진 경우 예외를 해결하고 동작을 새로 정의할 수 있는 방법을 제공한다. 컨트롤러 밖으로 던져진 예외를 해결하고, 동작 방식을 변경하고 싶으면 HandlerExceptionResolver를 사용하면 된다. 줄여서 ExceptionResolver라 한다.        

![ExceptionResolver_Before](./exception/exception_img/ExceptionResolver_Before.png)

![ExceptionResolver_After](./exception/exception_img/ExceptionResolver_After.png)

    ● 참고 : ExceptionResolver로 예외를 해결해도 postHandle()은 호출되지 않는다. 

    ● HandlerExceptionResolver - 인터페이스
    public interface HandlerExceptionResolver {

        ModelAndView resolverException(
            HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex);
    }
    - handler : 핸들러(컨트롤러) 정보
    - Exception ex : 핸들러(컨트롤러)에서 발생한 예외

    ● MyHandlerExceptionResolver
    @Slf4j
    public class MyHandlerExceptionResolver implements HandlerExceptionResolver {

        @Override
        public ModelAndView resolverException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

            try {
                if (ex instanceof IllegalArgumentException) {
                    log.info("IllegalArgumentException resolver to 400");
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
                    return new ModelAndView();
                } catch (IOException e) {
                    log.error("resolver ex", e);
                }

                return null;
            }
        } 
    }

    ● ExceptionResolver가 ModelAndView를 반환하는 이유는 마치 try, catch를 하듯이, Exception을 처리해서 정상 흐름 처럼 변경하는 것이 목적이다. 이름 그대로 Exception을 Resovler(해결)하는 것이 목적인다.

    여기서는 IllegalArgumentExcepion이 발생하면 response.sendError(400)를 호출해서 HTTP 상태 코드를 400으로 지정하고, 빈 ModelAndView를 반환한다.

    ● 반환 값에 따른 동작 방식
    HandleExceptionResolver의 반환 값에 따른 DispatcherServlet의 동작 방식은 다음과 같다.

        - 빈 ModelAndView : new ModelAndView() 처럼 빈 ModelAndView를 반환하면 뷰를 렌더링 하지 않고, 정상 흐름으로 서블릿이 리턴된다.
        - ModelAndView 지정 : ModelAndView에 View, Model 등의 정보를 지정해서 반환하면 뷰를 렌더링 한다.
        - null : null을 반환하면, 다음 ExceptionResolver를 찾아서 실행한다. 만약 처리할 수 있는 ExceptionResolver가 없으면 예외 처리가 안되고, 기존에 발생한 예외를 서블릿 밖으로 던진다.
        
    ● ExceptionResolver 활용
        
        - 예외 상태 코드 변환
          - 예외를 response.sendError(xxx) 호출로 변경해서 서블릿에서 상태 코드에 따른 오류를 처리하도록 위임
          - 이후 WAS는 서블릿 오류 페이지를 찾아서 내부 호출, 예를 들어서 스프링 부트가 기본으로 설정한 /error가 호출됨
        - 뷰 템플릿 처리
          - ModelAndView에 값을 채워서 예외에 따른 새로운 오류 화면 뷰 렌더링 해서 고객에게 제공 
        - API 응답 처리
          - response.getWriter().println("hello"); 처럼 HTTP 응답 바디에 직접 데이터를 넣어주는 것도 가능하다. 여기에 JSON으로 응답하면 API응답 처리를 할 수 있다.

    ● WebConfig - 수정
    WebMvcConfigurer 를 통해 등록
    /**
     * 기본 설정을 유지하면서 추가
     */
    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(new MyHandlerExceptionResolver());
    }           

### API 예외 처리 - HandlerExceptionResolver 활용
    예외를 여기서 마무리하기
    예외가 발생하면 WAS까지 예외가 던져지고, WAS에서 오류 페이지 정보를 찾아서 다시 /error를 호출하는 과정은 생각해보면 너무 복잡하다. ExceptionResolver를 활용하면 예외가 발생했을 때 이런 복잡한 과정 없이 여기에서 문제를 깔끔하게 해결할 수 있다.

    ● UserException
    public class UserException extends RuntimeException {

        public UserException() {
            super();
        }

        public UserException(String message) {
            super(message);
        }

        public UserException(String message, Throwable cause) {
            super(message, cause);
        }

        public UserException(Throwable cause) {
            super(cause);
        }

        protected UserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
            super(message, cause, enableSuppression, writableStackTrace);
        }
    }

    ● ApiExceptionController - 예외 추가
    @Slf4j
    @RestController
    public class ApiExceptionController {
        
        @GetMapping("/api/members/{id}")
        public MemberDto getMember(@PathVariable("id") String id) {

            if (id.equals("ex")) {
                throw new RuntimeException("잘못된 사용자");
            }
            if (id.equals("bad")) {
                throw new IllegalArgumentException("잘못된 입력 값");
            }
            if (id.equals("user-ex")) {
                throw new UserException("사용자 오류");
            }

            return new MemberDto(id, "hello " + id);
        }

        @Data
        @AllArgsConstructor
        static class MemberDto {
            private String memberId;
            private String name;
        }
    }

    ● UserHandlerExceptionResolver 
    @Slf4j
    public class UserHanderExceptionResolver implements HandlerExceptionResolver {

        private final Object objectMapper = new ObjectMapper();

        @Override
        public ModelAndView resolverException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

            try {
                if (ex instanceof UserException) {
                    log.info("UserException resolver to 400");
                    String acceptHeader = request.getHeader("accept");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

                    if ("application/json".equals(acceptHeader)) {
                        Map<String, Object> errorResult = new HashMap<>();
                        errorResult.put("ex', ex.getClass());
                        errorResult.put("message", ex.getMessage());
                        String result = objectMapper.writeValueAsString(errorResult);

                        response.setContentType("application/json");
                        response.setCharacterEncoding("utf-8");
                        response.getWriter().write(result);
                        return new ModelAndView();
                    } else {
                        //TEXT/HTML
                        return new ModelAndView("error/500");
                    }
                }
            } catch (IOException e) {
                log.error("resolver ex", e);
            }

            return null;
        }
    }
    - HTTP 요청 헤더의 ACCEPT 값이 application/json 이면 JSON으로 오류를 내려주고, 그 외 경우에는 error/500에 있는 HTML 오류 페이지를 보여준다.
    
    ● WebConfig에 UserHandleExceptionResolver 추가
    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        resolvers.add(new MyHandlerExceptionResolver());
        resolvers.add(new UserHandlerExceptionResolver());
    }
    - postman 실행 : http://localhost:8080/api/members/user-ex
      - ACCEPT : application/json
            {
                "ex" : "hello.exception.exception.UserException",
                "message" : "사용자 오류"
            }

    ● 정리
    ExceptionResolver를 사용하면 컨트롤러에서 예외가 발생해도 ExceptionResolver에서 예외를 처리해버린다.
    따라서 예외가 발생해도 서블릿 컨테이너까지 예외가 전달되지 않고,스프링 MVC에서 예외 처리는 끝이 난다.
    결과적으로 WAS 입장에서는 정상 처리가 된 것이다. 이렇게 예외를 이곳에서 모두 처리할 수 있다는 것이 핵심이다.
    서블릿 컨테이너까지 예외가 올라가면 복잡하고 지저분하게 추가 프로세스가 실행된다. 반면에 
    ExceptionResolver 를 사용하면 예외처리가 상당히 깔끔해진다.
    그런데 직접 ExceptionResolver를 구현하려고 하니 상당히 복잡하다. 지금부터 스프링이 제공하는 
    ExceptionResolver 들을 알아보자.
