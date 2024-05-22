# 본문

### Jar vs War
    JSP를 사용하지 않기 때문에 Jar를 사용하는 것이 좋다.(SpringBoot)
    Jar를 사용하면 항상 내장 서버(톰캣등)를 사용하고, webapp 경로도 사용하지 않는다. 내장 서버 사용에 최적화 되어 있는 기능이다.
    War를 사용하면 내장 서버도 사용가능 하지만, 주로 외부 서버에 배포하는 목적.

### Logging
    앞으로는 System.out.println() 같은 시스템 콘솔을 사용해서 필요한 정보를 출력하지 않고, 로깅 라이브러리를 사용해서 로그를 출력한다.

    ● SLF4J 
    ● Logback

    로그 라이이브러리는 Logback, Log4J, Log4J2 등 수 많은 라이브러리가 있지만, 통합해서 인터페이스로 제공하는 것이 바로 SLF4J 라이브러리다.
    쉽게 말해, SLF4J는 interface이고, 그 구현체로 Logback 같은 라이이브러리를 선택하면 된다.

### 로그 선언
    ● private Logger log = LoggerFactory.getLogger(getClass());
    ● private static final Logger log = LoggerFactory.getLogger(Xxx.class)    
    ● @Slf4j : 롬복 사용 가능

    // @Slf4j
    @RestController
    public class LogTestController {

        private Logger log = LoggerFactory.getLogger(getClass());

        @RequestMapping("/log-test")
        public String logTest() {
            String name = "Spring";

            log.trace("trace log={}", name);
            log.debug("debug log={}", name);
            log.info(" info log={}", name);
            log.warn(" warn log={}", name);
            log.error("error log={}", name);
            
            //로그를 사용하지 않아도 a+b 계산 로직이 먼저 실행됨, 이런 방식으로 사용하면 X
            og.debug("String concat log=" + name);
            return "ok";
        }
    }

    ● 매핑 정보
    @RestController
        -@Controller는 반환 값이 String이면 뷰 이름으로 인식된다.
        그래서 뷰를 찾고 뷰가 랜더링 된다.

        -@RestController는 반환 값으로 뷰를 찾는 것이 아니라, HTTP 메시지 바디에 바로 입력한다. 따라서 실행 결과로 OK 메세지를 받을 수 있다.

    ● 로그 레벨 설정
        -level : trace > debug > info > warn >error
        -개발 서버는 debug 출력
        -운영 서버는 info 출력
        -전체 로그 레벨 설정(기본 info)
            logging.level.root=info

### 로그 사용시 장저
    ● 쓰레드 정보, 클래스 이름 같은 부가 정보를 함께 볼 수 있고, 출력 모양 조절 가능
    ● 로그 레벨에 따라 개발 서버에서는 모든 로그를 출력하고, 운영서버에서는 출력하지 않는 등 로그를 상황에 맞게 조절할 수 있다.
    ● 시스템 아웃 콘솔에만 출력하는 것이 아니라, 파일이나 네트워크 등, 로그를 별도의 위치에 남길 수 있다.
    ● 성능도 일반 System.out 보다 좋다. (내부 버퍼링, 멀티 쓰레드 등등)

### Mapping Controlelr - 요청 매핑
    @RestController
    public class MappingController {

        private Logger log = LoggerFactory.getLogger(getClass());

        /**
         * 기본 요청
         * 둘다 허용 /hello-basic, /hello-basic/
         * HTTP 메서드 모두 허용 GET, HEAD, POST, PUT, PATCH, DELETE
         */ 
        @RequestMapping("/hello-basic")
        public String helloBasic() {
            log.info("helloBasic");
            return "ok";
        } 
    }

    ● 매핑 정보 (스프링 부트 3.0 이전)
    다음 두가지 요청은 다른 URL이지만, 스프링은 다음 URL 요청들을 같은 요청으로 매핑한다.
        -매핑 : /hello-basic
        -URL 요청 : /hello-basic, /hello-basic/

    ● 매핑 정보 (스프링 부트 3.0 이후)
    스프링 부트 3.0부터는 /hello-basic, /hello-basic/ 는 서로 다른 URL 요청을 사용해야 한다. 기존에는 마지막에 있는 '/'를 제거했지만, 스프링 부트 3.0부터는 마지막의 '/'를 유지한다. 따라서 다음과 같이 다르게 매핑해서 사용해야 한다.

    매핑 : /hello-basic -> URL 요청 : /hello-basic
    매핑 : /hello-basic/ -> URL 요청 : /hello-basic/

### PathVariable(경로 변수) 사용
    /**
     * PathVariable 사용
     * 변수명이 같으면 생략 가능
     * @PathVariable("userId") String userId -> @PathVariable userId
     */
     @GetMapping("/mapping/{userId}")
     public String mappingPath(@PathVariable("userId") String data) {
        log.info("mappingPath userId={}", data);
        return "ok";
     }

