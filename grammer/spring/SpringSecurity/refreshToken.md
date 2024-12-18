## 본문

### 설정

    JWT는 한 번 발급하면 만료되기 전까지 삭제할 수 없다.
    따라서 짧은 유효시간을 갖는 AccessToken 과 저장소에 저장해서 AccessToken 을 재발급이 가능한 RefreshToken 이 있다. RefreshToken 발급과 관리 및 이를 통한 AccessToken 재발급에 대해 알아보자.

    + AccessToken 재발급은 2가지 방법이 있다.

    1. 요청마다, Access/Refresh Token 을 같이 넘기는 방법.
    2. 재발급 API를 만들고 서버에서 AccessToken 이 만료되었다고 응답하면, RefreshToken 으로 요청하여 재발급 받기.

    우선 1번으로 진행

### RefreshToken 사용

    JWT는 발급 후, 삭제가 불가능하기 때문에 접근에 관여하는 토큰은 유효시간을 길게 부여할 수 없다. 하지만 자동 로그인 혹은 로그인 유지를 위해서는 유효시간이 긴 토큰이 필요하다. 이때 사용되는 것이 RefreshToken 이다.

    ● 생명 주기

    AccessToken : 발급된 이후, 서버에 저장되지 않고 토큰 자체로 검증을 하며 사용자 권한을 인증한다.

        - 이런 역할을 하는 AccessToken 이 탈취되면 토큰이 만료되기 전 까지, 토큰을 획득한 사람은 누구나 권한 접근이 가능해진다.

        - 따라서 AccessToken 유효 주기는 짧게 가져가야 한다.

    그러면, 자동 로그인 혹은 로그인 유지는?

        - 이제 RefreshToken 이 역할이다. RefreshToken 은 한 번 발급되면 AccessToken 보다 훨씬 길게 발급된다.

        - 대신에 접근에 대한 권한을 주는 것이 아니라 AccessToken 재발급에 관여한다.

    ● AccessToken 재발급 방법

    보통 RefreshToken 은 로그인 성공시 발급되며 저장소에 저장하여 관리된다.
    그리고 사용자가 로그아웃을 하면 저장소에서 RefreshToken 을 삭제하여 사용이 불가능하도록 한다.

    AccessToken 이 만료되어, 재발급이 진행된다면 다음의 과정을 통해 재발급이 된다.

        1. RefreshToken 유효성 체크
        2. DB에 RefreshToken 존재유무 체크
        3. 1,2 모두 검증되면 재발급 진행
        4. Response Header 에 새로 발급한 AccessToken 저장

    이후 클라이언트는 재발급된 AccessToken 을 Request Header에 포함하여 요청을 보내면 정상적으로 접근이 허용된다.

### RefreshToken 적용

    ● Domain

        // RefreshToken.class
        @Entity
        @AllArgsConstructor
        @NoArgsConstructor
        public class RefreshToken {

            @Id
            @Column(nullable = false)
            private String refreshToken;
        }

    - 단순 토큰 값만 저장하면 되기 때문에, id 는 생성x
    - 이번에는 H2 를 사용한 방법이고, 다음에는 Redis(key, value)를 이용해보겟다.

    ● Interface

        public interface TokenRepository extends JpaRepository<RefreshToken, Long> 
        {
            boolean existsByRefreshToken(String token);
        }
        
        - token 존재 여부 판단이 목적.

    ● Controller

        // 로그인
        @PostMapping("/login")
        public ResposneEntity login(@RequestBody UserDTO user, HttpServletResponse response) {

            // 회원 존재 유무
            User member = userService.findMember(user);

            // 비밀번호 체크
            uerService.checkPassword(member, user);

            // access, refresh token 발급 및 헤더 설정
            String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), member.getRoles());
            String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), member.getRoles());
            jwtTokenProvider.setHeaderAccessToken(response, accessToken);
            jwtTokenProvider.setHeaderRefreshToken(response, refreshToken);

            // refreshToken DB에 저장.
            tokenRepository.save(new RefreshToken(refreshToken));

            return ResponseEntity.ok().body("로그인 성공!");
        }

        - 응답 헤더에 토큰을 추가하는 작업을 AccessToken 재발급 진행하면서 사용하기 때문에 jwtProvider 의 메서드로 작성하였다.

### JwtTokenProvider

    ● refreshToken 유효시간

        private long refreshTokenValidTime = 1 * 60 * 1000L;

    ● token 가져오기

        // Request의 Header에서 AccessToken 값을 가져온다. "authorization" : "token"
        public String resolveAccessToken(HttpServlerRequest request) 
        {
            if (request.getHeader("authorization") != null) {
                return request.getHeader("authorization").substring(7);
            }
            return null;
        }

        // response.setHeader("Authorization", "Bearer " + accessToken);
        // response.setHeader("Set-Cookie", refreshTokenHeader);

    ● refreshToken 재발급 후, 권한 부여를 위한 권한 반환 메서드

        // Email 로 권한 가져오기
        public List<String> getRoles(String email) 
        {
            return userRepository.findByEmail(email).get().getRoles();
        }      

### JwtAuthenticationFilter

    상속 객체 변경 GenericFilterBead -> OncePerRequestFilter

    기존 Filter 는 jwt 검증 예외가 발생하는 경우 Filter가 여러번 동작하는 것을 확인하여 새로운 방안으로 적용하였다.

    전체적인 뼈대는 같으나 오버라이딩 하는 메서드 명과 doFilter 방식이 다르다.

    @RequiredArgsConstructor
    public class JwtAuthenticationFilter extends OncePerRequestFilter {

        private final JwtTokenProvider jwtTokenProvider;

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            // 헤더에서 JWT 를 받아옵니다.
            String accessToken = jwtTokenProvider.resolveAccessToken(request);
            String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
        }
    }    


