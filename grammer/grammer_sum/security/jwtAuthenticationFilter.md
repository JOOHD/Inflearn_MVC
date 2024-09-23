##  Joo_Cafe - jwtAuthenticationFilter class 분석 및 학습

### jwtAuthenticationFilter class code
    
    @Slf4j
    @Component
    @RequiredArgsConstructor
    public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
        public static final String TOKEN_HEADER = "Authorization";
        public static final String TOKEN_PREFIX = "Bearer "; // 인증 타입
    
        private final TokenProvider tokenProvider;
    
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
            // 다음 필터 실행 전 처리하는 로직
            String token = resolveTokenFromRequest(request);
    
            if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
                // 토큰 유효성 검증
                Authentication authentication = tokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                logger.debug("유효한 JWT 토큰이 없습니다.");
            }
            // 다음 filter-chain 실행
            filterChain.doFilter(request, response);
        }
    
        private String resolveTokenFromRequest(HttpServletRequest request) {
            String token = request.getHeader(TOKEN_HEADER);
    
            if (StringUtils.hasText(token) && token.startsWith(TOKEN_PREFIX)) {
                return token.substring(TOKEN_PREFIX.length());
            }
    
            return null;
        }
    }

### 코드 분석

    1. @Component
        - Spring Security의 필터 체인에서 이 필터를 사용하기 위해 등록.

    2. TOKEN_HEADER & TOKEN_PREFIX
        - TOKEN_HEADER : 클라이언트가 HTTP 요청을 보낼 때, 인증 정보를 담는 헤더의 이름이다.
        - TOKEN_PREFIX : JWT 토큰 앞에 붙는 문자열로, Bearer 로 시작하는 토큰을 의미한다.
                            즉, 실제 토큰 값은 Bearer 이후의 문자열이다.

    3. TokenProvider
        - 토큰의 생성, 유효성 검증, 인증 정보 추출 등의 로직을 포함하는 서비스이다.
            이 클래스는 JWT 관련 로직을 담당한다.

    4. doFilterInternal() 
        - 이 메서드는 필터 체인에서 호출되며, 실제로 요청을 처리하는 로직이 담겨있는 목적의 메서드이다.
        
        - 주요흐름
            1) HTTP 요청에서 토큰을 추출하고,
            2) 추출된 토큰이 유효하다면(validateToken(token)) 토큰을 사용해, 
                인증 객체(Authentication)를 생성하고, 이를 Spring security의 컨텍스트에 저장한다.
            3) 인증 정보가 없거나 토큰이 유효하지 않으면, 로그를 남기고 이후 필터로 요청을 남긴다.

    5. resolveTokenFromRequest()
        - 요청 헤더에서 토큰을 추출하는 메서드
            
        - 주요 흐름
            1) getHeader(TOKEN_HEADER) : HTTP 요청 헤더에서 Authorization 헤더의 값을 가져온다.
            2) stringUtils.hasText(token) : 토큰 값이 있는지 확인
            3) token.startsWith(TOKEN_PREFIX) : 토큰이 Bearer로 시작하는지 확인.
                만약 "Bearer "로 시작하는 유효한 토큰이 있다면, "Bearer " 부분을 제거하고,
                실제 JWT 토큰을 반환**합니다. 그렇지 않으면 null을 반환합니다.

    6. securityContextHolder
        - 현재 사용자의 보안 정보를 저장하는 컨텍스트
        - SecurityContextHolder.getContext().setAuthentication(authentication)을 통해,
            인증 객체(Authentication)를 설정하여, 이후의 요청에서 인증된 사용자 정보에 접근할 수 있게 됩니다.

    7. filterChain.doFilter(request, response)
        - 현재 필터의 작업이 완료된 후, 다음 필터로 요청을 넘긴다. 
            Spring Security의 필터 체인은 여러 개의 필터로 구성되며, jwt 인증 필터도 그 중 하나.

### 코드 해석 요약

    1. 토큰 추출 : HTTP 요청의 Authorization 헤더에서 JWT 토큰을 추출   
    2. 토큰 검증 : 추출된 토큰이 유효한지 검증(.validateToken)
    3. 인증 정보 설정 : 토큰이 유효하면, 인증 정보를 설정하여 Spring security가 이 사용자를 인증된 상태로 인식.

### 클라이언트의 요청 흐름 예시
    
    1. 클라이언트가 HTTP 요청을 보냄.
        - Authorization : Bearer eyJhbGciOiJIUzI1NiIsInR5... 헤더를 포함한 요청을 서버에 전송.

    2. 필터에서 토큰 추출
        - 위 필터가 실행되면서 JWT 토큰이 추출된다.

    3. 토큰 검증 및 인증 정보 설정
        - 토큰이 유효하다면, 해당 토큰을 통해 사용자의 인증 정보를 설정.

    4. 다음 필터로 요청 전달
        - 인등된 상태에서 요청이 계속 진행되어 컨트롤러가 호출된다.

### 토큰 
     
    토큰은 JWT(JSON WEB TOKEN) 라이브러리를 통해 생성되며, 
    Spring Boot 프로젝트에서는 이를 쉽게 처리할 수 있도록 다양한 라이브러리에서 지원한다.

    Spring Secutiry와 JWT를 함꼐 사용한다면, 로그인 후 서버가 사용자에게 JWT 토큰을 생성해 전달하고,
    이후 요청마다 클라이언트가 이 토큰을 보내서 인증을 받는 방식이다.

    토큰을 생성하는 과정에서는 보통 다음과 같은 단계를 따른다.

        1. 사용자 인증 
            - 사용자가 로그인하면, 애플리케이션은 사용자의 자격 증명을 확인
        2. JWT 생성
            - 자격 증명이 확인되면, 서버는 사용자 정보를 바탕으로 JWT 토큰을 생성한다.
                이때 서명을 통해 토큰의 무결성을 보장한다.
        
        3. 토큰 반환
            - 생성된 토큰을 클라이언트에 반환되고, 이후 요청마다 이 토큰을 보내서 인증을 받는다.

        이 과정을 Spring에서 처리할 때는 TokenProvider와 같은 클래스를 사용하여 토큰을 생성하고 검증하며, 
        JwtAuthenticationFilter로 요청마다 토큰을 확인하는 방식으로 진행됩니다.

