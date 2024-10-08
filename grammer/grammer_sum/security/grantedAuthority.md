## 본문 

### grantedAuthority & Role

    Spring Scurity에서 GrantedAuthority와 일반적인 권한을 의미하는 단어로 Authority를 사용하면 단어가 비슷하여 혼란을 줄 수 있따.

### 1. CustomUserDetails class (이전 상황)

    ● 설명

    먼저 다룰 클래스인, CustomUserDetails class에 대해서 알아보자.
    Authentication확인을 위한 비교 대상 객체인 UserDetails의 권한 Authority가 다 EmptyList를 반환하게 했다.

    public class CustomUserDetails implements UserDetails, OAuth2User, Serializable {

        private static final long serialVersionUID = 1747288297321L;

        private Strign id; // DB에서 PK 값
        private String loginId;  // 로그인용 ID 값
        private String password; // 비밀번호
        private Stirng email;    // 이메일
        private boolean emailVerified // 이메일 인증 여부
        private boolean locked;  // 계정 잠김 여부
        private String nickname; // 닉네임
        private Collection<GrantedAuthority> authorities; // 권한 목록

        private User user;
        private Map<String, Object> attributes;

        // Social Login 
        public CustomUserDetails(String id, 
                                Collection<GrantedAuthority> authorities, 
                                User user, 
                                Map<String, Object> attributes) {
            //PrincipalOauth2UserService 참고
            this.id = id;
            this.authorities = authorities; // social 회원가입 여부를 나타내는 것으로 사용됨
            this.user=user;
            this.attributes = attributes;
        }


        // Non Social Login 
        public CustomUserDetails(Long authId,
                                 String userEmail,
                                 String userPw,
                                 boolean emailVerified,
                                 boolean locked) {
            this.id = String.valueOf(authId);
            this.email = userEmail;
            this.password = userPw;
            this.emailVerified = emailVerified;
            this.locked = !locked;                                
        }

        // 해당 유저의 권한 목록
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            if (user == null) { // non social
                return Collections.emptyList();
            } 
            else {
                return user.getAuthorities();
            }
        }
    }

    - authority 객체에 user ROLE 별로, 다른 ROLE에 해당하는 권한을 부여하는 코딩 구현을 해야 된다.

### 2. ROLE 설정

    @Getter 
    @RequiredArgsConstructor 
    public enum Role {

        USER ("ROLE_USER"),
        EMPLOYER ("ROLE_EMPLOYER, ROLE_USER"),
        ADMIN ("ROLE_ADMIN, ROLE_EMPLOYER, ROLE_USER"),
        TEMP_EMPLOYER ("ROLE_TEMPORARY_EMPLOYER"),
        OAUTH_USER ("ROLE_FIRST_JOIN_OAUTH_USER");

        private final String roles;

        poblic static String getIncludingRoles(String role) {
            return Role.valueOf(role).getRoles();
        }

        public static String addRole(Role role, String addRole) {
            String priorRoles = role.getRoles();
            priorRoles += "," + addRole;
            return priorRoles;
        }

        public static String addRole(String roles, Role role) {
            return roles + "," + role.getRoles();
        }
    }

    - getIncludingRoles : DB에 User Role 정보가 enum 인스턴스들의 이름 그대로 저장되어 있고, 해당 enum 클래스의 필드인 roles를 가져오기 위한 함수.
        - 문자열로 주어진 role을 Role(enum)에서 찾아서, 해당 역할에 연결된 권한을 반환하는 역할.

        ex)
            getIncludingRoles("AMIND")을 호출하면, 문자열 "ADMIN"이 Role.ADMIN으로 변환되고, Role.ADMIN의 getRoles() method가 호출되어,
            ROLE_ADMIN이 반환된다.

    - addRole : 권한들이 모여있는 String에 추가하고 싶은 Role의 roles들을 추가하여 해당 권한들을 String으로 반환하는 함수.

    ● Role.getValueOf() vs toString()

      - 두 method 모두 enum과 문자열 간의 변환에 사용되지만, 목적이 다르다.

        Role.getValueOf()

          - String -> enum 상수 변환
          - 문자열 값을 받아, 그와 일치하는 enum 상수를 반환한다.
          만갹 주어진 문자열이 enum에 존재하지 않으면, IllegalArgumentException이 발생한다.

            ex)
                Role role = Role.valueOf("ADMIN")

        toString()

          - enum -> String 문자열로 변환
          - enum 상수를 문자열로 변환하여 반환한다. 기본적으로 enum 상수의 이름(대문자)이 반환되지만, toString()을 오버라이드하면 원하는 문자열을 반환할 수 있다.

            ex)
                Role role = Role.ADMIN;
                role.toString()

### Role Hierarchy

    - 상위 권한을 가진 사용자는 하위 권한도 자동으로 갖게 된다.

    enum 클래스는 다수의 권한이 존재한다.
    예를들어 "USER"의 Role의 경우 딱 한가지 권한인 "ROLE_USER"가 부여,

    하지만 "EMPLOYER"의 Role의 경우, ROLE_USER & ROLE_EMPLOYER 권한을 부여하여, 유저의 권한도, 채용자의 권한도 가지게 된다.

    즉, 넣은 범위의 권한을 포함할 수 있게끔, 작은 권한들 부터 큰 권한들까지 모두를 포함하게 코드를 작성하였다.

    이렇게 해야, 권한별로 접근할 수 있는 resource를 정하기 편하다.

    왜냐하면 Security Config에서 채용자의 권한이 필요한 특정 엔드 포인트에 hasRole("EMPLOYER") 이렇게 설정하는데, 우리의 로직이 채용자의 경우에도, USER 권한을 행세할 수 있을 때, Role 별로 한가지의 권한만 부여하게 된다면, 여러 권한을 hasRole로 나열해야할 것이기 때문이다.
    
