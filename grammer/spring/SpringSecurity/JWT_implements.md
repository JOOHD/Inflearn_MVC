## 본문 

### 1. JWT 구현 - 프로젝트 설정

    이 전에 Security에 Jwt를 도입하여 Authentication & authorization 방식은 인증 아키텍쳐를 따라 어떤 흐름으로 로직이 흘러가는지 알아보았다.
    이번에는 이 전과 조금 다른 방식으로 JWT를 Security에 도입해보자.

    ● 프로젝트 설정

    - Spring boot : 2.7.12 ver
    - dependencies에 추가해주고 Generate
        - Spring Web
        - Spring Security
        - Validation
        - Spring Data Jpa
        - Lombok
        - H2 Database

    ● Controller

    @RestController
    @RequestMapping("/api")
    public class HelloController {
    
        @GetMapping("/hello")
        public ResponseEntity<String> hello() {
            return ResponseEntity.ok("hello");
        }
    }    

![jwt_login_page](/grammer/img/jwt_login_page.png) 

    - Security가 자동으로 로그인 페이지를 띄워준다.
    - 로그에서 인증키를 찾아 ID에 user, Pw에 인증키를 입려하면 성공.

### 2. JWT 구현 - Security 설정, 데이터 삽입

    @Configuration
    @EnableWebSecurity  // 1
    public class SecurityConfig {

        @Bean // 2
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http .authorizeRequests() // 3
                 .antMatches("/api/hello").permitAll() // 4
                 .antMatchers("/api/authenticate").permitAll() // 5
                 .antMatchers("/api/signup").permitAll() // 6
                 .anyRequest().authenticated(); // 7
        
            return http.build();
        }
    }

    1. SecurityConfig 클래스를 @Configuration으로 설정파일로 등록하고, @EnableWebSecurity으로 기본적인 보안 구성을 활성화 해준다.

    2. Security에서 사용하는 필터를 customizing하기 위해 빈으로 등록한다.

    3. 아래 체인으로 연결된 메소드에 한해, 접근 권한을 설정한다.

    4. "/api/hello/" 는 접근 권한 설정 x

    5. "/api/authenticate" 는 접근 권한 설저어 x

    6. "/api/signup" 은 접근 권한 설정 x

    7. 그 외의 요청에 한해서는 Authentication 인증 필요.

    - /api/hello와, 로그인, 회원가입 한해 인증 없이 접근 가능하도록, 그 외의 API에 대해서는 인증을 해야만 접근할 수 있게 설정.

    ● Entity class

    @Entity
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public class Member {
    
        @Id
        @Column(name = "member_id")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
    
        private String username;
    
        private String password;
    
        private String nickname;
    
        private boolean activated;
    
        @ManyToMany
        @JoinTable(
                name = "member_authority",
                joinColumns = {@JoinColumn(name = "member_id", referencedColumnName = "member_id")},
                inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "authority_name")})
        private Set<Authority> authorities;
    }

    ● Authority class

    @Entity
    @Table(name = "authority")
    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public class Authority {
    
        @Id
        @Column(name = "authority_name", length = 50)
        private String authorityName;
    }

    - Member (*) -> Authority (*) : 다대다 관계 매핑 by JoinTable

    ● DataSource 설정

    server:
      port: 8080
      servlet:
        context-path: /
        encoding:
            charset: UTF-8
            enabled: true
            force: true

    spring:
    datasource:
        driver-class-name: org.h2.Driver
        url: jdbc:h2:tcp://localhost/~/jwtserver
        username: sa
        password:
    
    jpa:
        database-platform: org.hibernate.dialect.H2Dialect
        hibernate:
    #      hbm2ddl-auto: create #create update none
        ddl-auto: create-drop
        properties:
        hibernate:
            show_sql: true

    ● JPA 설정

    insert into member (username, password, nickname, activated) values ('admin', '$2a$08$lDnHPz7eUkSi6ao14Twuau08mzhWrL4kyZGGU5xfiGALO/Vxd5DOi', 'admin', 1);
    insert into member (username, password, nickname, activated) values ('user', '$2a$08$UkVvwpULis18S19S5pZFn.YHPZt3oaqHZnDwqbCW9pft6uFtkXKDC', 'user', 1);  

    insert into authority (authority_name) values ('ROLE_USER');
    insert into authority (authority_name) values ('ROLE_ADMIN');      

    insert into member_authority (member_id, authority_name) values (1, 'ROLE_USER');
    insert into member_authority (member_id, authority_name) values (1, 'ROLE_ADMIN');
    insert into member_authority (member_id, authority_name) values (2, 'ROLE_USER');

    - 현재 설정이 ddl-auto: create-drop으로 되어 있으므로 애플리케이션을 실행할 때마다 데이터를 모두 지우게 되는데, 편의를 위해 데이터를 생성해두고 실행될 때마다 추가할 수 있도록 구현.

![jwt_db](/grammer/img/jwt_db.png)  

