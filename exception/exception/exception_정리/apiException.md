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

### ExceptionHandler    
    스프링 프레임워크에서 예외를 처리하기 위한 애너테이션이다. 주로 컨트롤러 클래스 내에서 사용되면, 특정 예외가 발생했을 때 해당 예외를 처리하는 메서드를 지정할 수 있다.

    @ControllerAdvice
    public class GlobalExceptionHandler {

        @ExceptionHandler(IllegalArgumentException.class)
        public String handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "error/illegalArgumentError"; // 에러 페이지로 리닥
        }

        @ExceptionHandler(Exception.class) 
        public String handleException(Exception ex, Model model) {
            model.addAttribute("errorMessage", "An unexpected error occurred : " + ex.getMessage());
            return "error/genericError"; // 에러 페이지로 리닥
        }
    }
    - 위 코드에서 GlobalExceptionHanlder 클래스가 @ControllerAdvice 어노테이션으로 지정되어, 모든 컨트롤러에서 발생하는 예외를 처리할 수 있다.
    handleIllegalArgumentException 메서드는 IllegalArgumentException이 발생할 때 호출되며, handleException 메서드는 모든 종류의 예외를 처리.


### API 예외 처리 - HandlerExceptionResolver 시작
    ● 목표
    예외가 발생해서 서블릿을 넘어 WAS까지 예외가 전달되면 HTTP 상태코드가 500으로 처리된다. 발생하는 예외에 따라서 400, 404 등등 다른 상태코드로 처리하고 싶다. 오류 메시지, 형식들을 API마다 다르게 처리하고 싶다.        

    ● 상태코드 반환
    예를 들어서 IllegalArgumentExcepton을 처리하지 못해서 컨트롤러 밖으로 넘어가는 일이 발생하면 HTTP 상태코드를 400으로 처리하고 싶다. 어떻게 해야할까?

### IllegalArgumentException
    IllegalArgumentException은 잘못된 인수(argument)가 메서드에 전달되었을 때 발생하는 예외이다. 주로 메서드의 인수 검증 시 사용된다.

    ● 예제 코드
    public class UserService {
        
        public User findUserById(String userId) {
            if (userId == null || userId.trim().isEmpty()) {
                throw new IllegalArgumentException("User ID must not be null or empty");
            }

            // 논리적으로 올바른 userId에 대해서만 사용자 검색 로직을 수행
            // ex) DB에서 사용자 검색
            User user = userRepository.findById(userId);
            if (user == null) {
                throw new IllegalArgumentException("User not found for ID: " + userId);
            }
            return user;
        }
    }
    - 위 코드에서는 findUserById 메서드가 인수로 userId를 받는다. 만약 userId 가 null 이거나 비어있으면 IllegalArgumentException을 발생 시킨다.

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

    ● ExceptionResolver : Spring MVC에서 예외를 처리하기 위한 인터페이스.

    ● HandlerExceptionResolver : ExceptionResolver 인터페이스를 확장시킨 인터페이스이다.

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

### API 예외 처리 - 스프링이 제공하는 ExceptionResolver1
    스프링 부트가 기본으로 제공하는 ExceptionResolver는 다음과 같다.
    HandlerExceptionResolverComposite에 다음 순서로 등록
    1) ExceptionHandlerExceptionResolver
       - @ExceptionHandler을 처리한다. API 예외 처리는 대부분이 이 기능으로 해결한다. 
    2) ResponseStatusExceptionResolver
       - HTTP 상태 코드를 지정해준다. 
         - @ResponseStatus(value = HttpStatus.NOT_FOUND)
    3) DefaultHandlerExceptionResolver -> 우선 순위가 가장 낮다.
       - 스프링 내부 기본 예외를 처리한다.

