## 본문

### Exception - 예외
    ● 자바 직접 실행
    자바의 메인 메서드를 직접 실행하는 경우 main이라는 이름의 쓰레드가 실행된다. 실행 도중에 예외를 잡지 못하고 처음 실행한 main() 메서드를 넘어서 예외가 던져지면, 예외 정보를 남기고 해당 쓰레드는 종료된다.

    ● 웹 애플리케이션
    웹 애플리케이션은 사용자 요청별로 별도의 쓰레드가 할당되고, 서블릿 컨테이너 안에서 실행된다. 애플리케이션에서 예외가 발생했는데, 어디선가 try ~ catch로 예외를 잡아서 처리하면 아무런 문제가 없다.
    그런데 만약에 애플리케이션에서 예외를 잡지 못하고, 서블릿 밖으로 까지 예외가 전달되면 어떻게 동작할까?

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

    WAS는 해당 예외를 처리하는 오류 페이지 정볼르 확인한다.
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