### 'ROLE' 은 필수적이다.   

    Employer의 권한을 가진 유저만 접근할 수 있는 엔드포인트가 GET/user/employer라고 하자. 그러면 SecurityConfig에 설정을 해야 된다.

        eX)
            httpSecurity.requestMatchers(HttpMethod.GET, "/users/employer").hasRole("EMPLOYER")

            - SecurityFilter는 'ROLE_EMPLOYER' 권한을 가지고 있는 유저인지 확인한다.

    SecurityFilterChain의 설정정보를 등록하는 AuthorizeHttpRequestConfigurer의 hasRole 관련 설정이다.

    'ROLE_'이라는 prefix가 자동으로 붙게 된다.

    즉, 엔드포인트마다 ROLE_USER, ROLE_EMPLOYER의 권한을 가지고 있는 
    Authentication 객체인지 확인한다는 것이다.

    요구사항에 맞추기 위해서, Role enum에 roles 필드들의 앞에 ROLE_ 접두어를 붙여주었다.

### 2. Service 로직에 어떻게 적용시킬 것 인가?

    회원가입을 하거나, 채용자의 인증을 거칠 때, enum 타입의 이름 그대로를 DB에 저장할 것이다.

    즉, 일반 유저는 'USER', 인증받은 채용자는 'EMPLOYER', 인증 대기중인 채용자는 'TEMP_EMPLOYER'이다.

    로그인 로직을 처리할 때, DB에 저장되어있는 ROLE을 가져와서, CustomUserDetails의 authority 필드에 해당 Role 인스턴스가 가지고 있는 roles들을 하나씩 추가해주면 된다.

    그 이후, jwt token을 만들고 클라이언트로 응답해야 한다.
    이 token에는 당연히 authority 관련 정보들이 들어 있어야, 나중에 클라이언트에서 정보를 요청할 때 jwt filter에서 해당 token을 호출하고, authoritication을 확인하고 해당 유저의 authority를 가져올 수 있을 것 이다.

    기존의 jwt token을 만들 때에는 username(여기 예시 프로젝트에서는 pk 값이다.)과 empty list authority 객체를 사용하여 jwt 토큰을 암호화 하였다. 

    하지만 이제는 실제 authentication 객체 내에 들어있는 authorities들을 불러와서 jwt 토큰을 만들어야 하는 것이 목표이다.

    ● 정리 (로그인, 소셜 로그인)

    - 로그인

      1. 로그인 성공 시, DB에 저장되어 있는 ROLE 'USER'를 가져와서 해당 Role이 가지고 있는 Roles를 authenticaiton의 authorities에 추가.
      
      2. 인증받은 Authentication 객체를 확인하고 Authentication Success Handler 호출이 된다.

      3. Token Provider 에서 access token 과 refresh 토큰을 Authentication 객체 정보를 파싱해서 만들고 cookie에 담아 client에 전송.

    - 소셜 로그인

      1. DB에서 해당 계정으로 등록도니 계정이 있는지 확인.

        1-1. 계정이 있으면, 
        회원가입 유저이므로, 일반 로그인과 동일하게 Authentication 객체의 authority에 DB에 저장된 ROLE 'USER' 정보를 가져오고 authorities에 해당 ROLE의 roles를 추가.   

        1-2. 계정이 없으면,
        회원가입 해야한는 유저이므로, DB에 해당 유저 정보를 ROLE 'USER'로 저장하고, authority에 최초 로그인을 위한 권한을 임시로 추가하고 DB에 저장된 ROLE의 roles들에 대한 authority도 추가.

      2. 인증받은 Authentication 객체를 확인하고 Authentication Success Handler 호출이 된다.

      3. Handler에서 authentication 객체의 authorities에 최초 로그인의 권한이 존재하는지 아닌지를 판단하고, redirect url을 분기함. 