### ResponseStatusExceptionResolver
    - HandlerExceptionResolver 의 구현체. @ResponseStatus 어노테이션이 붙은 예외나 ResponseStatusException을 던졌을 때, HTTP 상태 코드를 설정하고 적절한 응답을 반환한다.
    예외에 따라서 HTTP 상태 코드를 지정해주는 역할을 한다.
    - @ResponseStatus 가 달려있는 예외
    - ResponseStatuseException 예외
    
    @RestController
    @RequestMapping("/api")
    public class ExampleController {

        @GetMapping("/example")
        public ResponseEntity<String> exampleEndpoint() {
            if (true)  {
                throw new ResponseStatausException(HttpStatus.BAD_REQUEST, "error message");
            }
            return ResponseEntity.ok("success");
        }
    }
    - 위 코드에서 ResponseStatusException을 던지면, ResponseStatusExceptionResolver가 이를 처리하여 적절한 상태 코드와 메시지를 클라이언트에게 반환한다.

    @ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "잘못된 요청 오류")
    public class BadRequestException extends RuntimeException {}
      
    - BadRequestException 예외가 컨트롤러 밖으로 넘어가면 ResponseStatusExceptionResolver 예외가 해당 애노테이션을 확인해서 오류 코드를 HttpStatus.BAD_REQUEST(400)으로 변경하고, 메시지도 담는다.
    
    - ResponseStatusExceptionResolver 코드를 확인해보면 결국 response.sendError(statusCode, resolvedReason)를 호출하는 것을 확인할 수 있다.
    sendError(400)를 호출했기 때문에 WAS에서 다시 오류 페이지(/error)를 내부 요청한다.

    ● ApiExceptionController - 추가
    @GetMapping("/api/response-status-ex1")
    public String responseStatusEx1() {
        throw new BadRequestException();
    }
    - 실행 http://localhost:8080/api/response-status-ex1?message=
        {
            "status": 400,
            "error": "Bad Request",
            "exception": "hello.exception.exception.BadRequestException",
            "message":"잘못된 요청 오류",
            "path": "/api/response-status-ex1"
        }
    - 메시지 기능 : reason을 MessageSource에서 찾는 기능도 제공한다. reason = "error.bad"
        // @ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "잘못된 요청 오류")
        @ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "error.bad")
        - error.bad = 잘못된 요청 오류입니다. (매시지 사용)
    - 메시지 사용 결과
        {
            "status" : 400,
            "error" : "Bad Request",
            "exception" : "hello.exception.exception.BadRequestException",
            "message" : "잘못된 요청 오류입니다. 메시지 사용",
            "path" : "/api/response-status-ex1"
        }
    
### ResponseStatusException
    - Spring 5에서 도입된 예외 클래스로, 상태 코드와 메시지를 지정하여 예외를 던질 수 있다. 이를 통해 더 간단하고 직관적으로 예외를 처리할 수 있다.
    @ResponseStatus는 개발자가 직접 변경할 수 없는 예외에는 적용할 수 없다.(어노테이션을 직접 넣어야 하는데, 내가 코드를 수정할 수 없는 라이브러리 의 예외 코드 같은 곳에는 적용할 수 없다.)
    추가로 어노테이션을 사용하기 때문에 조건에 따라 동적으로 변경하는 것도 어렵다. 이때는 ResponseStatusException 예외를 사용하면 된다.

    1)    
    @GetMapping("/api/response-status-ex2")
    public String responseStatusEx2() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "error.bad", new IllegalArgumentException());
    }

    2)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    @RestController 
    @RequestMapping("/api")
    public class ExampleController(@PathVariable Long id) {
        Resource resource = findResourceById(id);
        if (resource == null) {
            throw new ResourceNotFoundException("Resource not found with id: " + id);
        }
        return resource;
    }

