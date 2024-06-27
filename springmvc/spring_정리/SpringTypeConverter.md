## 본문

### Spring Type Converter - 소개
    문자를 숫자로 변환하거나, 반대로 숫자를 문자로 변환해야 하는 것 처럼 애플리케이션을 개발하다 보면 타입을 변환해야 하는 경우가 상당히 많다.
    ex) 
        @RestController
        public class HelloController {

            @GetMapping("/hello-v1")
            public String helloV1(HttpServletRequest request) {
                String data = request.getParameter("data"); //문자 타입 조회
                Integer intValue = Integer.valueOf(data); //숫자 타입으로 변경
                Sytem.out.println("intValue = " + intValue);
                return "ok";
            }
        }
        - HTTP 요청 파라미터는 모두 문자로 처리된다. 따라서 요청 파라미터를 자바에서 다른 타입으로 변환해서 사용하고 싶으면 다음과 같이 숫자 타입으로 변환하는 과정을 거쳐야 한다.

    이번에는 스프링 MVC가 제공하는 @RequestParam을 사용.
    ex)        
        @GetMapping("/hello-v2")
        public String helloV2(@RequestParam Integer data) {
            System.out.println("data = " + data);
            return "ok";
        }
        - http"//localhost:8080/hello-v2?data=10
        - 앞서 보았듯이 HTTP 쿼리 스트링으로 전달하는 data=10 부분에서 10은 숫자 10이 아니라 문자 10이다. 스프링이 제공하는 @RequestParam을 사용하면 이 문자 10을 Integer 타입의 숫자 10으로 편리하게 받을 수 있다.
        - 이것은 스프링이 중간에서 타입을 변환해주었기 때문이다.
        - 이러한 예는 @ModelAttribute, @PathVariable에서도 확인 가능하다.

    ● 스프링의 타입 변환 적용 
    ex)
        1) 스프링 MVC 요청 파라미터
            - @RequestParam, @ModelAttribute, @PathVariable
        2) @Value 등으로 yml 정보 읽기
        3) xml에 넣은 스프링 빈 정보를 변환
        4) view를 랜더링 할 때

    ● 스프링과 타입 변환
    이렇게 타입을 변환해야 하는 경우는 상당히 많다. 개발자가 직접 하나하나 변환을 해야 한다면, 생각만 해도 괴롭다.
    스프링이 중간에 타입 변환기를 사용해서 타입을 String -> Integer로 변환해주었기 때문에 개발자는 편리하게 해당 타입을 바로 받을 수 있다. 앞에서는 문자를 숫자로 변경하는 예시를 들었지만, 반대로 숫자를 문자로 변경하는 것도 가능하고, Boolean 타입을 숫자로 변경하는 것도 가능하다. 
    
    - 만약 개발자가 새로운 타입을 만들어서 변환하고 싶으면 어떻게 될까?     

    스프링은 확장 가능한 컨버터 인터페이스를 제공한다.
    개발자는 스프링에 추가적인 타입 변환이 필요하면 이 컨버터 인터페이스를 구현해서 등록하면 된다. 이 컨버터 인터페이스는 모든 타입에 적용할 수 있다.
    필요하면 X -> Y 타입으로 변환하는 컨버터 인터페이스를 만들고, 또 Y -> X 타입으로 변환하는 컨버터 인터페이스를 만들어서 등록하면 된다.
        ex)
        문자로 "true"가 오면 Boolean 타입으로 받고 싶으면 String -> boolean 타입으로 변환 되도록 컨버터 인터페이스를 만들어서 등록하고, 반대로 적용하고 싶으면 Boolean -> String 타입으로 변환되도록 컨버터를 추가로 만들어서 등록하면 된다.

        ● 참고
        과거에는 PropertyEditor라는 것으로 타입 변환을 했다. PropertyEditor는 동시성 문제가 있어서 타입을 변환할 때 마다 객체를 계속 생성해야 하는 단점이 있다. 지금은 Converter의 등장으로 해당 문제들이 해결되었고, 기능 확장이 필요하면 Converter를 사용하면 된다.

