## 본문

### 로그인 요구사항
    - 홈 화면 - 로그인 전
        - 회원 가입
        - 로그인
    - 홈 화면 - 로그인 후        
        - 본인 이름(누구님 환영합니다.)
        - 상품 관리
        - 로그 아웃
    - 보안 요구사항
        - 로그인 사용자만 상품에 접근하고, 관리할 수 있다.
        - 로그인 하지 않은 사용자가 상품 관리에 접근하면 로그인 화면으로 이동
    - 회원 가입, 상품 관리

    ● Member
    @Data 
    public class Member {

        private Long id;

        @NotEmpty
        private String longId; // 로그인 Id
        @NotEmpty
        private String name;
        @NotEmpty
        private String password;
    }

    ● MemberRepository
    @Slf4j
    @Repository
    public class MemberRepository {

        private static Map<Long, Member> store = new HashMap<>(); // static 사용
        private static long sequence = 0L;

        public Member save(Member member) {
            member.setId(++sequence);
            store.put(member.getId(), member);
            return member;
        }

        public Member findById(Long id) {
            return store.get(id);
        }

        public Optional<Member> findByLoginId(String loginId) {
            return findAll().stream()
                .filter(m -> m.getLoginId().equals(loginId))
                .findFirst();
        }
 
        public List<Member> findAll() {
            return new ArrayList<>(store.values());
        }

        public void clearStore() {
            store.clear();
        }
    }

    ● MemberController
    @Controller
    @RequiredArgsConstructor
    @RequestMapping("/members")
    public class MemberController {

        private final MemberRepository memberRepository;

        @GetMapping("/add")
        public String addForm(@ModelAttribute("member") Member member) {
            return "members/addMemberForm";
        }

        @PostMapping("/add")
        public String save(@Valid @ModelAttribute Member member, BindingResult result) {
            if (result.hasErrors()) {
                return "members/addMemberForm";
            }

            memberRepository.save(member)
            return "redirect:/";
        }
    }

    ● templates/members/addMemberForm.html
    <form action="" th:action th:object="${member}" method="post">
        <div th:if="#{fields.hasGlobalErrors()}">
            <p class="field-error" th:each="err : ${#fields.globalErrors()}" th:text="${err}">전체 오류 메시지</>
        </>

        <div>
            <label for="loginId">로그인 ID</>
            <input type="text" id="loginId" 
                    th:field="*{loginId}" 
                    class="form-control" 
                    th:errorclass="field-error"> 
            <div class="field-error" th:errors="*{loginId}" /> 
        </div>

        <div>
            <label for="password">비밀번호</>
            <input type="password" id="password"
                    th:field="*{password}" 
                    class="form-control"
                    th:errorclass="field-error">
            <div class="field-error" th:errors="*{password}">
        </div>

        <div>
            <label for="name">이름</>
            <input type="text" id="name" 
                    th:field="*{name}" 
                    class="form-control"
                    th:errorclass="field-error">
            <div class="field-error" th:errors="*{cname}"/>
    </form> 
    
    ● LoginService
    @Service
    @RequiredArgsConstructor
    public class LoginService {
        private final MemberRepository memberRepository;                   

        /**
         * @return null이면 로그인 실패
         */
        public Member login(String loginId, String password) {
            return memberRepository.findByLongId(loginId)
                    .filter(m -> m.getPassword().equals(password))
                    .orElse(null);
        }
    }

    - 로그인의 핵심 비즈니스 로직은 회원을 조회한 다음에 파라미터로 넘어온 password와 비교해서 같으면 회원을 반환하고, 만약 password가 다르면 null을 반환한다.
  
    ● LoginForm
    @Data
    public class LoginForm {
        @NotEmpty
        private String loginId;
        @NotEmpty
        private String password;
    }

    ● LoginController
    @Slf4j
    @Controller
    @RequiredArgsConstructor
    public class LoginController {
        private final LoginService loginService

        @GetMapping("/login")
        public String loginForm(@ModelAttribute("loginForm") LoginForm form) {
            return "login/loginForm";
        }

        @PostMapping("/login")
        public String login(@Valid @ModelAttribute LoginForm form, BindingResult bindingResult) {
            if (bindingResult.hasErrors()) {
                return "login/loginForm";
            }
        }

        Member loginMember = loginService.login(form.getLoginId(),
        form.getPassword());

        if (loginMember == null) {
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "login/loginForm";

            // 로그인 성공 처리 TODO
            return "redirect:/";
        }
    }

    ● 로직 흐름
    1) 로그인 컨트롤러는 로그인 서비스를 호출해서 
    2) 로그인에 성공하면 홈 화면으로 이동하고, 
    3) 로그인에 실패하면 bindingResult.reject()를 사용해서 글로벌 오류(ObjectError)를 생성한다. 그리고 정보를 다시 입력하도록 로그인 폼을 뷰 템플릿으로 사용한다.
 
    ● 문제
    로그인이 되면 홈 화면에 고객 이름이 보여야 한다는 요구사항을 만족하지 못한다. 로그인의 상태를 유지하면서, 로그인에 성공한 사용자는 홈 화면에 접근시 고객의 이름이 보여주려면 어떻게 해야할까?