### API 예외 처리 - 스프링이 제공하는 ExceptionResolver2
    DefaultHandlerExceptionResolver는 스프링 내부에서 발생하는 스프링 예외를 해결한다. 대표적으로 파라미터 바인딩 시점에 타입이 맞지 않으면 내부에서 TypeMismatchException이 발생하는데, 이 경우 예외가 발생했기 때문에 그냥 두면 서블릿 컨테이너까지 오류가 올라가고, 결과적으로 500 오류가 발생한다.
    그런데 파라미터 바인딩은 대부분 클라이언트가 HTTP 요청 정보를 호출해서 발생하는 문제이다. HTTP 에서는 이런 경우 HTTP 상태 코드 400을 사용하도록 되어 있다. DefaultHandlerExceptionResolver는 이것을 500 오류가 아니라 HTTP 상태 코드 400 오류로 변경한다.

    - 코드 확인
    DefaultHandlerExceptionResolver.hadlerTypeMismatch를 보면 다음과 같은 코드를 확인할 수 있다.
    response.sendError(HttpServletResponse.SC_BAD_REQUEST)(400) 결국 response.sendError()를 통해서 문제를 해결한다.

    ● ApiExceptionController - 추가
    @GetMapping("/api/default-handler-ex")
    public String defaultException(@RequestParam Integer data) {
        return "ok";
    }
    - Integer data에 문자를 입력하면 내부에서 TypeMismachException이 발생.
    - 실행 http://localhost:8080/api/default-handler-ex?data=hello&message=
        {
            "status": 400,
            "error": "Bad Request",
            "exception": "org.springframework.web.method.annotation.MethodArgumentTypeMismatchException"
            "message": "Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer'; nested exception is java.lang.NumberFormatException: For input string: \"hello\"",
            "path": "/api/default-handler-ex"
        } 
        - 결과를 보면 HTTP 상태 코드가 400인 것을 확인.

    ● 정리
    ResponseStatusExceptionResolver -> HTTP 응답 코드 변경
    DefaultHandlerExceptionResolver -> 스프링 내부 예외 처리
    - 지금까지 HTTP 상태 코드를 변경하고, 스프링 내부 예외의 상태코드를 변경하는 기능도 알아보았다.
    그런데 HandlerExceptionResolver를 직접 사용하기는 복잡하다. API 오류 응답의 경우 response에 직접 데이터를 넣어야 해서 매우 불편하고 번거롭다. ModelAndView를 반환해야 하는 것도 API에는 잘 맞지 않는다.
    스프링은 이 문제를 해결하기 위해 @ExceptionHandler라는 매우 현식적인 예외 처리 기능을 제공한다. 그것이 ExceptionHandlerExceptionResolver

