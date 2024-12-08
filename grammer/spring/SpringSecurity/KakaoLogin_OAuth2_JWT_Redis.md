## 본문

## Kakao OAuth2 + JWT + Redis를 통한 인증 과정 구현 (1) - 카카오 로그인

    Spring Security + JWT + OAuth2 Client + Redis 를 사용하여 카카오 로그인을 프로젝트에 적용.

### step1. 인가 코드 받기

![kakao_authority](/grammer/img/kakao_authority.png)

    1. 카카오 인증 서버로 /oauth/authorizze URI 로 GET 요청을 보낸다.
    2. 카카오 인증 서버에서 클라이언트에게 카카오 로그인 페이지를 통해 로그인을 요청한다.
    3. 사용자는 카카오계정으로 로그인
    4. 카카오계정이 유요한 경우 카카오 인증 서버에서 클라이언트에게 동의 화면을 통해 사용자 정보 수집 동의를 요청한다.
    5. 클라이언트가 동의한 항목을 카카오 인증 서버에게 요청한다.
    6. 카카오 인증 서버에서는 302 Redirect URI 로 서비스 서버에게 인가 코드를 전달한다.

    - 여기서 Redirect URI 는 Kakao Developers - 내 애플리케이션 - 카카오 로그인에서 추가한 URI 로 설정된다.

### step2. 토큰 받기

![kakao_token](/grammer/img/kakao_token%20.png)

    이제 서비스 서버로 받은 인가 코드를 가지고 사용자 정보에 대한 토큰을 발급받을 수 잇따.

    2. 카카오 인증 서버가 토큰을 발듭해서 서비스 서버에 전달.
    - 여기서 헷갈릴 수 있다. 여기서의 토큰은 서비스 서버에서 사용할 access Token 이 아니다.

### step3. 사용자 로그인 처리

![kakao_userLogin](/grammer/img/kakao_userLogin.png)

    카카오 서버가 해주어야 할 일은 모두 끝났다. 이 부분은 서비스 서버에 직접 구현해야 하는 부분이다.

### 0. application.yml 설정

![kakao_applicationyml](/grammer/img/kakao_applicationyml.png)

    - Redirect URI 는 Kakao Developers 에 설정한 URI 와 동일하게 지정해준다.

    - Client ID, Cient Secret Kakao Developers 에서 발급받은 값으로 지정해준다.

    - authorization-grant-type 은 인가 코드 방식을 사용하므로 authorization_code 지정해주고, client-authentication-method 는 반드시 client_secret_post 로 지정해주어야 한다.

    ● gradle

    - implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
    - implementation 'org.springframework.boot:spring-boot-starter-security'

    인증 필터를 사용하기 위해 Security 를, 카카오 로그인을 사용하기 위해 OAuth2 를 추가한다.

### KakaoMemberDetails - Authentication 객체안에 사용자 정보를 담기 위함

    @RequiredArgsConstructor
    public class KakaoMemberDetails implements OAuth2User {
    
        private final String email;
        private final List<? extends GrantedAuthority> authorities;
        private final Map<String, Object> attributes;
    
        @Override
        public String getName() {
            return email;
        }
    
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }
    
        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }
    }

    - step3 의 로그인 성공 후처리 서비스를 구현하기에 앞서 후처리 서비스에서 필요한 정보들을 먼저 구현.

    - 이 클래스는 인증 객체인 Authentication 객체안에 사용자 정보를 담기 위한 클래스로, 인스턴스는 카카오 계정의 email과 사용자의 권한인 authority 가 컬렉션 형태로 필드로 가지게된다. 
    
    - gradle 에 추가한 OAuth2 Client 가 제공하는 OAuth2User interface 의 구현체로 작성해야 Authentication 객체 안에 담을 수 있기 때문에 OAuth2User 를 구현하여 getter 를 오버라이딩 하였다.