### Type Converter
    org.springframework.core.convert.converter.Converter, 인터페이스 사용.

    - Converter Interface
        public interface Converter<S, T> {
            T convert(S source);
        }

    ● SpringToIntegerConverter - 문자를 숫자로 변환하는 타입 컨버터
    @Slf4j
    public class StringToIntegerConverter implements Converter<String, Integer>{
        @Override
        public Integer convert(String source) {
            log.info("convert source-{}", source);
            return Integer.valueOf(source);
        }
    }            
    - String -> Integer로 변환하기 때문에 소스가 String이 된다.
    이 문자를 Integer.valueOf(source)를 사용해서 숫자로 변경한 다음에 변경된 숫자를 반환하면 된다.

    ● IntegerToStringConverter - 숫자를 문자로 변환하는 타입 컨버터
    @Slfj4
    public class IntegerToStringConverter implements Converter<Integer,  String> {
        @Override
        public String convert(Integer source) {
            log.info("convert source={}", source);
            return String.valueOf(source);
        }
    }

    ● ConverterTest - 타입 컨버터 테스트 코드
    class ConverterTest {

        @Test
        void IntegerToString() {
            IntegerToStringConverter converter = new IntegerToStringConverter();
            String result = converter.convert("10")
            assertThat(result).isEqualTo("10");
        }

        @Test
        void StringToInteger() {
            StringToIntegerConverter converter = new StringToIntegerConverter();
            Integer result = converter.convert("10");
            assertThat(result).isEqualTo(10);
        }
    } 

### 사용자 정의 타입 컨버터
    - ip, port 같은 값을 입력하면 IpPort 객체러 변환하는 컨버터를 만들어보자.
    
    ● IpPort
    @Getter
    @EqualsAndHashCode
    public class IpPort {

        private String ip;
        private int port;

    public IpPort(String ip, int port) {
        this.ip = ip;
        this.port = port;
        }
    }      

    ● StringToIpPortConverter - 컨버터
    @Slf4j
    public class StringToIpPortConverter implements Converter<String, IpPort> {
        @Override
        public IpPort convert(String source) {
            log.info("Convert source={}", source);
            String[] split = source.split(":");
            String ip = split[0];
            int port = Integer.parseInt(split[1]);

            return new IpPort(ip, port);
        }
    }
    - 127.0.0.1:8080 같은 문자를 입력하면 IpPort 객체를 만들어 반환한다

    ● IpPortToStringConverter
    @Slf4j
    public class IpPortToStringConverter implements Converter<IpPort, String> {
        @Override
        public String convert(IpPort source) {
            log.info("convert source={}", source);
            retern source.getIp() + ":" + source.getPort();
        }
    }

    ● ConverterTest - IpPort 컨버터 테스트 추가
    @Test
    void StringToIpPort() {
        StringToIpPortConverter converter = new ~~
        String source = "127.0.0.1:8080";
        IpPort result = converter.convert(source);
        assertThat(result).isEqualTo(new IpPort("127.0.0.1", 8080));
    }

    @Test
    void ipPortToString() {
        IpPortToStringConverter converter = new ~~
        IpPort source = new IpPort("127.0.0.1", 8080);
        String result = converter.convert(source);
        assertThat(result).isEqualTo("127.0.0.1:8080");
    }

    ● 정리
    타입 컨버터 인터페이스가 단순해서 이해하기 어렵지 않을 것이다.
    그런데 이렇게 타입 컨버터를 하나하나 직접 사용하면, 개발자가 직접 컨버팅 하는 것과 큰 차이가 없다. 타입 컨버터를 등록하고 관리하면서 편리하게 변환 기능을 제공하는 역할을 하는 무언가가 필요하다.

    ● 참고
    스프링 용도에 따라 다양한 방식의 타입 컨버터를 제공한다.
    Converter -> 기본 타입 컨버터
    ConverterFactory -> 전체 클래스 계층 구조가 필요할 때
    GenericConverter -> 정교한 구현, 대상 필드의 어노테이션 정보 사용 가능
    ConditionalGenericConverter -> 특정 조건이 참인 경우에만 실행

    스프링은 문자, 숫자, 불린, Enum등 일반적인 타입에 대한 대부분의 컨버터를 기본으로 제공한다. IDE에서 Converter, ConverterFactory, GenericConverter 의 구현체를 찾아보면 수 많은 컨버터를 확인할 수 있다.