### 3. CustomUserDetails (이후 로직)

    public class CustomUserDetails implements UserDetails, OAuth2User, Serializable {

        private static final long serialVersionUID = 1423123812412L;

        private String id;	// DB에서 PK 값
        private String loginId;		// 로그인용 ID 값
        private String password;	// 비밀번호
        private String email;	//이메일
        private boolean emailVerified;	//이메일 인증 여부
        private boolean locked;	//계정 잠김 여부
        private String nickname;	//닉네임
        private Collection<GrantedAuthority> authorities;	//권한 목록

        private User user;
        private Map<String, Object> attributes;

        // Social Login 용
        public CustomUserDetails(String id, 
                                 String roles,
                                 Map<String, Object attributes>) {

            //PrincipalOauth2UserService 참고
            this.id = id;
            this.authorities = createAuthorities(roles);
            this.attributes = attributes;
        }

        ● attributes : OAuth2 사용자 정보에서 사용자의 추가 정보를 포함하는 Map 형태로 사용된다. (이 속성은 일반적으로 OAuth2 인증을 통해 받은 사용자 정보를 저장하는 데 사용된다.)

            - OAuth2에서의 attributes
              - 사용자의 프로필 정보와 같은 여러 속성 값을 포함, claims와 유사.
              - 소셜 로그인에서 각 플랫폼에서 제공하는 사용자 정보를 담고있다.

            ex) 
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("email", "john.doe@example.com");
                attributes.put("name", "John Doe");
                attributes.put("picture", "https://example.com/john.jpg");  

        //Non Social + Employer 로그인 용도
        public CustomUserDetails(Long authId, 
                                 String roles, 
                                 String userEmail, 
                                 String userPw, 
                                 boolean emailVerified, 
                                 boolean locked) {

            this.id = String.valueOf(authId);
            this.authorities = createAuthorities(roles);
            this.email = userEmail;
            this.password = userPw;
            this.emailVerified = emailVerified;
            this.locked = !locked;
        }

        private Collection<GrantedAuthority> creatAuthorities(String roles) {
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            for(String role : roles.split(",")) {
                if (!StringUtils.hasText(role)) continue;
                authorities.add(new SimpleGrantedAuthority(role))
            }
            return authorities;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        /**
        * 해당 유저의 권한 목록
        */
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return this.authorities;
        }

        /**
        * 비밀번호
        */
        @Override
        public String getPassword() {
            return password;
        }


        /**
        * PK값
        */
        @Override
        public String getUsername() {
            return id;
        }

        /**
        * 계정 만료 여부
        * true : 만료 안됨
        * false : 만료
        * @return
        */
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        /**
        * 계정 잠김 여부
        * true : 잠기지 않음
        * false : 잠김
        * @return
        */
        @Override
        public boolean isAccountNonLocked() {
            return locked;
        }

        /**
        * 비밀번호 만료 여부
        * true : 만료 안됨
        * false : 만료
        * @return
        */
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }


        /**
        * 사용자 활성화 여부
        * ture : 활성화
        * false : 비활성화
        * @return
        */
        @Override
        public boolean isEnabled() {
            //이메일이 인증되어 있고 계정이 잠겨있지 않으면 true
            //상식과 조금 벗어나서, Customizing 하였음
            return (emailVerified && locked);

        }

        @Override
        public String getName() {
            String sub = attributes.get("sub").toString();
            return sub;
        }
    }

    - OAuth와 일반 로그인 인증 대상 객체를 담기 위하여 OAuth2User, UserDetails를 둘 다 구현함.

    - DB에 저장된 role을 꺼내고, 해당 role의 roles들을 파라미터로 받는 createAuthorities로 authority를 생성한다.

    - roles들은 쉼표로 나열되어 있으므로, 이를 parsing하고, simpleGrantedAuthority 함수로 String의 role들을 하나씩 추가한다.

### 3-1. 일반 로그인

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        NonSocialMemner member = nonSocialMemberRepository.findNonSocialMemberByEmail(userEmail). orElseThorw(() -> new UsernameNotFoundException("이 이메일과 매칭되는 유저가 존재하지 않습니다. : " + userEmail));
        // non social, social 섞여있기 때문에, user_id를 CustomUserDetail 의 id로 생성합니다. -> 토큰의 getName의 user_id가 들어갑니다.

        return new CustomUserDetails(member.getUserId(), 
                            Role.getIncludingRoles(member.getRole()), member.getUserEmail(), 
                            member.getUserPw(), true, false);
    }

### 3-2. 소셜 로그인

    @Slf4j
    @Service
    @RequiredArgsConstructor
    public class OAuthService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

        private final MemberRepository membrRepository;

        @Override
        @Transactional
        public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

            OAth2UserService delegate = new DefaultOAuth2UserService();  
            OAuth2User oAuth2User = delgate.loadUser(userRequest);
            String email = oAuth2User.getAttribute("email");
            String nickname = UUID.randomUUID().toString().subString(0,15);

            Optional<SocialMember> socialMember = memberRepository.findSocialMemberByEmail(email);

            if(socialMember.isEmpty()) {
                SocialMember savedSocialMember = SocialMember.createSocialMember(email, nickname);
                SaveMemberReposneDto savedResponse = memberRepository.save(savedSocialMember);
                String roles = Role.addRole(Role.getIncludingRoles(savedResponse.getRole()), Role.OAUTH_FIRST_JOIN);// 최초 회원가입을 위한 임시 role 추가

                return new CustomUserDetails(String.valueOf(savedResponse.getId()), roles, oAuth2User.getAttributes());
            }
            else {
                return new CustomUserDetails(String.valuOf(socialMember.get().getUserId()), Role.getIncludingRoles(socialMember.get().getRole()),oAuth2User.getAttributes());
            }
        }
    }

    - 최초 회원가입의 경우 socialMember.isEmpty(), CustomUserDetails에 회원가입을 처리하기 위한 임시 권한 부여.