### RequestHeaderController - HTTP 기본, 헤더 조회
    @Slf4j
    @RestController
    public class RequestHeaderController {

        @RequestMapping("/headers")
        public String headers(HttpServletRequest request,
                              HttpServletResponse response,
                              Locale locale,
                              @RequestHeader MultiValueMap<String, String> headerMap,
                              @RequestHeader("host") String host,
                              @CookieValue(value = "myCookie", required = false) String cookie) {

            log.info("request={}", request);
            log.info("response={}", response);
            log.info("httpMethod={}", httpMethod);
            log.info("locale={}", locale);
            log.info("headerMap={}", headerMap);
            log.info("header host={}", host);
            log.info("myCookie={}", cookie);

            return "ok";
        }
    }

### HTTP 요청 파라미터 - 쿼리 파라미터, HTML Form
    ● HTTP 요청 데이터 조회 
    클라이언트에서 서버로 요청 데이터를 전달할 때는 주로 3가지 방법을 사용한다.
    ● GET - 쿼리 파라미터
        -/url?username=hello&age=20
        -메시지 바디 없이, URL의 쿼리 파라미터에 데이터를 포함해서 전달
        -ex) 검색, 필터, 페이징등등
    ● POST - HTML Form
        -content-type : application/x-www-form-urlencoded
        -메시지 바디에 쿼리 파라미터 형식으로 전달 username=hello&age=20
        -ex) 회원 가입, 상품 주문, HTML Form 사용    
    ● HTTP message body에 데이터를 직접 담아서 요청
        -HTTP API에서 주로 사용, JSON, XML, TEXT
        -데이터 형식은 주로 JSON 사용
        -POST, PUT, PATCH   

    ● 요청 파라미터 - 쿼리 파라미터, HTML Form
    HttpServletRequest의 requeset.getParameter()를 사용하면 다음 두가지 요청 파라미터를 조회할 수 있다.

        GET, 쿼리 파라미터 전송
        ex) http://localhost:8080/request-param?username=hello&age=20

        POST, HTML Form 전송
        ex) 
        POST /request-param ...
        content-type : application/x-www-form-urlencoded

        username=hello&age=20

    GET 쿼리 파라미터 전송 방식이든, POST HTML Form 전송 방식이든 둘다 형식이 같으므로 구분없이 조회할 수 있다.
    이것을 간단히 요청 파라미터(request parameter) 조회라 한다.

### RequestParamController
    @Slf4j
    @Controller
    public class RequestParamController {
        /*
         * 반환 타입이 없으면서 이렇게 응답에 값을 직접 넣으면, view 조회x
         * 반환 타입 void = response.~~ 안된다는 코드
         */
         @RequestMapping("/request-param-v1")
         public void reqeustParamV1(HttpServletRequest request, HttpServletResponse response) throws IOException {
            String username = request.getParameter("username");
            int age = Integer.parseInt(request.getParameter("age"));

            response.getWriter().write("ok");
         }
    }

    ● 위에 코드에서는 단순히 HttpServletRequest가 제공하는 방식으로 조회.

    ● 참고
    Jar를 사용하면 webapp 경로를 사용할 수 없다. 정적 resources도 클래스 경로에 함께 포함해야 한다.

### @RequestParam - requestParamV2
    /**
     * @RequestParam 사용
     * -파라미터 이름으로 바인딩
     * @ResponseBody추가
     * -View 조회를 무시하고, Http message body에 직접 해당 내용 입력 
     */
    @ResponseBody
    @RequestMapping("/request-param-v2") 
    public String requestParamV2(
            @RequestParam("username") String memberName,
            @RequestParam("age") int age) {

        return "ok";
    }

    ● @RequestParam : 파라미터 이름으로 바인딩
    ● @ResponseBody : view 조회를 무시하고, HTTP message body에 직접 내용 입력.

    ● @RequestParam의 name(value) 속성의 파라미터 이름으로 사용.
        @RequestParam("username") String memberName
            - request.getParameter("username")

### @RequestParam - requestParamV3    
    /**
     * @RequestParam 사용
     * HTTP 파라미터 이름이 변수 이름과 같으면 @RequestParam(name="xx") 생략 가능
     */
    @ResponseBody
    @RequestMapping("/request-param-v3")
    public String requestParamV3(
            @RequestParam String username,
            @RequestParam int age) {

        return "ok";
    }

### @RequestParam - requestParamV4
    /**
     * @RequestParam 사용
     * String, int, Integer 등의 단순 타입이면 @RequestParam도 생략 가능
     */
    @ResponseBody
    @RequestMapping("/request-param-v3")
    public String requestParamV3(String username,int age) {
        return "ok";
    } 

    ● @RequestParam을 생략하면 스프링 MVC는 내부에서 required=false를 적용

### @RequestParam - requestParamRequired
    /**
     * @RequestParam.required
     * /request-param-required -> username이 없으므로 400예외
     *
     * 주의!
     * /request-param-required?username= -> 빈문자로 통과
     *
     * 주의!
     * /request-param-required
     * int age -> null을 int에 입력하는 것은 불가능, 따라서 Integer      변경해야 함(또는 다음에 나오는 -defaultValue 사용)
     */
    @ResponseBody
    @RequestMapping("/request-param-required")
    public String requestParamRequired(
            @RequestParam(required = true) String username,
            @RequestParam(required = false) Integer age) {
        
        return "ok";
    }

    ● 기본 값을 적용할 수 있다.
        @RequestParam(defaultValue = "guest") String username,
        파라미터에 값이 없는 경우 defaultValue를 사용하면 기본 값 적용 가능.
        이미 기본 값이 있으므로 required는 의미가 없다.