### KakaoUserInfo - 로그인 성공 시, kakaoMemberDetails 로 캐스팅 위한 정보

    public class KakaoUserInfo {

        public static final String KAKAO_ACOUNT = "kakao_account";
        public static final String EMAIL = "email";

        private Map<String, Object> attributes;

        public kakaoUserInfo(Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        public String getEmail() {

            ObjectMapper objectMapper = new ObjectMapper();
            TypeRefrence<Map<String, Object>> typeReferencer = new TypeReference<Map<String, Object>>() {};
        

            Object kakaoAccount = attributes.get(KAKAO_ACCOUNT);
            Map<String, Object> account = objectMapper.convertValue(kakaoAccount, typeReferencer);

            return (String) account.get(EMAIL);
        }
    }

    로그인에 성공하게 되면 후처리 서비스에서 받은 사용자 정보들을 kakaoMemberDetails 로 캐스팅하기위한 정보들이다.

### KakaoMemberDetailsService - 카카오 로그인 성공 후처리 서비스 객체

    step2 에서 토큰을 통해 사용자 정보를 받게 되면 이렇게 DefaultOAuth2UserService 의 OAuth2UserRequest 객체에 사용자 정보가 담기게 된다.

    public class DefaultOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> 
    {
        private Coverter<OAuth2UserRequest, RequestEntity<?>> requestEntityConverter = new OAuth2UserRequestEntityConverter();
    }

    후처리 서비스는 사용자가 비즈니스 로직에 맞게 커스텀 구현해야 한다.
    떄문에 이 DefaultOAuth2UserService 를 상속받아 요구사항에 맞게 커스터마이징 해보자.

    @Service
    @RequiredArgsConstructor
    public class KakaoMemberDetailsService extends DefaultOAuth2UserService {

        private static final String PREFIX = "낯선";

        private final MemberRepository memberRepository;

        @Transactional
        @Override
        public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

            OAuth2User oAuth2User = super.loadUser(userRequest);
            KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(oAuth2User.getAttributes());

            Member member = memberRepository.findByEmail(kakaoUserInfo.getEmail())
                .orElseGet(() ->
                        memberRepository.save(
                            Member.builder()
                                    .email(kakaoUserInfo.getEmail())
                                    .role(Role.USER)
                                    .nickName(PREFIX)
                                    .gender(Gender.NONE)
                                    .updateAgeCount(0)
                                    .updateGenderCount(0)
                                    .build()
                        )
                );

            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(member.getRole().name());

            return new KakaoMemberDetails(String.valueOf(member.getEmail()), Collections.singletonList(authority), 
            oAuth2User.getAttributes());
        }
    }

    DefaultOAuth2UserService 의 loadUser 메소드는 로그인 성공 후 동작하는 메소드로 실질적으로 사용자 정보를 어떻게 처리할 지 작성해야 하는 구현부이다.

        OAuth2User oAuth2User = super.loadUser(userRequest);

    1. 먼저 request 로부터 사용자 정보가 담긴 객체를 꺼내온다.
 
        KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(oAuth2User.getAttributes());

    2. 사용자 정보가 담긴 객체에서 사용자 정보를 꺼내서 KakaoUserInfo 에 담는다.