### SuccessHandler

    @Component
    @RequiredArgsConstructor
    @Slf4j
    public class AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

        private final TokenProvider tokenProvider;
        @Value("${jwt.domain}") private String domain;
        @Value("${oauth-signup-uri}") private String signUpURI;
        @Value("${oauth-signin-uri}") private String signInURI;

        @Override
        public void onAuthenticationSuccess(HttpServletRequest request,
                                            HttpServletResponse response,
                                            Authentication authentication
                                            ) throws IOException {
            String accessToken = tokenProvider.createAccessToken(authentication);
            String refreshToken = tokenProvider.createRefreshToken(authentication);

            /**
              일반 로그인일 경우 생성되는 Authentication 객체를 상속한 UsernamePasswordAuthenticationToken 으로 response 생성
              바로 jwt 토큰 발급하여 response에 쿠키를 추가.
             */

            if (authentication instanceof UsernamePasswordAuthenticationToken) {
                makeSuccessResponseBody(response);
                resolveResponseCookieByOrigin(request, response, accessToken, refreshToken);
                return;
            }

            resolveResponseCookieByOrigin(request, response, accessToken, refreshToken);
            resposne.sendRedirect(UriByFirstJoinOrNot(authentication));
        }

        private static void makeSuccessResponseBody(HttpServletResponse response) throws IOException {
            String successResponse = convertSuccessObjectToString();
            response.setStatus(response.SC_OK);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.getWriter().write(successResponse);
        }

        private static String convertSuccessObjectToString() throws JsonProcessingException {
            ObjectMapper objectMapper = new ObjectMapper();
            IsSuccessResponseDto isSuccessResponseDto = new IsSuccessResposneDto(true, "로그인에 성공하였습니다.);
        }

            private void resolveResponseCookieByOrigin(HttpServletRequest request, HttpServletResponse response, String accessToken, String refreshToken) {
                if (request.getServerName().equals("localhost") || request.getServerName().equals("dev.inforum.me")) {
                    addCookie(accessToken, refreshToken, response, false);
                }
                else {
                    addCookie(accessToken, refreshToken, response, true);
                }
            }

            private void addCookie(String accessToken, String refreshToken, HttpServletResponse response,boolean isHttpOnly) {
                String accessCookieString = makeAccessCookieString(accessToken, isHttpOnly);
                String refreshCookieString = makeRefreshCookieString(refreshToken, isHttpOnly);
                response.setHeader("Set-Cookie", accessCookieString);
                response.addHeader("Set-Cookie", refreshCookieString);
            }

            private String makeAccessCookieString(String token,boolean isHttpOnly) {
                if(isHttpOnly){
                    return "accessToken=" + token + "; Path=/; Domain=" + domain + "; Max-Age=3600; SameSite=Lax; HttpOnly; Secure";
                }else{
                    return "accessToken=" + token + "; Path=/; Domain=" + domain + "; Max-Age=3600;";
                }
            }

            private String makeRefreshCookieString(String token,boolean isHttpOnly) {
                if(isHttpOnly){
                    return "refreshToken=" + token + "; Path=/; Domain=" + domain + "; Max-Age=864000; SameSite=Lax; HttpOnly; Secure";
                }else{
                    return "refreshToken=" + token + "; Path=/; Domain=" + domain + "; Max-Age=864000;";
                }
            }

            private String redirectUriByFirstJoinOrNot(Authentication authentication){
                OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
                Collection<? extends GrantedAuthority> authorities = oAuth2User.getAuthorities();
                if(authorities.stream().filter(o -> o.getAuthority().equals(Role.OAUTH_FIRST_JOIN)).findAny().isPresent()){
                    return UriComponentsBuilder.fromHttpUrl(signUpURI)
                            .path(authentication.getName())
                            .build().toString();

                }
                else{ // non social 로그인의 경우 회원가입한 유저이므로 else문으로 항상 들어감.
                    return UriComponentsBuilder.fromHttpUrl(signInURI)
                            .build().toString();
                }
            }
        }

### JWT의 구조

    - JWT는 세 부분으로 구성되어 있습니다:
    
        1. 헤더(header) 
        - JWT의 타입과 서명 알고리즘을 명시합니다. 일반적으로 다음과 같은 구조를 가집니다.

            ex)
                {
                    "alg": "HS256",
                    "typ": "JWT"
                }

        2. 페이로드(payload) 
        - 사용자 정보와 같은 클레임을 포함합니다. 예를 들어, 사용자 ID, 역할, 만료 시간 등이 포함될 수 있습니다.

            ex)
                {
                    "sub": "userId",
                    "role": "ROLE_USER",
                    "exp": 1633072800 // 만료 시간
                }

        3. 서명(signature) 
        - 헤더와 페이로드를 조합하여 secretKey로 서명한 부분입니다. 이 서명을 통해 JWT가 변조되지 않았는지 확인할 수 있습니다.       

### 1. SuccessHandler class 해석

    - Spring security에서 인증(authentication)에 성공했을 때, 실행되는 후속 처리를 담당하는 클래스이다. 주로 인증에 성공한 사용자에게 추가적인 응답을 전송하거나, 사용자를 특정 페이지로 redirect하는 등의 작업을 수행하는 데 사용.

    - SimpleUrlAuthenticationSuccessHandler
        - 인증에 성공하면 사용자를 지정된 URL로 redirect한다.

    - SavedRequestAwareAuthenticationSuccessHandler
        - 사용자가 인증 전에 접근을 시도했던 URL을 기억하고 있다가, 인증이 성공하면 그 url로 redirect한다.
        (로그인 페이지로 리다이렉트된 경우, 다시 로그인을 완료한 후 원래의 요청으로 돌아가게 한다.)
    
    ● @Value
    - property 값을 주입하는 역할, application.properties/yml 파일에 정의된 값을 가져와서 해당 필드에 주입

        ex) @Value("${jwt.domain}") private String domain;
            @Value("${oauth-signup-uri}") private String signUpURI;
            @Value("${oauth-signin-uri}") private String signInURI

        - 위 코드를 보면 properties/yml 파일에 jwt.domain/oauth-signup-uri/signin-uri 정의된 (key)값이 domain/signinURI/signupURI 변수에 주입된다.

        - jwt.domain = example.com 이면, String domain = example.com

### 1-1 Enum의 성격

    - java에서 열거형 타입을 나타낸다. 이는 고정된 상수 집합을 정의하는 것으로, 특정 값들이 미리 정해져 있는 경우 사용된다.

        ex)
            Role 이라는 Enum은 USER, ADMIN, EMPLOYER 등의 역할을 고정된 값으로 정의.

            이러한 고정된 상수 값들은 재사용성이 높으며, 실수로 다른 값이 들어가는 것을 방지.