### API 예외 처리 - @ExceptionHandler
    HTML 화면 오류 vs API 오류
    웹 브라우저에 HTML 화면을 제공할 때는 오류가 발생하면 BasicErrorController를 사용하는게 편하다.
    이때는 단순히 5xx, 4xx 관련된 오류 화면을 보여주면 된다. BasicErrorController는 이런 메커니즘을 모두 구현해두었다.    

    그런데 API는 각 시스템 마다 응답의 모양도 다르고, 스펙도 모두 다르다. 예외 상황에 단순히 오류 화면을 보여주는 것이 아니라 예외에 따라서 각각 다른 데이터를 출력해야 할 수도 있다.
    그리고 같은 예외라고 해도 어떤 컨트롤러에서 발생했는가에 따라서 다른 예외 응답을 내려주어야 할 수 있다. 한마디로 매우 세밀한 제어가 필요하다.

    결국 지금까지 살펴본 BasicErrorController를 사용하거나 HandlerExceptionResolver를 직접 구현하는 방식으로 API 예외를 다루기 쉽지 않다.
    
    ● API 예외 처리의 어려운 점
    - HandlerExceptionResolver를 떠올려 보면 ModelAndView를 반환해야 했다. 이것은 API 응답에는 필요하지 않다.
    - API 응답을 위해서 HttpServletResponse에 직접 응답 데이터를 넣어주었다. 이것은 매우 불편하다. 스프링 컨트롤러에 비유하면 마치 과거 서블릿을 사용하던 시절이다.
    - 특정 컨트롤러에서만 발생하는 예외를 별도로 처리하기 어렵다. 예를 들어서 회원을 컨트롤러에서 발생하는 RuntimeException 예외와 상품을 관리하는 컨트롤러에서 발생하는 동일한 RuntimeException 예외를 서로 다른 방식으로 처리하고 싶다면 어떻게 해야할까?

    ● @ExceptionHandler
    스프링은 API 예외 처리 문제를 해결하기 위해 @ExceptionHandler라는 어노테이션을 사용하는 매우 편리한 예외 처리 기능을 제공하는데, 이것이 바로 ExceptionHandlerExceptionResolver 이다.
    스프링은 ExceptionHandlerExceptionResolver 를 기본으로 제공하고, 기본으로 제공하는 ExceptionResolver 중에 우선순위도 가장 높다. 실무에서 대부분 이 기능을 사용한다.

    ● ErrorResult
    @Data
    @AllArgsConstructor
    public class ErrorReesult {
        private String code;
        private String message;
    }            

    ● ApiExceptionV2Controller
    @Slf4j
    @RestController
    public class ApiExceptionV2Controller {

        @ResponseStatus(HttpStatus.BAD_REQUEST)
        @ExceptionHandler(IllegalArgumentException.class)
        public ErrorResult illegalExHandle(IllegalArgumentException e) {
            log.error("[exceptionHandle] ex", e);
            return new ErrorResult("BAD", e.getMessage());
        }

        @ExceptionHandler
        public ResponseEntity<ErrorResult> userExHandle(UserException e) {
            log.error("[exceptionHandle] ex", e);
            ErrorResult errorResult = new ErrorResult("USER-EX", e.getMessage());
            return new ResponseEntity<>(errorResult, HttpStatus.BAD_REQUEST);
        }

        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        @ExceptionHandler
        public ErrorResult exHandle(Exception e) {
            log.error("[exceptionHandle] ex", e);
            return new ErrorResult("EX", "내부오류");
        }

        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        @ExceptionHandler
        public ErrorResult exHandle(Exception e) {
            log.error("[exceptionHandle] ex", e);
            return new ErrorResult("EX", "내부 오류");
        }

        @GetMapping("/api2/members/{id}")
        public MemberDto getMember(@PathVariable("id") String id) {

            if (id.equals("ex")) {
                throw new RuntimException("잘못된 사용자");
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
    
    ● ExceptionHandler 예외 처리 방법
    @ExceptionHandler 어노테이션을 선언하고, 해당 컨트롤러에서 처리하고 싶은 예외를 지정해주면 된다. 해당 컨트롤러에서 예외가 발생하면 이 메서드가 호출된다. 참고로 지정한 예외 또는 그 예외의 자식 클래스는 모두 잡을 수 있다.

    다음 예제는 IllegalArgumentException 또는 그 하위 자식 클래스를 모두 처리할 수 있다.
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorResult illegalExHandle(IllegalArgumentException e) {
        log.error("[exceptionHandle] ex", e);
        return new ErrorResult("BAD", e.getMessage());
    }

    ● 다양한 예외
    @ExceptionHandler({AException.class, BException.class})
    public String ex(Exception e) {
        log.info("exception e", e);
    }

    ● 예외 생략
    @ExceptionHandler에 예외를 생략할 수 있다. 생략하면 메서드 파라이터의 예외가 지정된다.
    @ExceptionHandler
    public ResponseEntity<ErrorResult> userExHandler(UserException e) {}

    ● 파라미터와 응답
    @ExceptionHandler에는 마치 스프링의 컨트롤러의 파라미터 응답처럼  다양한 파라미터와 응답을 지정할 수 있다.
        
        - postman 실행
        http://localhost:8080/api2/members/bad

        - IllegaArgumentException 처리
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        @ExceptionHandler(IllegalArgumentException.class)
        public ErrorResult illegalExHandle(IllegalArgumentException e) {
            log.info("[exceptionHandle] ex", e);
        }

        - 흐를
        1) 컨트롤러를 호출한 결과 IllegalArgumentException 예외가 컨트롤러 밖으로 던져진다.
        2) 예외가 발생했으므로 ExceptionResolver가 작동한다. 가장 우선순위가 높은 ExceptionHandlerExceptionResolver가 실행된다.
        3) ExceptionHandlerExceptionResolver는 해당 컨트롤러에 IllegalArgumentException을 처리할 수 있는 @ExceptionHandler가 있는지 확인한다.
        4) illegalExHandle()를 실행한다. @RestController이므로 illegalExHandle()에도 @ResponseBody가 적용된다. 따라서 HTTP 컨버터가 사용되고, 응답이 다음과 같은 JSON으로 반환된다.
        5) @ResponseStatus(HttpStatus.BAD_REQUEST)를 지정했으므로 HTTP 상태코드 400으로 응답한다.
     
        - 결과
            {
                "code" : "BAD",
                "message" : "잘못된 입력 값"
            }

    ● UserException 처리
    @ExceptionHandler
    public ResponseEntity<ErrorResult> userExHandle(UserException e) {
        log.error("[exceptionHandle] ex", e);
        ErrorResult errorResult = new ErrorResult("USER-EX", e.getMessage());
        return new ResponseEntity<>(errorResult, HttpStatus.BAD_REQUEST);
    }
    - @ExceptionHandler에 예외를 지정하지 않으면 해당 메서드 파라미터 예뢰를 사용한다. 여기서는 UserException을 사용한다.
    - ResponseEntity를 사용해서 HTTP 메시지 바디에 직접 응답한다. 물론 HTTP 컨버터가 사용된다.
    ResponseEntity를 사용하면 HTTP 응답 코드를 프로그래밍해서 동적으로 변경할 수 있따. 앞서 살펴본 @ResponseStatus는 어노테이션이므로 HTTP 응답 코드를 동적으로 변경할 수 있다.

    - Postman 실행
        http://localhost:8080/api2/members/ex

        ● exception
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        @ExceptionHandler
        public ErrorResult exHandle(Exception e) {
            log.error("[exceptionHandle] ex", e);
            return new ErrorResult("EX", "내부 오류");
        }
        - throw new RuntimeException("잘못된 사용자")이 코드가 실행되면서, 컨트롤러 밖으로 RuntimeException이 던져진다.
        - RuntimeException은 Exception의 자식 클래스이다. 따라서 이 메서드가 호출된다.
        - @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)로 HTTP 상태 코드를 500으로 응답한다.

        ● HTML 오류 화면
        다음과 같이 ModelAndView를 사용해서 오류 화면(HTML)을 응답하는데 사용할 수 있다.
        @ExceptionHandler(ViewException.class)
        public ModelAndView ex(ViewException e) {
            log.info("exception e", e);
            return new ModelAndView("error");
        }