### 왜 KakaoUserInfo 가 필요한가?

    - 사용자 정보가 담긴 OAuth2User 객체의 attributes 를 출력해보면, 로그인 유저 정보가 아래와 같이 Map<String, Object> 형태로 저장된 것을 볼 수 있다.

    - 그 중에서도 DB에 저장하기 위해 필요한 이메일 정보는 Map<String(key:kakao_account), Map<String, Object>> 형태로 저장되어 있기 때문에 이 정보들을 편하게 parsing 해오기 위해 KakaoUserInro 클래스를 만들어 준다.

    oauth2User.getAttributes() = {
                setPrivacyInfo=true, 
                id=3292176436,
                connected_at=2024-01-21T09:45:17Z,
                properties={nickname=김동웅}, 
                kakao_account = {
                        profile_nickname_needs_agreement=false,        
                        profile={nickname=김동웅}, 
                        has_email=true, 
                        email_needs_agreement=false, 
                        is_email_valid=true, 
                        is_email_verified=true, 
                        email=kdo0422@nate.com
                    }
    }   

    KakaoUserInfo를 아래처럼 oauth2User.getAttributes() 로 생성하면,

    KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(oAuth2User.getAttributes());

    이제 kakaoUserInfo.getEmail() 을 통해,

    1. Map<String, Map<String, String>> attributes 에 담긴 kakao_account key 의 value 를 가져와서,

    2. kakao_account key 의 value 인 Map<String, String> 에서 email 을 가져오도록 함으로써, 실제 DB에 INSERT 할 회원 정보 중 하나인 email 을 편하게 가져올 수 있다.

    본론으로 돌아와 후처리 메소드인 loadUser() 을 보면,

    - 사용자 정보를 통해 kakaoUserInfo 를 생성해주고, kakaoUserInfo 로 회원 리포지토리에 해당 회원이 존재하는 지 조회하여 없으면 생성 후, 리턴하고 있으면 리턴하는 방식으로 구현

    - repository 에 회원이 없는 경우에는 email 필드만 카카오 로그인 계정으로 등록해주고, 나머지 필드들은 요구사하엥 맞게 기본적인 정보들을 Builder 로 생성하여 repository 에 저장해준다.

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(member.getRole().name());

    3. OAuth2User interface 를 구현한 kakaoMemberDetails 에 회원의 권한 정보를 담기 위해 저장된 회원으로부터 권한 정보를 꺼내오고, SimpleGrantedAuthority 객체를 만들어 주었다.

        return new kakaoMemberDetails(String.valueOf(member.getEmail()),
        Collections.singletonList(authority),
        oAuth2User.getAttributes());

    4. KakaoMemberDetails 는 권한 정보를 Collection 형태의 필드로 갖기 때문에 3에서 만든 권한을 형태에 맞게 변경하여 KakakoMemberDetails 를 생성 후 리턴한다.    

        ● 어디에 return 되는가?

        -  여기서 return 된 UserDetails 객체는 사용자 인증 정보를 나타내기 위해 Authentication 객체에 담겨지고, 이 Authentication 객체는 사용자의 인증 상태를 나타내며, SecurityContext 에 저장된다.

    마지막으로 구현한 후처리 서비스 클래스를 SecurityConfig 에 다음과 같이 등록해주면 끝이다.

    .oauth2Login(oAuth2Login -> {
        oAuth2Login.userInfoEndPoint(userInfoEndpointConfig ->
                userInfoEndPointConfig.userService(kakaoMemberDetailsService));
    });

### 클래스 간 관계 

    KakaoMemberDetailsService
        ㄴ DefaultOAuth2UserService
            ㄴ loadUser() 호출
                ㄴ KakaoUserInfo 생성 (attributes parsing)
                    ㄴ getEmail()
                ㄴ MemberRepository 로, DB 조회/저장
                    ㄴ Member Entity 셍성 (필요 시)
                ㄴ KakaoMemberDetails 반환 (사용자 정보 포함)

### 전체 흐름

    1. OAuth2 로그인 요청
        - 사용자가 카카오 OAuth2 로 로그인하면, kakaoMemberDetailsService 의 loadUser() 가 호출된다.

    2. 사용자 정보 가져오기
        - DefaultOAuth2UserService 의 super.loadUser() 가 실행되어 카카오 API에서 사용자 속성을 가져온다.
      
    3. 속성 파싱
        - KakaoUserInfo 가 속성을 분석하여 이메일과 같은 필요한 정보를 추출.

    4. DB 조회 및 저장
        - Email 기준으로 MemberRepository 를 통해 사용자를 조회한다.
        사용자가 없으면 새로 생성하여 저장한다.

    5. 사용자 정보 반환
        - kakaoMemberDetails 객체를 생성하여 사용자 이메일, 권한, 속성을 반환한다. 이 객체는 스프링 시큐리티의 인증 시스템에서 사용된다.


## Kakao OAuth2 + JWT + Redis를 통한 인증 과정 구현 (2) - JWT 적용

    최초 로그인의 경우 DB에 회원을 저장하고, Details 객체를 만들어 Authentication 객체에 담고, SecurityContext 에 Authentication 객체를 보관하도록 설정하였고,

    최초 로그인이 아닌 경우에는 DB에서 회원을 가져와 Details 객체를 만들어 Authentication 객체에 담고, SecurityContext 에 Authentication 객체를 보관하도록 설정하였다.

    - 이번 목적은 JWT를 어떻게 적용했는지를 중점으로 보자.