### @RequestParam - requestParamMap 
    /**
     * @RequestParam Map, Map(key = value)
     */
    @ResponseBody
    @RequestMapping("/required-param-map") 
    public String requestParamMap(@RequestParam Map<String, Object> paramMap) {
        return "ok";
    }

### HTTP 요청 파라미터 - @ModelAttributeV1
    ● 보통 요청 파라미터를 받아서 객체를 만들고, 그 객체에 값을 넣어주는 코드.
        -나는 이런 방법을 Command Object 객체를 이용한다. 라고 알고 있다.
        @RequestParam String username,
        @RequestParam int age,

        HelloData data = new HelloData();
        data.setUsername(username);
        data.setAge(age);

    ● ModelAttribute 사용
        HelloData
        @Data
        public class HelloData{
            private String username;
            private int age;
        }

        ● @Data(lombok)
        @Getter, @Setter, @ToString, @RequiredArgsConstructor를 자동 적용.

    /**
     * 참고 : model.addAttribute(helloData) 코드도 자동 적용,
     */
    @ResponseBody
    @RequestMapping("/model-attribute-v1")
    public String modelAttributeV1(@ModelAttribute HelloData helloData) {
        return "ok";
    }

    ● HelloData 객체가 생성되고, 요청 파라미터 값도 모두 들어가 있다.
        1) HelloData 객체를 생성한다.
        2) 요청 파라미터의 이름으로 HelloData 객체의 프로퍼티를 찾는다.
            해당 프로퍼티의 setter를 호출해서 파라미터의 값을 바인딩 한다.
            ex) 파라미터 이름이 username이면, setUsername() 메서드를 찾아서 호출하면서 값을 입력한다.

    ● Property
    객체에 getUsername(), setUsername() 메서드가 있다면, 이 객체는 username 이라는 프로퍼티를 가지고 있다.
    username 프로퍼티의 값을 변경하면 setUsername() 이 호출되고, 조회하면 getUsername() 이 호출된다.       

### HTTP 요청 파라미터 - RequestBodyStringController
    ● 요청 파라미터와 다르게, HTTP 메시지 바디를 통해 데이터가 직접 넘어오는 경우는 @RequestParam, @ModelAttribute 를 사용할 수 없다.
    (물론 html form 형식으로 전달되는 경우는 요청 파라미터로 인정된다.)

    /**
     * HTTP 메시지 바디의 데이터를 InputStream 을 사용해서 직접 읽을 수 있다.
     */     
    @Slf4j
    @Controller
    public class RequestBodyStringController {

        @PostMapping("/request-body-string-v1")
        public void requestBodyString(HttpServletRequest request,
        HttpServletResponse response) throws IOException {

            ServletInputStream inputStream = request.getInputStream();
            String messageBody = StreamUilts.copyToString(inputStream, StandardCharset.UTF_8);

            response.getWriter().writer("ok");
        }
    }
     
### Input,Output 스트림, Reader - requestBodyStringV2
    /**
     * InputStream(Reader) : HTTP 요청 메시지 바디의 내용을 직접 조회
     * OutputStream(Reader) : HTTP 요청 메시지 바디의 내용을 결과 출력
     */     
    @PostMapping("/request-body-string-v2")
    public void requestBodyStringV2(InputStream, Writer responseWriter) throws IOException {

        String messageBody = StreamUtils.copyToSring(inputStream, StandardCharsets.UTF_8);

        responseWriter.write("ok");
    }

### HttpEntity - requestBodyStringV3
    /**
     * HttpEntity: HTTP header, body 정보를 편리하게 조회
     * - 메시지 바디 정보를 직접 조회(@RequestParam X, @ModelAttribute X)
     * - HttpMessageConverter 사용 -> StringHttpMessageConverter 적용
     *
     * 응답에서도 HttpEntity 사용 가능
     * - 메시지 바디 정보 직접 반환(view 조회X)
     * - HttpMessageConverter 사용 -> StringHttpMessageConverter 적용
     */
    @PostMapping("/request-body-string-v3")
    public HttpEntity<String> requestBodyStringV3(HttpEntity<String> httpEntity) {
        String messageBody = httpEntity.getBody();

        return new HttpEntity<>("ok");
    }

### @RequestBody - requestBodyStringV4
    /**
     * @ResponseBody
     * -메시지 바디 정보를 직접 반환(view 조회X)
     */
    @ResponseBody
    @PostMapping("/request-body-string-v4")
    public String requestBodyString(@RequestBody String messageBody) {
        return "ok";
    } 

    ● 참고로 헤더 정보가 필요하다면 HttpEntity를 사용하거나 @RequestHeader를 사용하면 된다.
    이렇게 메시지 바디를 직접 조회하는 기능은 요청 파라미터를 조회하는 @RequestParam, @ModelAttribute와는 전혀 관계가 없다.