### 1-2 Enum class의 상수 값을 String으로 변환하는 이유

    - Enum 값은 보통 코드나 개발 로직에서만 의미가 있다. 하지만, DB에 Enum을 저장하거나, 외부 API와 통신할 때는 문자열로 변환해야 하는 경우가 많다.
      
        ex)
            권한 관리에서 ROLE_USER 같은 값을 String으로 변환해서 DB에 저장하거나, JSON 응답으로 전달하는 경우가 일반적이다.

    - HTTP 응답이나 JSON 등의 포맷에서는 Enum을 직접 사용할 수 없다. 문자열로 변환해줘야 외부 시스템에서 해당 데이터를 처리할 수 있다.

        ex)
            Role.ADMIN 이라는 Enum 상수 간의 비교보다 유연한 조건 처리가 가능하다. 예를 들어, 권한(Role)이 여러 개일 때 이를 콤마로 연결된 문자열로 처리하여, 복수 권한을 비교하거나 추가하는 로직에서 유용하게 사용할 수 있다.

### 2. onAuthenticationSuccess method

    - Spring Security에서 인증이 성공적으로 완료되었을 때 호출되는 method이다. 

    - Authentication authenticaiton : 인증정보를 포함하는 객체로, 인증된 사용자에 대한 정보(username, auth,,,,)를 포함.

    - accessToken/refreshToken (JWT(JSON WEB TOKEN))

      ● accessToken
          - 역할 : 사용자의 인증 상태, 서버에 인증된 요청을 보낼 때(사용자가 프로필 정보를 조회할 떄, accessToken을 HTTP 헤더에 포함시켜 요청.)

          - 만료 시간 : 일반적으로 짧은 유효 시간을 가진다.(몇 분에서 몇 시간)

          - 단점 : 짧은 만료시간 때문에 자주 만료될 수 있다. 만료되면 새로운 토큰이 필요하므로, refreshToken을 사용해 다시 발급.

       ● refreshToken
          - 역할 : accessToken을 발급받기 위해 사용. 기존 accessToken 만료 시, 새로운 accessToken 발급

          - 만료 시간 : 오래 유효한 토큰(며칠에서 몇 주 or 몇 달)  
            (사용자가 1시간 사이트 활동 후, accessToken 만료 시, 자동으로 refreshToken을 사용, 새로운 accessToken 재발급)

          - 주의점  : 서버에서는 refreshToken을 HTTP-Only cookie에 저장해 클라이언트에서 접근할 수 없게 한다.

        ● 동작 흐름        
        1. 로그인 성공 -> 서버는 accessToken/refreshToken 클라이언트에 발급.
        2. accessToken 만료 전 -> 클라이언트는 유요한 accessToken을 사용하여 서버에 인증된 요청을 보냄.
        3. accessToken 만료 후 -> 클라이언트는 refreshToken을 사용하여 새로운 accessToken을 요청.
        4. 서버가 refreshToken을 확인한 후 새로운 accessToken을 발급.
        5. 만약 refreshToken도 만료되었거나 유효하지 않다면 다시 로그인.

    - 일반 로그인일 경우 로직

        - if (authentication instanceof UsernamePassword...) {}
          - 사용자가 일반 로그인(소셜 로그인이 아닌)

        - makeSuccessResponseBody(resposne);
          - 클라이언트에 로그인 성공 메시지 반환(JSON)

        - resolverRepseonCookieByOrigin(request, response, access/refreshToken);
          - 토큰들을 쿠키에 저장하여 클라이언트가 인증 정보를 유지.
    
        - response.sendRedirect(UriByFirstJoinOrNot(authentication));
          - 소셜 로그인의 경우 다른 URL로 redirect(UriByFirstJoinOrNot method를 통해 사용자가 처음 가입한 경우와 아닌 경우를 구분하여 적절한 URL로 이동시킨다.)

    ● 결론

    Authentication 클래스는 사용자가 성공적으로 로그인한 후 필요한 토큰을 생성하고, 이를 클라이언트에게 응답으로 보내거나 쿠키에 저장하여 인증 세션을 유지하는 역할을 한다. 

    refreshToken은 주로 accessToken이 만료되었을 때 새로운 엑세스 토큰을 발급받기 위해 사용된다.        

### 3. makeSuccessResponseBody method

    - 성공적인 로그인 후 클라이언트에게 JSON 형식의 응답을 보내는 역할.

        - convertSuccessObjectToString();
          - 로그인 성공 시 반환할 JSON 문자열을 생성
            (이 method는 로그인 성공에 대한 정보를 포함하는 DTO -> JSON 형식으로 변환.)

        - response.getWriter().write(successResponse);
          - successResponse에 저장된 JSON 문자열을 HTTP 응답 본문으로 작성, 클라이언트는 이 데이터를 수신하게 된다.  

    ● 결론

    makeSuccessResponseBody method는 성공적인 로그인 후 클라이언트에게 JSON 형식으로 응답을 보내기 위한 설정을 담당한다. HTTP 응답을 구성하는 기본적인 작업을 수행하며, 클라이언트와의 통신에서 중요한 역할.

