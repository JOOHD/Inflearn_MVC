# 본문

### Jar vs War
    JSP를 사용하지 않기 때문에 Jar를 사용하는 것이 좋다.(SpringBoot)
    Jar를 사용하면 항상 내장 서버(톰캣등)를 사용하고, webapp 경로도 사용하지 않는다. 내장 서버 사용에 최적화 되어 있는 기능이다.
    War를 사용하면 내장 서보도 사용가능 하지만, 주로 외부 서버에 배포하는 목적.

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
         */
         @RequestMapping("/request-param-v1")
         public void reqeustParamV1(HttpServletRequest request, HttpServletResponse response) throws IOException {
            String username = request.getParameter("username");
            int age = Integer.parseInt(request.getParameter("age"));

            response.getWriter().write("ok");
         }
    }