### ConversionService 
    이렇게 타입 컨버터를 하나하나 직접 찾아서 타입 변환에 사용하는 것은 매우 불편, 그래서 스프링은 개별 컨버터를 모아두고 그것들을 묶어서 편리하게 사용할 수 있는 기능을 제공하는데, 이것이 바로 컨버전 서비스이다.

    - ConversionService interface
    public interface ConversionService {
        boolean canConvert(@Nullable Class<?> sourceType, Class<?> targetType);
        boolean canConvert(@Nullable TypeDescriptor sourceType, TypeDescriptor targetType);

        <T> T convert(@Nullable object source, Class<T> targetType);
        object convert(@Nullable Object source, @Nullable TypeDescriptor sourceType, TypeDescriptor targetType);
    }

    ● ConversionServiceTest - 컨버전 서비스 테스트 코드
    public class ConversionServiceTest() {
        @Test
        void conversionService() {
            // 등록
            DefaultConversionService conversionService = new ~
            conversionService.addConverter(new StringToIntegerConverter());
            conversionService.addConverter(new IntegerToStringConverter());
            conversionService.addConverter(new StringToIpPortConverter());
            conversionService.addConverter(new IpPortToStringConverter());

            // 사용
            assertThat(conversionService.convert("10", Integer.class)).isEqualTo(10);
            assertThat(conversionService.convert(10, String.class)).isEqualTo("10");

            IpPort ipPort = conversionService.convert("127.0.1:8080", IpPort.class);
            assertThat(ipPort).isEqualTo(new IpPort("127.0.0.1", 8080));

            String ipPortString = conversionService.convert(new IpPort("127.0.1:8080", 8080), String.class);
            assertThat(ipPortString).isEqualTo(new IpPort("127.0.0.1 :8080"));
        }
    }
    - DefaultConversionService는 ConversionService 인터페이스를 구현했는데, 추가로 컨버터를 등록하는 기능도 제공한다.
    
    ● 등록과 사용 분리
    컨버터를 등록할 때는 StringToIntegerConverter 같은 타입 컨버터를 명확하게 알아야 한다. 반면에 컨버터를 사용하는 입장에서는 타입 컨버터를 전혀 몰라도 된다. 타입 컨버터들은 모두 컨버전 서비스 내부에 숨어서 제공된다. 따라서 타입을 변환을 원하는 사용자는 컨버전 서비스 인터페이스에만 의존하면 된다. 물론 컨버전 서비스를 등록하는 부분과 사용하는 부분을 분리하고 의존관계 주입을 사용해야 한다.

        - 컨버전 서비스 이용
        Integer value = conversionService.convert("10", Integer.class)

        - 인테페이스 분리 원칙 - ISP(Interface Segregation Principle)
        인터페이스 분리 원칙은 클라이언트가 자신이 이용하지 않는 메서드에 의존하지 않아야 한다.

        - DefaultConversionService 는 다음 두 인터페이스를 구현했따.
          - ConversionService : 컨버터 사용에 초점
          - ConverterRegistry : 컨터버 등록에 초점
          
        이렇게 인터페이스를 분리하면 컨버터를 사용하는 클라이언트와 컨버터를 등록하고 관리하는 클라이언트의 관심사를 명확하게 분리할 수 있다.
        특히 컨버터를 사용하는 클라이언트는 ConversionService만 의존하면 되므로, 컨버터를 어떻게 등록하고 관리하는지는 전혀 몰라도 된다.
        결과적으로 컨버터를 사용하는 클라이언트는 꼭 필요한 메서드만 알게된다.
        이렇게 인터페이스를 분리하는 것을 ISP라 한다.