### 로그인 처리하기 - cookie 사용
    ● 로그인 상태 유지하기 - 쿠키
![cookie1](./login/login_img/cookie1.png) 

![cookie2](./login/login_img/cookie2.png) 

![cookie3](./login/login_img/cookie3.png) 

    - 영속 쿠키 : 만료 날짜를 입력하면 해당 날짜까지 유지
    - 세션 쿠키 : 만료 날짜를 생략하면 브라우저 종료시 까지만 유지
    
    - 쿠키에 시간 정보를 주지 않으면 세션 쿠키(브라우저 종료시 모두 종료)
        Cookie idCookie = new Cookie("memberId", String.valueOf(loginMember.getId()))'
        response.addCookie(idCookie);

        return "redirect:/";

        - 로그인에 성공하면 쿠키를 생성하고, HttpServletResponse에 담는다.
        쿠키 이름은 memberId이고, 값은 회원의 id를 담아둔다. 웹 브라우저는 종료 전까지 회원의 id를 서버에 계속 보내줄 것이다.

### 로그아웃 처리하기 - 쿠키 사용
    ● LoginController - logout 기능 추가
    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        expireCookie(response, "memberId");
        return "redirect:/";
    }

    private void expireCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    - 로그아웃도 응답 쿠키를 생성하는데 Max-Age=0를 확인할 수 있다. 해당 쿠키는 즉시 종료된다.
    
    ● 쿠키와 보안 문제
    - 쿠키 값은 임의로 변경할 수 있다.
      - 클라이언트가 쿠키를 강제로 변경하면 다른 사용자가 된다.
      - 실제 웹브라우저 개발자모드 -> Application -> Cookie 변경으로 확인
      - Cookie : memberId=1 -> Cookie : memberId=2 (다른 사용자의 이름이 보임)
    - 쿠키에 보관된 정보는 훔쳐갈 수 있다.
      - 만약 쿠키에 개인정보나, 신용카드 정보가 있다면?
      - 이 정보가 웹 브라우저에도 보관되고, 네트워크 요청마다 계속 클라이언트에서 서버로 전달된다.
      - 쿠키의 정보가 나의 로컬 PC에서 털릴 수도 있고, 네트워크 전송 구간에서 털릴 수도 있다.
    - 해커가 쿠키를 한번 훔쳐가면 평생 사용할 수 있다.
    
    ● 대안
    - 사용자 별로 예측 불가능한 임의의 토큰(랜덤 값)을 노출하고, 서버에서 토큰과 사용자 id를 매핑해서 인식한다. 그리고 서버에서 토큰을 관리한다.
    - 토큰은 해커가 임의의 값을 넣어도 찾을 수 없도록 예상 불가능 해야 한다.
    - 서버에서 해당 토큰의 만료시간을 짧게 유지한다.
    
### 로그인 처리하기 - session    
    앞서 쿠키에 중요한 정보를 보관하는 방법은 여러가지 보안 이슈가 있었다.
    이 문제를 해결하려면 결국 중요한 정보를 모두 서버에 저장해야 한다.
    클라이언트와 서버는 추정 불가능한 임의의 식별자 값으로 연결해야 한다.

    이렇게 서버에 중요한 정보를 보관하고 연결을 유지하는 방법을 세션이라 한다.

![session1](./login/login_img/session1.png)