### 4. convertSuccessObjectToString method

    - 로그인 성공 시 응답으로 보낼 JSON 문자열을 생성하는 역할.

        - ObjectMapper objectMapper = new ObjectMapper();
          - Jackson 라이브러리의 objectMapper 클래스를 사용하여 JSON과 Java 객체 간의 변환을 처리하는 인스턴스 생성.

        - IsSuccessResponseDto isSuccessResponseDto = new IsSuccessResponseDto(true, "로그인에 성공하였습니다.");:
          - 로그인 성공을 나타내는 DTO 생성.  

        - String successResponse = objectMapper.writeValueAsString(isSuccessResponseDto);
          - writeValueAsString() method를 호출하여 isSuccessResponseDto 객체를 JSON 형식의 문자열로 변환.

    ● 결론

    convertSuccessObjectToString method는 로그인 성공 시 클라이언트에게 전달할 정보를 JSON 형태로 변환하여 응답 본문을 구성하는 데 사용.
    DTO를 사용하여 필요한 데이터를 구조화 하고, 이를 JSON 형식으로 변환한다. 이 과정을 통해 클라이언트에게 명확한 성공 메시지를 전달.    

### 5. resolveResponseCookieByOrigin method  

    - 클라이언트의 요청이 어디에서 왔는지 확인, 그에 땨라 JWT를 쿠키에 추가하는 역할.

        - request.getServerName() 
            - 클라이언트의 요청에서 서버 이름(HOST)을 가져온다.

            ex) 
                if (request.getServerName().equals("localhost") || request.getServerName().equals("dev.inforum.me")):

        - addCookie(accessToken, refreshToken, response, false);
            - if문이 true일 경우, access/refreshToken 쿠키에 추가.

            - false는 쿠키가 HTTP-only 속성을 갖지 않도록 지정.
                (즉, 클라이언트 측 Javascript에서 쿠키에 접근 가능.)       

        - else { addCookie(accessToken, refreshToken, response, true); }        
            - if문이 false일 경우, 프로덕션 환경에서는 HTTP-only 속성을 갖도록 쿠키를 추가. (Javascript에 접근 불가.)

        ● 결론

        resolveResponseCookieByOrigin method는 요청의 출처에 따라 JWT 토큰을 쿠키에 추가할 때, 보안 요구 사항을 충족하도록 조정.

        로컬 개발 환경에서는 쿠키를 더 쉽게 접근할 수 있도록 설정, 프로덕션 환경에서는 보안을 강화하여 공격에 대한 취약성을 줄인다.   

### 6. addCookie method   

       - addCookie 메서드는 HTTP 응답에 액세스 토큰과 리프레시 토큰을 쿠키로 추가하는 역할을 합니다. 

        - String accessCookieString = makeAccessCookieString(accessToken, isHttpOnly);
        - String refreshCookieString = makeRefreshCookieString(refreshToken, isHttpOnly); 

            - 각각의 토큰에 대해 쿠키 문자열을 생성, 이 문자열은 쿠키의 속성과 값을 설정. 
            - make~~String 메서드를 호출하여 토큰과 보안 설정에 따라 쿠키 문자열을 반환.

        - response.setHeader("Set-Cookie", accessCookieString);  
            - accessToken cookie를 응답의 헤더에 추가. (이전 값을 덮어씌우고 새로운 값을 설정.)

        - response.addHeader("Set-Cookie", refreshCookieString);
            - refreshToken cookie를 응답의 헤더에 추가. (쿠키는 여러 개를 설정할 수 있으므로 addHeader 사용.)   

        ● 결론

        addCookie 메서드는 클라이언트에게 인증 정보를 안전하게 저장하도록 돕기 위해 액세스 토큰과 리프레시 토큰을 각각의 쿠키로 설정합니다.

### 7. makeAccessCookieString method

    - makeAccessCookieString method는 JWT를 포함하는 쿠키를 생성하는 역할을 한다. 쿠키의 속성과 특성을 설정하여 반환.
    (isHttpOnly 매개변수에 따라 쿠키의 보안 수준이 결정.)

        - String token : accessToken
        - boolean isHttpOnly : true - 쿠키는 클라이언트 측, Javascript에서 접근 불가.

        if (isHttpOnly) {
            return "accessToken=" + token + "; Path=/; Domain=" + domain + "; Max-Age=3600; SameSite=Lax; HttpOnly; Secure";
        }

            ex)
                ccessToken=<token>; // cookie name
                Path=/;             // cookie url
                Domain=<domain>;    // cookie domain(class domain instance)
                Max-Age=3600;       // cookie time
                SameSite=Lax;       // CSRF
                HttpOnly;           // cookie javascript access
                Secure              // HTTPS 

        else {
            // accessToken=<token>; Path=/; Domain=<domain>; Max-Age=3600;
            (Javascript에서 접근할 수 있게 된다.)
        }                

    ● 결론    

    makeAccessCookieString method는 JWT 액세스 토큰을 쿠키로 생성하는 데 사용되며, 보안 요구 사항에 따라 쿠키 속성이 다르게 설정됩니다. 이를 통해 사용자의 인증 상태를 안전하게 유지할 수 있으며, 보안이 중요한 프로덕션 환경에서는 HTTP-only 및 Secure 속성을 설정하여 CSRF 및 XSS 공격으로부터 보호합니다.