### 0. application.yml 설정     

    jwt: 
        secret_key : ${jwt.secret_key}
        access-token-validity-in-seconds : 30000 (배포 시, 30분 -> 1800 설정)
        refresh-token-validity-in-seconds : 86400 (배포 시, 1일 -> 86400 설정) 

    시그니처에 사용할 secret_key & accessToken expired & refreshToken expired 을 yml에 지정해준다. 

    secret_key 값은 중요한 정보이니만큼 github 로 형성관리하지 않고, 별도로 다른 파일에 설정해주고 해당 파일은 gitignore 에 등록해준다.

    ● build.gradle 에 의존성 추가

    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'

### 1. TokenProvider - 사용자 정보로 JWT 토큰 생성  

    public class TokenProvider {

        private static final String AUTH_KEY = "AUTHORITY";
        private static final String AUTH_EMAIL = "EMAIL";

        private final String secretKey;
        private final long accessTokenValidityMilliSeconds;
        private finla long refreshTokenValidityMilliSeconds;

        private Key secretKey;

        public TokenProvider(@Value("${jwt.secret_key"}) String secretKey,           
                             @Value("${jwt.access-token-validity-in-seconds}") long accessTokenValiditySeconds,
                             @Value("${jwt.refresh-token-validity-in-seconds}") long refreshTokenValiditySeconds) {
            
            this.secretKey = secretKey;
            this.accessTokenValidityMilliSeconds = accessTokenValiditySeconds * 1000;
            this.refreshTokenValidityMilliSeconds = refreshTokenValiditySeconds * 1000;
        }
    
        @PostConstruct
        public void initKey() {
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            this.secretkey = Keys.hmacShaKeyFor(keyBytes);
        }
    }

    각 토큰에 대한 만료시간과 secretKey 같은 yml 설정 정보를 토대로 생성자 주입하고, 생성자 주입 후 JWT 생성에 사용될 Key는 initKey() 메소드로 secretKey 를 decode 하여 Key에 주입했다.

    // access, refresh Token 생성
    public TokenDto createToken(String email, String role) {

        long now = (new Date()).getTime();

        Date accessValidity = new Date(now + this.accessTokenValidityMilliSeconds);
        Date refreshValidity = new Date(now + this.refreshTokenValidityMilliSeconds);

        String accessToken = Jwts.builder()
                .addClaims(Map.of(AUTH_EMAIL, email))
                .addClaims(Map.of(AUTH_KEY, role))
                .signWith(secretkey, SignatureAlgorithm.HS256)
                .setExpiration(accessValidity)
                .compact();
 
        String refreshToken = Jwts.builder()
                .addClaims(Map.of(AUTH_EMAIL, email))
                .addClaims(Map.of(AUTH_KEY, role))
                .signWith(secretkey, SignatureAlgorithm.HS256)
                .setExpiration(refreshValidity)
                .compact();
 
        return TokenDto.of(accessToken, refreshToken);
    }

    // token 이 유요한 지 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;        
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            return false;
        } catch (UnspportedJwtException e) {
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // token 이 만료되었는지 검사
    public boolean validateExpire(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
        }
    }

    // token으로부터 Authentication 객체를 만들어 리턴하는 메소드
    public Authentication getAuthentication(String token) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJwts(token)
                .getBody();

        List<String> authorities = Arrays.asList(claims.get(AUTH_KEY)
                .toString()
                .split(","));
 
        List<? extends GrantedAuthority> simpleGrantedAuthorities = authorities.stream()
                .map(auth -> new SimpleGrantedAuthority(auth))
                .collect(Collectors.toList());
 
        KakaoMemberDetails principal = new KakaoMemberDetails(
                (String) claims.get(AUTH_EMAIL),
                simpleGrantedAuthorities, Map.of());
 
        return new UsernamePasswordAuthenticationToken(principal, token, simpleGrantedAuthorities);
    }        

    TokenProvider 에서 토큰 생성 뿐만 아니라 토큰 검증 관련 메소드와 토큰으로부터 Authentication 객체를 리턴하는 기능들을 추가 구현했다.

