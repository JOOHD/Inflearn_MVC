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