![session2](./login/login_img/session2.png)
    - 세션 ID를 생성하는데, 추정 불가능해야 한다.
    - UUID는 추정이 불가능하다.
      - Cookie : mySessionId=zz0101xx-bab9-4b92-9b32-deadb280f4b21
    - 생성된 세션 ID와 세션에 보관할 값(memberA)을 서버의 세션 저장소에 보관한다.

![session3](./login/login_img/session3.png)
    - 클라이언트와 서버는 결국 쿠키로 연결이 되어야 한다.
    - 클라이언트는 쿠키 저장소에 mySessionId 쿠키를 보관한다.
    
    ● 중요
    - 여기서 중요한 포인트는 회원과 관련된 정보는 전혀 클라이언트에 전달하지 않는다는 것이다.
    - 오직 추정 불가능한 세션 ID만 쿠키를 통해 클라이언트에 전달한다.

![session4](./login/login_img/session4.png)
    - 클라이언트는 요청시 항상 mySessionId 쿠키를 전달한다.
    - 서버에서는 클라이언트가 전달한 mySessionId 쿠키 정보로 세션 저장소를 조회해서 로그인시 보관한 세션 정보를 사용한다.

    ● 정리
    세션을 사용해서 서버에서 중요한 정보를 관리하게 되었다. 덕분에 다음과 같은 보안 문제들을 해결할 수 있다.
        - 쿠키 값을 변조 가능 -> 예상 불가능한 복잡한 세션Id를 사용한다.
        - 쿠키에 보관하는 정보는 클라이언트 해킹시 털릴 가능성이 있다. 
            -> 세션Id가 털려도 여기에는 중요한 정보가 없다.
        - 쿠키 탈취 후 사용 -> 해커가 토큰을 털어가도 시간이 지나면 사용할 수 없도록 서버에서 세션의 만료시간을 짧게(30분)유지한다. 또는 해킹이 의심되는 경우 서버에서 해당 세션을 강제로 제거하면 된다.
        
### 로그인 처리하기 - session 직접 만들기
    - 세션 생성
      - sessionId 생성(임의의 추정 불가능한 랜덤 값)        
      - 세션 저장소에 sessionId와 보관할 값 저장
      - sessionId로 응답 쿠키를 생성해서 클라이언트에 전달
    - 세션 조회
      - 클라이언트가 요청한 sessionId 쿠키의 값으로, 세션 저장소에 보관한 값 조회
    - 세션 만료
      - 클라이언트가 요청한 sessionId 쿠키의 값으로, 세션 저장소에 보관한 sessionId와 값 제거
      
    ● SessionManager - 세션 관리
    @Component
    public class SessionManager {
        public static final String SESSION_COOKIE_NAME = "mySessionId";

        private Map<String, Object> sessionStore = new ConcurrentHashMap<>();

        /**
         * 세션 생성
         */
        public void createSession(Object value, HttpServletResponse response) {
            // 세션 id를 생성하고, 값을 세션에 저장
            String sessionId = UUID.randomUUID().toString();
            sessionStore.put(sessionId, value);

            // 쿠키 생성
            Cookie mySessionCookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
            response.addCookie(mySessionCookie);
        }

        /**
         *  세션 조회
         */
        public Object getSession(HttpServletRequest request) {
            Cookie sessionCookie = findCookie(request, SESSION_COOKIE_NAME);
            if (sessionCookie == null) {
                return null;
            }
            return sessionStore.get(sessionCookie.getValue());
        }
        /**
         *  세션 만료
         */
        public void expire(HttpServletRequest request) {
            Cookie sessionCookie = findCookie(request, SEESION_COOKIE_NAME);
            if (sessionCookie != null) {
                sessionStore.remove(sessionCookie.getValue());
            }
        }

        private Cookie findCookie(HttpServletRequest request, String cookieName) {
            if (request.getCookies() == null) {
                return null;
            }
            return Arrays.stream(request.getCookies()) 
                    .filter(cookie -> cookie.getName().equals(cookieName))
                    .findAny()
                    .orElse(null);
        }
    }

    - @Component : 스프링 빈으로 자동 등록한다.
    - ConcurrentHashMap : HashMap 은 동시 요청에 안전하지 않다. 동시 요청에 안전한 ConcurrentHashMap 를 사용했다