### 3. JWT 구현 - TokenProvider 

    이 전에는 UsernamePasswordAuthenticationFilter의 successfulAuthentication() 메소드에서 Token 정보를 지정하고 JWT를 만들어 주었지만, application.yml 파일에 JWT 관련 설정을 추가하여 JWT 생성 정보를 지정해줄 수 있다.

    ● application.yml

    jwt:
        header: Authorization
        secret: zklspefnlskne // secret key 
        token-validty-in-seconds: 86400 // 만료 시간(초)

    ● Dependency (build.gradle)       

    implementation group: 'io.jsonwebtoken', name: 'jjwt-api', version: '0.11.5'
    runtimeOnly group: 'io.jsonwebtoken', name: 'jjwt-impl', version: '0.11.5'
    runtimeOnly group: 'io.jsonwebtoken', name: 'jjwt-jackson', version: '0.11.5'
    
    ● TokenProvider

    토큰의 생성과 검증을 담당하는 클래스를 별도로 만들어 사용할 것이다.

    @Component
    public class TokenProvider implements InitializingBean {

        private final Logger log = LoggerFactory.getLogger(TokenProvider.class);

        private static final String AUTHORITIES_KEY = "auth";
        private final String secret;
        private final long tokenValidityInMilliseconds;
        private Key key;

        public TokenProvider(@Value("${jwt.secret}") String secret,
                             @Value("${jwt.token-validity-in-seconds}") long tokenValidityInseconds) {
            this.secret = secret;
            this.tokenValidityInMilliseconds = tokenValidityInseconds * 1000;
        } 
        - 생성자 및 초기화.
        - yml에 설정해둔 정보를 기반(secretKey, validity)으로 TokenProvider를 생성.

        @Override
        public void afterPropertiesSet() {
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            this.key = Keys.hmacShaKeyFor(keyBytes);
        }
        - InitializingBean interface의 메소드로 BeanFacotry에 의해 모든 property 가 설정되고 난 뒤, 실행되는 메소드이다. 생성자가 실행된 이후 시크릿 키를 Base64로 인코딩 후 HMAC-SHA 알고리즘으로 암호화한다.

        // 토큰 생성 : Authentication 객체의 property를 기반으로 Token을 생성.
        public String createToken(Authentication authentication) {
            // 권한 정보 얻기
            String authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            // 토큰 만료시간 
            long now = (new Date()).getTime();
            Date validity = new Date(now + this.tokenValidityInMilliseconds); 

            return Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();       
        }

        ● 동작 방식

        1. Authentication.getAuthorities() 메소드를 통해 UserDetails의 authorities를 반환하여 권한 정보를 받아낸다.
        2. 현재 시간을 받아낸다. (=now)
        3. 권한 정보와 현재 시간을 토대로 Jwt Builder를 통해 토큰 생성.

        // 인증정보 생성 (토큰으로부터 Authentication 객체 추출)
        public Authentication getAuthentication(String token) {
            Claims claims = Jwts
                    .parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
    
            Collection<? extends GrantedAuthority> authorities =
                    Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
    
            User principal = new User(claims.getSubject(), "", authorities);
    
            return new UsernamePasswordAuthenticationToken(principal, token, authorities);
        }
        - Jwt-Parse를 이용하여 입력받은 token을 parsing하여 claims(payload + signature + header)라는 객체를 얻을 수 있따.
        - 이 claims 객체를 통해 권한정보를 추출한 후, 권한정보와 claims의 subject를 토대로 User를 하나 생성하여 UsernamePasswordAuthenticationToken로 만들어 리턴한다.
      
        ● 구성 

        claims : (payload, signature, header)
        UsernamePasswordAuthenticationToken : (principal, token, authorities)

            authority : 역할(Role), 권한(Permission)
            principal : 인증된 사용자의 정보 (=UserDetails 객체 id, pw, activities..)

        // 토큰 검증 
        public boolean validateToken(String token) {
            try {
                Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
                return true;
            } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
                logger.info("잘못된 JWT 서명입니다.");
            } catch (ExpiredJwtException e) {
                logger.info("만료된 JWT 토큰입니다.");
            } catch (UnsupportedJwtException e) {
                logger.info("지원되지 않는 JWT 토큰입니다.");
            } catch (IllegalArgumentException e) {
                logger.info("JWT 토큰이 잘못되었습니다.");
            }
            return false;
        }
    }

### 4. JWT 구현 - Filter & Handler

    이번에는 인증 시점에 자동으로 호출될 수 있는 JWT 필터를 구현해보고, JWT 필터를 통과하지 못한 경우 (권한 미달, 토큰 Validate 실패) 처리하는 JWT 핸들러를 구현해봅시다.

    ● JWT Filter

    @Slf4j
    public class JwtFilter extends GenericFilterBean {

        public static final String AUTHORIZATION_HEADER = "Authorization";

        private TokenProvider tokenProvider;

        public JwtFilter(TokenProvider tokenProvider) {
            this.tokenProvider = tokenProvider;
        }

        // 토큰의 인증정보 SecurityContext에 저장.
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throw IOException, ServletException {

            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequeset;
            String jwt = resolveToken(httpServletRequest);
            String requestURI = httpServletRequest.getRequestURI();

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                Authentication authentication = tokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Security Context에 '{}' 인증 정보를 저장했습니다, uri: {}", authentication.getName(), requestURI);
            } else {
                log.debug("유요한 JWT 토큰이 없습니다, uri: {}", requestURI);
            }

            filterChain.doFilter(servletRequest, serlvetResponse);
        }

        private String resolveToken(HttpServletRequest request) {
            String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
    
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7);
            }
    
            return null;
        }
    }

    - GenericFilterBean은 Spring Security와 함께 많이 사용되며, 사용자 인증, 권한 부여, 로깅 등과 같은 공통된 작업을 처리하는 데 유용하게 사용된다.
    
    - 따라서 UsernamePasswordAuthenticationFilter는 주로 SpringSecurity에서 사용자 인증에 특화된 목적으로 사용하는 반면, GenericFilterBean은 Spring Framework에서 일반적인 필터닐 작업을 수행하고자 할 떼, 사용된다. GenericFilterBean은 다양한 필터링 작업을 구현할 수 있는 유연성을 제공하며, Spring의 다른 기능과 통합하여 사용할 수 있다.