### 8. redirectUriByFirstJoinOrNot method    

    - method는 사용자가 소셜 로그인을 통해 최초 가입한 경우와 기존 사용자의 로그인 상황에 따라 리다이렉트할 URI를 결정하는 역할을 합니다.
  
        - OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal(); 
            - 인증 정보를 가져옴.

        - Collection<? extends GrantedAuthority> authorities = oAuth2User.getAuthorities(); 
            - 권한 목록을 가져옴.

        - if (authorities.stream().filter(o -> o.getAuthority().equals(Role.OAUTH_FIRST_JOIN)).findAny().isPresent());
            - 사용자의 권한 목록에서 Role.OAUTH_FIRST_JOIN 권한이 존재하는지를 확인합니다

            - return UriComponentsBuilder.fromHttpUrl(signUpURI) // 최초 가입자일 경우
                .path(authentication.getName()) // 사용자 이름을 URI에 추가
                .build().toString(); // 최종 URI를 문자열로 반환   

          else { // 기존 가입자인 경우
          return UriComponentsBuilder.fromHttpUrl(signInURI)
                .build().toString(); // 로그인 URI를 반환
    }    

     ● 결론

    redirectUriByFirstJoinOrNot method는 소셜 로그인 사용자의 가입 상태에 따라 적절한 페이지로 리다이렉트하는 로직을 제공합니존 사용자는다. 최초 가입자는 회원가입 페이지로, 기존 로그인 페이지로 안내되므로 사용자 경험을 향상시키는 데 중요한 역할을 합니다.

### TokenProvider

    @Sl4fj
    @Component
    public class TokenProvider imlements InitializingBean {

        private static final String AUTHORITIES_KEY = "auth";
        private final String secret;
        private final long accessTokenValidityInMilliseconds;
        private final long refreshTokenValidityInMillisenconds;

        private Key key;
        public TokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValidityInMilliseconds,
            @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInMilliseconds) {
        this.secret = secret;
        this.accessTokenValidityInMilliseconds = accessTokenValidityInMilliseconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInMilliseconds * 1000;
        
        }

        // 빈이 생성되고 주입을 받은 후에 secret 값을 Base64 Decode헤서 key 변수에 할당하기 위해
        @Override
        public void afterPropertiesSet() {
            // BASE64(인코딩 된 문자열)을 디코딩하여 바이트 배열로 변환.
            // BASE64 = secret key(JWT 토큰을 서명할 때 사용) 서명을 위한 형식으로 디코딩.
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            this.key = Keys.hmacShaKeyFor(keyBytes);
        }

        - JWT 토큰을 생성하거나 검증할 때 사용할 비밀키를 초기화하는 메서드
        - scret이라는 Base64로 인코딩도니 문자열에서 디코딩된 값을 사용하여 HMAC-SHA 알고리즘에 필요한 키 객체를 생성하는 과정.

        - 이 초기화 작업은 의존성 주입 후에 실행되며, JWT 서명과 검증에서 중요한 부분을 담당한다.

        public String createAccessToken(Authentication authentication) {
            String authorities = authoritication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            // 토큰의 expire 시간을 설정
            long now = (new Date()).getTime();
            Date validity = new Date(now + this.accessTokenValidityInMilliseconds);
            log.info("Login User Id = {}, authority = []", authentication.getName(), authorities);
            return Jwts.builder()
                .setSubject(authentication.getName())     // user_Id가 반환
                .claim(AUTHORITIES_KEY, authorities)      // 정보 저장
                .signWith(key, SignatureAlgoristhm.HS512) // 사용할 암호, 알고리즘, signature에 들어갈 secret값 세팅
                .setExpiration(validity) // set Expire Time 해당 옵션 안 넣으면 expire안함
                .compact();
        }

        public Strint createRefreshToken(Authentication authentication) {
            String authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            // 토큰의 expire 시간을 설정
            long now = (new Date()).getTime();
            Date validity = new Date(now + this.refreshTokenValidityInMilliseconds);
            return Jwts.builder()
                    .setSubject(authentication.getName())    // user_id가 반환됨
                    .claim(AUTHORITIES_KEY, authorities)     // 정보 저장
                    .signWith(key, SignatureAlgorithm.HS512) // 사용할 암호화 알고리즘과 , signature 에 들어갈 secret값 세팅
                    .setExpiration(validity)                 // set Expire Time 해당 옵션 안넣으면 expire안함
                    .compact();    
        }

        // 토큰으로 클레임을 만들고 이를 이용해 유저 객체를 만들어서 최종적으로 authentication 객체를 리턴
        public Authentication getAuthentication(String token) {
            Claims claims = Jwts
                    .parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            List<SimpleGrantedAuthority> authorities = Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            User principal = new User(claims.getSubject(), "", authorities);

            return new UsernamePasswordAuthenticationToken(prinicipal, token, authorities);
        }

        - claims

        // 토큰의 유효성 검증을 수행
        public boolean validateToken(String token) {
            try {
                Jwts.parserBilder().setSigningKey(key).parseClaimsJws(token);
                return true;
            } catch (io.jsonwebtoken.security.SecurityException |       MalformedJwtException e) {
                throw new BadTokenException("잘못된 JWT 서명입니다.", e);
            } catch (ExpiredJwtException e ){
                return false;
            } catch (UnsupportedJwtException e) {
                throw new BadTokenException("지원되지 않는 JWT 토큰입니다.", e);
            } catch (IllegalArgumentException e) {
                throw new BadTokenException("JWT 토큰이 잘못되었습니다.", e);
            }
        }
    }

    ● 결론

    - createAccessToken() 메서드로 authentication에 존재하는 authority 정보들을 쉼표로 파싱후, jwt builder로 jwt token 생성.

    - getAtuhentication(String token) 메서드를 이용하여, 발급된 token 정보를 쿠키 값으로 계속 사용하는 유저들의 authorities들을 파악.

    그후 UsernamePasswordAuthenticationToken의 authentication 구현 객체를 SecurityContextHolder 객체에 내부적으로 저장함 -> 사실상 세션임 언제든지 꺼내서 확인할 수 있다. (같은 Request, Response 사이클 내에서만)