### WebConfig - 컨버터 등록
    @Configuration
    public class WebConfig implements WebMvcConfigurer {

        @Override
        public void addFormatters(FormatterRegistry registry) {
            registry.addConverter(new StringToIntegerConverter());
            registry.addConverter(new IntegerToStringConverter());
            registry.addConverter(new StringToIpPortConverter());
            registry.addConverter(new IpPortToStringConverter())
        }
    }
    스프링은 내부에서 ConversionService를 제공한다. 우리는 WebMvcConfigurer가 제공하는 addFormatters()를 사용해서 추가하고 싶은 컨버터를 등록하면 된다. 이렇게 하면 스프링은 내부에서 사용하는 ConversionService에 컨버터를 추가해준다

    ● HelloController - 기존 코드
    @GetMapping
    public String helloV2(@RequestParam Integer data) {
        System.out.println("data = " + data);
        return "ok";
    }

    - 실행 로그
    StringToIntegerConverter : convert source = 10 
    data = 10
    ?data=10의 쿼리 파라미터는 문자이고 이것을 Integer data로 변환하는 과정이 필요하다. 실행해보면 직접 등록한 StringToIntegerConverter 가 작동하는 로그를 확인할 수 있따. 

    그런데 생각해보면 StringToIntegerConverter를 등록하기 전에도 이 코드는 잘 수행되었다. 그것은 스프링 내부에서 수 많은 기본 컨버터들을 제공하기 때문이다. 컨버터를 추가하면 추가한 컨버터가 기본 컨버터 보다 높은 우선순위를 가진다.

    ● HelloController - 추가
    @GetMapping("/ip-port")
    public String ipPort(@RequestParam IpPort ipPort) {
        Sytem.out.println("ipPort IP = " + ipPort.getIp());
        Sytem.out.println("ipPort PORT = " + ipPort.getPort());
        return "ok";
    }
    - 실행 http://localhost:8080/ip-port?ipPort=124.0.1:8080
    StringToIpPortConverter : convert source=127.0.0.1:8080
    ipPort IP = 127.0.0.1
    ipPort PORT = 8080
    ?ipPort=127.0.0.1:8080 쿼리 스트링이 @RequestParam IpPort ipPort에서 객체 타입으로 잘 변환 된 것을 확인할 수 있다.

    ● 처리 과정
    @RequestParam은 @RequestParam을 처리하는 ArgumentResovler인
    RequestParamMethodArgumentResovler에서 ConversionService를 사용해서 타입을 변환한다. 부모 클래스와 다양한 외부 클래스를 호출하는 등 복잡한 내부 과정을 거치기 때문에 대략 이렇게 처리되는 것으로 이해해도 충분하다.
    만약 더 깊이있게 확인하고 싶으면 IpPortConvert에 디버그 브레이크 포인트를 걸어서 확인해보자.

### 뷰 템블릿에 컨버터 적용하기
    이번에는 뷰 템플릿에 컨버터를 적용하는 방법
    타임리프는 렌더링 시에 컨버터를 적용해서 렌더링 하는 방법을 편리하게 지원한다.
    이전까지는 문자를 객체로 변환했다면, 이번에는 그 반대로 객체를 문자로 변환하는 작업을 확인할 수 있다.

    ● ConverterController
    @Controller
    public class ConverterController {

        @GetMapping("/converter-view")
        public String converterView(Model model) {
            model.addAttribute("number", 1000);
            model.addAttribute("ipPort", new IpPort("127.0.0.1", 8080));
            return "converter-view";
        }
    }

    ● resources/templates/converter-view.html
    <html xmlns:th="http://www.thymeleaf.org">
    <head>
        <meta charset="UTF-8">
        <title>Title</>
    </>
    <body>
    <ul>  
        <li>${number}: <span th:text="${number}"></></>  
        <li>${{number}}: <span th:text="${{number}}" ></span></li>
        <li>${ipPort}: <span th:text="${ipPort}" ></span></li>
        <li>${{ipPort}}: <span th:text="${{ipPort}}" ></span></li>
    </ul>
    </body> 

    - 타임리프는 ${{...}}를 사용하면 자동으로 컨버전 서비스를 사용해서 변환된 결과를 출력해준다. 물론 스프링과 통합 되어서 스프링이 제공하는 컨버전 서비스를 사용하므로, 우리가 등록한 컨버터들을 사용할 수 있다.
    - 변수 표현식 : ${...}
    - 컨버전 서비스 적용 : ${{...}}

    ● 실행 결과 http://localhost:8080/converter-view
        - ${number} : 10000
        - ${{number}} : 10000
        - ${ipPort} : hello.typeconverter.type.IpPORT@59CB0946
        - ${{ipPort}} : 127.0.0.1:8080

    - ${{number}} : 뷰 템플릿은 데이터를 문자로 출력한다. 따라서 컨버터를 적용하게 되면 Integer 타입인 10000을 String 타입으로 변환하는 컨버터인 IntegerToStringConverter를 실행하게 된다. 이 부분은 컨버터를 실행하자ㅣ 않아도 타임리프가 숫자를 문자로 자동으로 변환하기 때문에 컨버터를 적용할 때와 하지 않을 때가 같다.
    - ${{ipPort}} : 뷰 템플릿은 데이터를 문자로 출력한다. 따라서 컨버터를 적용하게 되면 IpPort 타입을 String 타입으로 변환해야 하므로 IpPortToStringConverter가 적용된다. 그 결과 127.0.0.1:8080가 출력된다.