### 2. OAuth2SuccessHandler

    @Component
    @RequiredArgsConstructor
    public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler
    {
        private static final String REDIRECT_URI = "http://localhost:8080/api/sign/login/kakao?accessToken=%s&refreshToken=%s";

        private final TokenProvider tokenProvider;
        private final MemberRepository memberRepository;

        @Transactional
        @Override
        public void onAuthenticatoinSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException 
        {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            KakaoUserInfo kakaoUserInfo = new KakaoUserInfo(oAuth2User.getAttributes());

            Member member = memberRepsitory.findByEmail(kakaoUserInfo.getEmail()).orElseThrow(MemberNotFoundException::new);

            TokenDto tokenDto = tokenProvider.createToken(member.getEmail(), member.getRole().name());

            String redirectURI = String.format(REDIRECT_URI, tokenDto.getAccessToken(), tokenDto.getRefreshToken());

            getRedirectStrategy().sendRedirect(request, response, redirectURI);
        }
    }

### compact() vs build()

    compact

    역할 : JWT 토큰을 문자열로 압축하고 직렬화하는 메서드이다.

    동작 : Jwts.builder() 를 통해 구성한 JWT의 Header, Payload, Signature 을 조합하여 최종적으로 token을 생성한다.

    String jwtToken = Jwts.builder()
        .setSubject("user")
        .setExpiration(expirationDate)
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact(); // 최종 JWT 문자열 생성

    build

    역할 : 객체 생성을 최종화하는 메서드이다.

    동작 : 객체의 구성 작업이 완료된 뒤, 최종적으로 객체를 반환.

    Object obj = Object.builder()
        .field("value1")
        .field("value2")    
        .build(); // 최종 객체 생성

### token ValidateExpire 메소드의 로직 중, build() 중간에 들어가는 이유

    Jwts.parserBuilder().build() 는 JWT 파싱 설정을 구성하기 위한 빌더 객체를 반환한다. 
    build() 는 그 빌더 객체를 JWT 파서로 최종 변환하는 역할을 하며, 이후 토큰 검증을 위해 마지막이 아닌, 중간에 온다.

    1. compact() - JWT 문자열 생성
    ex)
        public static void main(String[] args) {
            String secretKey = "mySecretKey"; // 토큰 서명에 사용할 키
            long expirationTime = 1000 * 60 * 60; // 1시간 유효기간

            // 토큰 생성
            String jwtToken = Jwts.builder()
                    .setSubject("user123") // 사용자 식별자
                    .setIssuedAt(new Date()) // 발급 시간
                    .setExpiration(new Date(System.currentTimeMillis() + expirationTime)) // 만료 시간
                    .signWith(SignatureAlgorithm.HS256, secretKey) // 서명 알고리즘 및 키
                    .compact(); // 최종 JWT 문자열 반환

            System.out.println("Generated JWT Token: " + jwtToken);
        }

        compact(jwtToken) :eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMTIzIiwia....

    2. build() - JWT 파서 생성
    ex)
        public static void main(String[] args) {
            String secretKey = "mySecretKey"; // 서명 검증에 사용할 키
            String jwtToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyMTIzIiwiaWF0IjoxNjkxNjgyNjAwLCJleHAiOjE2OTE2ODYyMDB9.S3uDs4YRgWn5MZwoEZ-5F1ey_GtpsXN_FmjLdjMsbM0";

            try {
                // 토큰 검증 및 파싱
                Claims claims = Jwts.parserBuilder()
                        .setSigningKey(secretKey) // 서명 키 설정
                        .build()                  // JWT 파서 객체 생성
                        .parseClaimsJws(jwtToken) // 토큰 검증 및 파싱
                        .getBody();               // 페이로드 추출

                // 검증 후 페이로드 데이터 출력
                System.out.println("Subject: " + claims.getSubject());
                System.out.println("Expiration: " + claims.getExpiration());
            } catch (Exception e) {
                System.out.println("Invalid JWT Token: " + e.getMessage());
            }
        }    