### TEST

    CustomUserDetails, AuthenticationSuccessHnadler, TokenProvider 세 클래스의 가상의 구현을 통해 이들간의 상호 작용 흐름을 보자.

    이 예시는 사용자가 OAuth2 소셜 로그인을 수행한 후, CustomUserDetails를 통해 사용자 정보를 가져오고, SuccessHandler를 사용하여 JWT를 생성하고 쿠키로 설정하는 과정을 설명.

    1. CustomUserDetails 클래스

        - 사용자 정보를 담고 있는 클래스이다. 
        - UserDetails interface를 구현하여 Spring Security와 통합된다
       
        public class CustomUserDetails implements UserDetails {
        private String id;                      // 사용자 ID
        private String username;                // 사용자 이름
        private String roles;                   // 사용자 권한
        private Map<String, Object> attributes; // 추가 속성

        public CustomUserDetails(String id, String roles, Map<String, Object> attributes) {
            this.id = id;
            this.roles = roles;
            this.attributes = attributes;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of(() -> roles); // ROLE 형태로 변환
        }

        @Override
        public String getPassword() {
            return null; // 비밀번호는 사용하지 않음
        }

        @Override
        public String getUsername() {
            return username; // 사용자 이름 반환
        }

        @Override
        public boolean isAccountNonExpired() {
            return true; // 계정 만료 여부
        }

        @Override
        public boolean isAccountNonLocked() {
            return true; // 계정 잠금 여부
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true; // 자격 증명 만료 여부
        }

        @Override
        public boolean isEnabled() {
            return true; // 활성화 여부
        }

        public String getId() {
            return id;
        }
    }

    2. TokenProvider 클래스
    
        - JWT를 생성하고 검증하는 클래스이다.

        @Component
        public class TokenProvider {

            private final String secretKey = "your_secret_key"; // 비밀 키
            private final long accessTokenValidity = 3600000;   // 1시간

            public String createAccessToken(CustomUserDetails userDetails) {
                return Jwts.builder()
                        .setSubject(userDetails.getId()) // 사용자 ID를 주제로 설정
                        .setExpiration(new Date(System.currentTimeMillis() + accessTokenVlidity)) // 만료 시간 설정
                        .signWith(SignatureAlgorithm.HS256, secretKey) // 서명 알고리즘 설정
                        .compact();
            }

            public Claims validateToken(String token) { // 사용자의 id, 권한 추출.
                return Jwts.parser()
                        .setSigningKey(secretKey)   // JWT 서명을 검증하기 위해 비밀 키를 설정
                        .parseClaimsJws(token)      // 주어진 JWT 토큰을 파싱하여 클레임을 추출
                        .getBody();                 // 클레임 정보 반환
            }
        }

    3. AuthenticationSuccessHandler 클래스

        - 사용자가 성공적으로 인증된 후의 처리를 담당.

        @Component
        @RequiredArgsConstructor
        public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler AuthenticationSuccessHandler {

            private final TokenProvider tokenProvider;   

            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Auhthentication authentication) throws IOException {

                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal(); // 사용자 정보 가져오기       
                String accessToken = tokenProvider.createAccessToken(userDetails); // JWT 생성

                // JWT를 쿠키로 생성
                response.addCookie(createCookie("accessToken", accessToken, 3600));
                response.sendRedirect("/home"); // 성공 후 리다이렉트                            
            }   

            private Cookie createCookie(String name, String value, int maxAge) { // jwt -> cookie 형태로 생성하는 역할.
                Cookie cookie = new Cookie(name, value); // 이름과 값을 가진 쿠키 생성
                cookie.setMaxAge(maxAge); // 쿠키의 유효 기간 설정 (초 단위)
                cookie.setHttpOnly(true); // 클라이언트의 JavaScript에서 접근할 수 없도록 설정
                cookie.setSecure(true); // HTTPS에서만 쿠키가 전송되도록 설정
                cookie.setPath("/"); // 쿠키가 유효한 경로 설정 (모든 경로에서 사용 가능)
                return cookie; // 생성된 쿠키 반환
            }

            ● value : JWT의 서명이나 클레임을 설정할 때 사용되는 문자열이다.
                ex)
                    jwt의 claims에서 사용자의 id, name, email 등의 정보를 표현하는데 사용된다.
        }

    ● 흐름

    1. 사용자 인증 
    - 사용자가 OAuth2 소셜 로그인을 통해 인증 요청을 보냄

    2. CustomUserDetails 생성 
    - 성공적인 인증 후, CustomUserDetails 객체가 생성되어 사용자 정보가 담긴다.
        ex)
            CustomUserDetails userDetails = new CustomUserDetails("123", "ROLE_USER", attributes);

    3. JWT 생성 
    - AuthenticationSuccessHandler의 onAuthenticationSuccess 메서드에서 TokenProvicer를 통해 JWT를 생성.
        ex)
            String accessToken = tokenProvider.createAccessToken(userDetails);

    4. 쿠키에 JWT 저장
    - 생성된 JWT는 쿠키로 설정되어 클라이언트에 전송된다.
        ex)
            response.addCookie(createCookie("accessToken", accessToken, 3600));

    5. 클라이언트 요청
    - 클라이언트는 이후 요청 시 이 쿠키를 포함하여 서버에 요청을 보낸다.
    - 서버는 쿠키에서 JWT를 읽어 인증을 수행.        