### API 예외 처리 - @ControllerAdvice
    @ExceptionHandler를 사용해서 예외를 깔끔하게 처리할 수 있게 되었지만, 정상 코드와 예외 처리 코드가 하나의 컨트롤러에 섞여 있다.
    @ControlelrAdvice 또는 @RestControllerAdvice를 사용하면 둘을 분리할 수 있다.
    
    ● ExControllerAdvice
    @Slf4j
    @RestControllerAdivce
    public class ExControllerAdvice { // 기존 코드

        @ResponseStatus(HttpStatus.BAD_REQUEST)
        @ExceptionHandler(IllegalArgumentException.class)
        public ErrorResult illegalExHandle(IllegalArgumentException e) {
            log.error("[exceptionHandle ex]", e);
            return new ErrorResult("BAD", e.getMessage());
        }
    }        
    
    ● ApiExceptionV2Controller 코드에 @ExceptionHandler 제거
    public class ApiExceptionV2Controller {

        @GetMapping("/api2/members/{id}")
        public MemberDto goetMember(@PathVariable("id") String id) {

            if (id.equals("ex")) {
                throw new getMemberException("잘못된 사용자");
            }

            return new MemberDto(id, "hello " + id);
        }

        @Data
        @AllArgusConstructor
        static class MemberDto {
            private String memberId;
            private String name;
        }
    }

    ● @ControllerAdivce
    - @ControllerAdvice는 대상으로 지정한 여러 컨트롤러에 @ExceptionHandler, @InitBinder 기능을 부여해주는 역할을 한다.
    - @ControllerAdvice에 대상을 지정하지 않으면 모든 컨트롤러에 적용된다.(글로벌 적용)
    - @RestControllerAdvice는 @ControllerAdvice와 같고, @ResponseBody가 추가되어 있따. @Controller, @RestController의 차이와 같다.

    ● 대상 컨트롤러 지정 방법
    @ControllerAdvice(annotations = RestController.class)
    public class ExampleAdvice {}

    @ControllerAdivce("org.example.controllers")
    public class ExampleAdvice2 {}

    @ControllerAdvice(assignableTypes = {ControllerInterface.class,
    AbstractController.class})
    public class ExampleAdivce3 {}

