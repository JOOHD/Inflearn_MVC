## 본문

### 파일 업로드 소개
    일반적으로 사용하는 HTML Form을 통한 파일 업로드를 이해하려면 먼저 폼을 전송하는 두 가지 방식의 차이를 이해해야 한다.

    ● HTML 폼 전송 방식
    - application/x-www-form-urlencoded
    - multipart/form-data
   
![FORM_urlencoded](./springmvc/spring_img/FORM_urlencoded.png)

    - application/x-www-form-urlencoded 방식은 HTML 폼 데이터를 서버로 전송하는 가장 기본적인 방법이다. Form 태그에 별도의 enctype 옵션이 없으면 웹 브라우저는 요청 HTTP 메시지의 헤더에 다음 내용을 추가한다.
     
      ex) Content-Type : application/x-www-form-urlencoded
      
    - 그리고 폼에 입력한 전송할 항목을 HTTP Body에 문자로 username=kim&age=20와 같이 &로 구분해서 전송한다.  

    - 파일을 업로드 하려면 파일은 문자가 아니라 바이너리 데이터를 전송해야 한다. 문자를 전송하는 이 방식으로 파일을 전송하기는 어렵다. 그리고 또 한가지 문제가 더 있는데, 보통 폼을 전송할 때 파일만 전송하는 것이 아니라는 점이다.
    
      ex) - 이름, 나이, 첨부파일
      여기에서 이름과 나이도 전송해야 하고,, 첨부파일도 함께 전송해야 한다.
      문제는 이름과 나이는 문자로 전송하고, 첨부파일은 바이너리로 전송해야 한다는 점이다. 여기에서 문제가 발생한다. 문자와 바이너리를 동시에 전송해야 하는 상황이다.

      이 문제를 해결하기 위해 HTTP는 multipart/form-data라는 전송 방식을 제공한다.

![FORM_multipart](.springmvc/spring_img/FORM_multipart.png)

    - 이 방식을 사용하려면 Form 태그에 별도의 entype="multipart/form-data"를 지정해야 한다.
    multipart/form-data 방식은 다른 종류의 여러 파일과 폼의 내용 함께 전송할 수 있다. (그래서 이름이 mulipart)

    - 폼의 입력 결과로 생성된 HTTP 메시지를 보면 각각의 전송 항목이 구분이 되어있다. Content-Disposition 이라는 항목별 헤더가 추가되어 있고 여기에 부가 정보가 있다. 예제에서는 username, age, file1이 각각 분리되어 있고, 폼의 일반 데이터는 각 항목별로 문자가 전송되고, 파일의 경우 파일 이름과 Content-Type이 추가되고 바이너리 데이터가 전송된다.
    multipart/form-data는 이렇게 각각의 항목을 구분해서, 한번에 전송하는 것이다.

    - Part
    multipart/form-data는 application/x-www-form-urlencoded와 비교해서 매우 복잡하고 각각의 부분(part)로 나누어져 있따. 그렇다면 이렇게 복잡한 HTTP 메시지를 서버에서 어떻게 사용할 수 있을까?

### Servlet fileupload(1)
    @Slf4j
    @Controller
    @RequestMapping("/servlet/v1")
    public class ServletUploadControllerV1 {

        @GetMapping("/upload")
        public String newFile() {
            return "upload-form";
        }

        @PostMapping("/upload")
        public String saveFileV1(HttpServletRequest request) throws ServletException, IOException {

            String itemName = request.getParameter("itemName");

            Collection<Part> parts = request.getParts();

            return "upload-form";
        }
    }
    - request.getParts() : multipart/form-data 전송 방식에서 각각 나누어진 부분을 받아서 확인할 수 있다.
    
    ● upload-form.html
    <form th:action method="post" enctype="multipart/form-data">
        <ul>
            <li>상품명 <input type="text" name="itemName">
            <li>파일 <input type="file" name="file">
        </>
    </>

    - application.properties 옵션 추가    
      - logging.level.org.apache.coyote.http11=debug

    - 실행 : http://localhost:8080/serlvet/v1/upload
    - 결과 로그
    content-Type : multipart/form-data; boundary=----xxxx
    content-Disposition : form-data; name="itemName"

    Spring
    content-Disposition : form-data; name="file"; fileName="test.data"
    content-Type : application/octet-stream

    ● 업로드 사이즈 제한
    - spring.servlet.multipart.max-file-size=1MB
    - spring.serlvet.multipart.max-request-size=10MB

    - 큰 파일을 무제한 업로드하게 둘 수는 없으므로 업로드 사이즈를 제한할 수 있다. (사이즈를 넘으면 예외 SizeLimitExceededException 발생)

    - max-file-size : 파일 하나의 최대 사이즈, 기본 1MB
    - max-request-size : 멀티파트 요청 하나에 여러 파일을 업로드 할 수 있는데, 그 전체 합이다. 기본 10MB

    ● 참고
    - spring.servlet.multipart.enabled 옵션을 켜면 스프링의 DispatcherServlet에서 MultiPartResolver를 실행한다.
    MultipartResolver는 multipart 요청인 경우 서블릿 컨테이너가 전달하는 일반적인 HttpServletRequest를 MultipartHttpServletRequest로 변환해서 반환한다.
    MultipartHttpServletRequest는 HttpServletRequest의 자식 인터페이스이고, 멀티파트와 관련된 추가 기능을 제공한다.

    하지만 MultipartHttpServletRequest 보다 더 편리한 기능인 MultipartFile이라는 것을 사용할 것 이다.

### Servlet Fileupload(2)    
    - 먼저 파일을 업로드를 하려면 실제 파일이 저장되는 경로가 필요하다.
      - application.properties
        - file.dir=파일 업로드 경로 설정 
          ex) /Users/joo/study/file/ (설정할 때 마지막 '/'가 포함해야 한다.)

    ● ServletUploadControllerV2
    @Slf4j
    @Controller
    @RequestMapping("/servlet/v2)
    public class ServletUploadControllerV2 {

        @Value("${file.dir}")
        private String fileDir;

        @GetMapping("/upload")
        public String newFile() {
            return "upload-form";
        }

        @PostMapping("/upload")
        public String saveFileV1(HttpServletRequest request) throws
        ServletException, IOException {

            String itemName = request.getParameter("itemName");
            Collection<Part> parts = request.getParts();
            for (Part part : parts) {
                Collection<String> headerNames = part.getHeaderNames();
                for (String headerName : headerNames) {
                    log.info("header : ", headerName, part.getHeader(headerName));
                }

                // 편의 메서드
                // content-disposition; fileName
                log.info("submittedFileName={}", part.getSubmittedFileName());
                log.info("size={}", part.getSize()); // part body size

                // 데이터 읽기
                InputStream inputStream = part.getInputStream();
                String body = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);

                // 파일에 저장하기
                if (StringUitls.hasText(part.getSubmittedFileName())) {
                    Strign fulPath = fileDir + part.getSubmittedFileName();
                    part.write(fulPath);
                }
            }

            return "upload-form";
        }
    }
    질문
    1. MAP<?,?> 타입이 아닌 Key, value 형태를 가진 파라미터 ?
        inputStream, StandardCharset.UTF_8

    2. Collection<Part> 제네릭의 사용?