### ConverterController - 폼에 적용    
    @Controller
    public class ConverterController {

        @GetMapping("/converter-view")
        public String converterView(Model model) {
            model.addAttribute("number", 10000);
            model.addAttribute("ipPort", new IpPort("127.0.0.1", 8080));
            return "converter-view";
        }

        @GetMapping("/converter/edit")
        public String converterForm(Model model) {
            IpPort ipPort = new IpPort("127.0.0.1", 8080);
            Form form = new Form(ipPort);

            model.addAttribute("form", form);
            return "converter-form";
        }

        @PostMapping("/converter/edit")
        public String converterEdit(@ModelAttribute Form form, Model model) {
            IpPort ipPort = form.getIpPort();
            model.addAttribute("ipPort", ipPort);
            return "converter-view";
        }

        @Data
        static class Form {
            private IpPort ipPort;

            public Form(IpPort ipPort) {
                this.ipPort = ipPort;
            }
        }
    }
    - GET / converter/edit : IpPort를 뷰 템플릿 폼에 출력한다.
    - POST / converter/edit : 뷰 템플릿 폼의 IpPort 정보를 받아서 출력한다.

    <form th:object="${form}" th:method="post">
        th:field <input type="text" th:field="*{ipPort}"><br/>
        th:value <input type="text" th:value="*{ipPort}">(보여주기 용도)<br/>
    <input type="submit"/>
    </form>

    - GET / converter/edit
      - th:field가 자동으로 컨버전 서비스를 적용해주어서 ${{ipPort}} 처럼 적용이 되었다. 따라서 IpPort -> String으로 변환
    - POST / converter/edit
      - @ModelAttribute를 사용해서 String -> IpPort로 변환된다.

### Formatter
    converter는 입력과 출력 타입에 제한이 없는, 범용 타입 변환 기능을 제공한다. 이번에는 일반적인 웹 애플리케이션 환경을 생각해보자, 불린 타입을 숫자로 바꾸는 것 같은 범용 기능 보다는 개발자 입장에서는 문자를 다른 타입으로 변환하거나, 다른 타입을 문자로 변환하는 상황이 대부분이다.
    앞서 살펴본 예제들을 떠올려 보면 문자를 다른 객체로 변환하거나 객체를 문자로 변환하는 일이 대부분이다.

    ● 웹 어플리케이션에서 객체를 문자로, 문자를 객체로 변환하는 예
    - 화면에 숫자를 출력해야 하는데, Integer -> String 출력 시점에 숫자 1000 -> 문자 "1,000" 이렇게 1000 단위에 쉼표를 넣어서 출력하거나, 또는 "1,000"라는 문자를 1000이라는 숫자로 변경해야 한다.
    - 날짜 객체를 문자인 "2024-06-27"와 같이 출력하거나 또는 그 반대의 상황

    이렇게 객체를 특정한 포멧에 맞추어 문자로 출력하거나 또는 그 반대의 역할을 하는 것에 특화된 기능이 formatter이다.

    ● Formatter interface
    public interface Printer<T> {
        String pritn(T object, Locale locale);
    }

    public interface Parser<T> {
        T parse(String text, Locale locale) throws ParseException;
    }

    public interface Formatter<T> extends Printer<T>, Parser<T> {

    }

    ● MyNumberFormatter
    @Slf4j
    public class MyNumberFormatter implements Formatter<Number> {

        @Override
        public Number parse(String text, Locale locale) throws ParseException {
            log.info("text={}, locale=", text, locale);
            NumberFormat format = NumberFormat.getInstance(locale);
            return format.parse(text);
        }

        @Override
        public String print(Number object, Locale locale) {
            log.info("object={}, locale={}", object, locale);
            return NumberFormat.getInstance(locale).format(object);
        }

        @Test
        void parse() throws ParseException {
            Number result = formatter.parse("1,000", Locale.KOREA);
            assertThat(result).isEqualTo(1000L);
        }

        @Test
        void print() {
            String result = formatter.print(1000, Locale.KOREA);
            assertThat(result).isEqualTo("1,000");
        }
    }
    - "1,000"처럼 숫자 중간의 쉼표를 적용하려면 자바가 기본으로 제공하는 NumberFormat 객체를 사용하면 된다. 
    - parse() : 문자 -> 숫자 (Number 타입은 Integer, Long)과 같은 숫자 타입의 부모 클래스이다.
    - print() : 객체 -> 문자