### blog project apiException - 적용
    ● ExceptionHandler (Subject - cafe)
    @RestControllerAdvice
    @Slf4j
    public class ExceptionHandler {

        // 컨트롤러 DTO validation 핸들러 
        @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<MethodInvalidResponse> methodInvalidException(final MethodArgumentNotValidException e) 
        {

            BindingResult bindingResult = e.getBindingResult();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(MethodInvalidResponse.builder()
                        .errorCode(bindingResult.getFieldErrors().get(0).getCode())
                        .errorMessage(bindingResult.getFieldErrors().get(0).getDefaultMessage())
                        .build());

        }

        // 컨트롤러 @PathVariable TypeMismatch 핸들러 
        @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ExceptionResponse> methodArgumentTypeMismatchException() {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ExceptionResponse.builder()
                        .errorCode(METHOD_ARGUMENT_TYPE_MISMATCH)
                        .errorMessage(METHOD_ARGUMENT_TYPE_MISMATCH.getMessage())
                        .build()
                    );
        }

        // CustomException 핸들러
        @org.springframework.web.bind.annotation.ExceptionHandler(CustomException.class)
        public ResponseEntity<ExceptionResponse> customRequestException(final CustomException c) {
            log.error("Api Exception => {}, {}", cc.getErrorCode(), c.getErrorMessage());
            return ResponseEntity
                    .status(c.getErrorStatus())
                    .body(ExceptionResponse.builder()
                        .errorCode(c.getErrorCode())
                        .errorMessage(c.getErrorMessage())
                        .build()
                    );
        }

        @Getter
        @Builder
        public static class MethodInvalidResponse {

            private String errorCode;
            private String errorMessage;
        }

        @Getter
        @Builder
        public static class ExceptionResponse {

            private ErrorCode errorCode;
            private String errorMessage;
        }
    }

    ● 정리
    Restful API에서 발생하는 예외를 처리하기 위한 전역 예외 처리 핸들러를 정의한 코드입니다. @RestControllerAdvice 와 @ExceptionHandler 어노테이션을 사용하여 다양한 예외 상황에 대해 적절한 HTTP 응답을 반환.

    ● methodInvalidException method
        1) 클래스 및 어노테이션 설명
        - @RestControllerAdvice : 모든 @RestController에서 발생하는 예외를 처리하는 클래스. Spring MVC의 @ControllerAdvice와 유사하지만, REST API를 위한 예외 처리를 지원합니다.

        2) MethodArgumentNotValidException 처리
        - MethodArgumentNotValidException : Spring에서 요청 바디의 @Valid 검증이 실패했을 때 발생하는 예외이다.
        - BindingResult : 검증 실패 결과를 담고 있는 객체
        - ResponseEntity : HTTP 응답을 나타내는 객체로, 상태 코드와 응답 본문을 포함
        - MethodInvalidResponse : 에러 코드와 메시지를 포함하는 커스텀 응답 객체
        
        이 핸들러는 @Valid 검증 실패 시 발생하는 MethodArgumentValidException 을 처리하여, HTTP 400 상태 코드와 함께 검증 오류 메시지를 반환한다.

        3) @ResponseEntity annotation
        Spring Framework에서 HTTP 응답을 표현하는 객체이다. 
        이를 사용하여 Statau, Header, Response 본문 등을 설정할 수 있다.
        ResponseEntity를 구성하는 각 메서드의 목적과 동작 방식은 다음과 같다.

            1) ResponseEntity.Status(HttpStatus status) : 응답의 상태코드
            - 목적 : HTTP 응답의 상태 코드를 설정
            - 동작 : 응답 객체의 상태 코드를 설정하는 빌더를 반환
            - ex)
                    ResponseEntity.status(HttpStatus.BAD_REQUEST)

            2) ResponseEntity.body(Object body) : 응답의 본문            
            - 목적 : HTTP 응답의 본문(body)을 설정
            - 동작 : 응답 객체의 본문을 설정하고, 이를 포함한 ResponseEntity를 반환
            - ex)
                    ResponseEnitty.status(HttpStatus.BAD_REQUEST)
                            .body(new ErrorResponse("error_code", "error_message"))

            3) ErrorResponse.builder() : 빌더 패턴을 사용하여 객체 생성
            - 목적 : 빌더 패턴을 사용하여 ErrorResponse 객체를 생성.
            - 동작 : ErrorResponse 객체를 단계별로 설정할 수 있는 빌더 객체를 반환.
            - ex)
                    ErrorResponse.builder()

            4) errorCode(String errorCode) : 응답 객체의 필드를 설정
            - 목적 : ErrorResponse 객체의 errorCode 필드를 설정
            - 동작 : errorCode 필드를 설정하고, 빌더 객체를 반환
            - ex)   
                    ErrorResponse.builder()
                            .errorCode("error_code")

            5) errorMessage(String errorMessage) : 응답 객체의 필드를 설정
            - 목적 : ErrorResponse 객체의 errorMessage 필드를 설정
            - 동작 : errorMessage 필드를 설정하고, 빌더 객체를 반환
            - ex)
                    ErrorResponse.builder()
                            .errorCode("error_code")
                            .errorMessage("error_message")                            

            6) .build() : 최종 객체를 생성
            - 목적 : 빌더 객체에 설정된 값들을 사용하여 최종 ErrorResponse 객체를 생성
            - 동작 : 설정된 값들을 포함하는 ErrorResponse 객체를 반환
            - ex)
                    ErrorResponse errorResponse = ErrorResponse.builder()
                            .errorCode("error_code")
                            .errorMessage("error_message")
                            .build();                                                              
    
    ● MethodArgumentTypeMismatchException method
        4) MethodArgumentTypeMismatchException : 요청 경로 변수의 타입이 일치하지 않을 때 발생하는 예외.
        5) ExceptionResponse : 에러 코드와 메시지를 포함하는 커스텀 응답 객체.

        이 핸들러는 경로 변수 타입 불일치 시 발생하는 예외를 처리하여, HTTP 400 상태 코드와 함께 오류 메시지를 반환.

    ● CustomException method
        6) CustomException : 애플리케이션에서 정의한 커스텀 예외
        7) log.error : 예외 발생 시 에러 메시지를 로그에 기록
        8) c.getErrorStatus() : 커스텀 예외에 정의된 상태 코드를 반환

        이 핸들러는 애플리케이션에서 발생하는 커스텀 예외를 처리하여, 해당 예외에 정의된 HTTP 상태 코드와 함께 오류 메시지를 반환한다.