### 스프링이 제공하는 기본 포맷터
    IDE에서 Formatter 인터페이스의 구현 클래스를 찾아보면 수 많은 날짜나 시간 관련 포맷터가 제공되는 것을 확인할 수 있다.
    그런데 포맷터는 기본 형식이 지정되어 있기 때문에, 객체의 각 필드마다 다른 형식으로 포맷을 지정하기는 어렵다.

    스프링은 이런 문제를 해결하기 위해 어노테이션 기반으로 형식을 지정해서 사용할 수 있는 매우 유용한 포맷터 두 가지를 기본으로 제공한다.

    - @NumberFormat : 숫자 관련 형식 지정 포맷터 사용
    - @DataTimeFormat : 날짜 관련 형식 지정 포맨터 사용

    ● FormatterController
    @Controller
    public class FormatterController {

        @GetMapping("/formatter/edit")
        public Strign formatterForm(Model model){
            Form form = new ~~
            form.setNumber(10000);
            form..setLocalDateTime(LocaleDateTime.now());

            model.addAttribute("form", form);
            return "formatter-form";
        }

        @PostMapping("/formatter/edit")
        public String formatterEdit(@ModelAttribute Form form) {
            return "formatter-view";
        }

        @Data
        static class Form {
            
            @NumberForm(pattern = "###,###")
            private Integer number;

            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
            private LocalDateTime localDateTime;
        }
    }

    ● templates/formatter-form.html
    <form th:object="${form}" th:method="post">
        number <input type="text" th:field="*{number}">
        localDateTime <input type="text" th:field="*{localDateTime}">

    ● templates/formatter-view.html
    <ul>
        <li>${form.number}: <span th:text="${form.number}">
        <li>${{form.number}}: <span th:text="${{form.number}}"> 
        <li>${form.localDateTime}: <span th:text="${form.localDateTime}" > 
        <li>${{form.localDateTime}}: <span th:text="${{form.localDateTime}}" >  

    ● 결과
    - ${form.number} : 10000
    - ${{form.number}} : 10,000
    - ${form.localDateTime} : 2024-06-27T00:00:00
    - ${{form.localeDateTime}} : 2024-06-27 00:00:00

    ● 정리
    메시지 컨버터(HttpMessageConverter)에는 컨버전 서비스가 적용되지 않는다.
    특히 객체를 JSON으로 변환할 때 메시지 컨버터를 사용하면서 이 부분을 많이 오해하는데, HttpMessageConverter의 역할은 HTTP 메시지 바디의 내용을 객체로 변환하거나 객체를 HTTP 메시지 바디에 입력하는 것이다.
    예를 들어서 JSON을 객체로 변환하는 메시지 컨버터는 내부에서 Jackson 같은 라이브러리를 사용한다. 객체를 JSON으로 변환한다면 그 결과는 이 라이브러리에 달린 것이다. 따라서 JSON 결과로 만들어지는 숫자나 날짜 포멧을 변경하고 싶으면 해당 라이브러리가 제공하는 설정을 통해서 포맷을 지정해야 한다. 결과적으로 이것은 컨버전 서비스와 전혀 관계가 없다.

    컨버전 서비스는 @RequestParam, @ModelAttribute, @PathVariable 뷰 템플릿 등에서 사용할 수 있따.