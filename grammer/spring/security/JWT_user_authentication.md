## 사용자 인증 전체 흐름 (JWT 기반)

### JWT & TOKEN 

![JWT 인증 흐름](../grammer/images/jwt_flow.png)

### ✅ JWT 기반 사용자 인증 전체 흐름

| 단계 | 설명 |
|------|------|
| 1. 사용자 로그인 요청 | 클라이언트에서 이메일(ID)과 비밀번호를 입력하고 로그인 API 요청 전송. 요청은 `LoginFilter`에서 가로챔. |
| 2. LoginFilter 처리 | `LoginRequest`로 요청 바디 매핑 → `AuthenticationManager`에 인증 위임 (`attemptAuthentication`). |
| 3. CustomUserDetailsService | `loadUserByUsername(email)` 호출하여 DB에서 사용자 조회 → `CustomUserDetails`로 감싸 반환. |
| 4. 비밀번호 검증 | 입력 비밀번호와 DB 저장된 해시 비밀번호를 `BCryptPasswordEncoder`로 비교. 일치하지 않으면 예외 발생. |
| 5. 인증 성공 및 JWT 발급 | `JWTUtil`을 통해 `AccessToken`, `RefreshToken` 생성. AccessToken은 헤더로, RefreshToken은 쿠키로 전송. |
| 6. RefreshToken 저장 | 사용자 객체(`Member`)에 refreshToken 저장 또는 DB 저장 (재발급 시 검증용). |
| 7. API 요청 시 토큰 포함 | 클라이언트는 이후 API 요청 시 `Authorization: Bearer <accessToken>` 헤더 포함. |
| 8. JWTFilter 처리 | AccessToken 추출 → 유효성 검사 → 사용자 정보 로드 → 인증 객체 생성 후 `SecurityContextHolder`에 저장. |
| 9. SecurityContextHolder 사용 | 인증된 사용자 정보는 `@AuthenticationPrincipal` 또는 `SecurityContextHolder`로 접근 가능. |
| 10. AccessToken 만료 시 재발급 | RefreshToken 쿠키로 재발급 요청 → 서버에서 토큰 검증 → 새 AccessToken 발급 후 전달. |
| 11. 로그아웃 처리 | `CookieUtil`을 통해 RefreshToken 쿠키 제거 및 서버 DB에서도 토큰 삭제. |

### 🔐 주요 클래스 역할 요약

| 클래스명 | 역할 |
|----------|------|
| `LoginFilter` | 로그인 요청 처리, 인증 성공 시 JWT 발급 |
| `CustomUserDetailsService` | 사용자 조회 로직 (UserDetails 반환) |
| `CustomUserDetails` | 사용자 정보를 담은 인증용 객체 |
| `JWTFilter` | 요청마다 토큰 유효성 검증, 인증 처리 |
| `JWTUtil` | 토큰 생성, 파싱, 검증 도구 |
| `CookieUtil` | RefreshToken 쿠키 저장/삭제 |
| `MemberService` | 회원가입, 로그인, refreshToken 저장 등 |

### OAuth2 추가 사용자 인증 전체 흐름

| 단계 | 설명 |
|------|------|
| 1. 사용자 로그인 요청 | 클라이언트가 일반 로그인 (`id(email) + password`) 또는 OAuth2 로그인(Google, Naver 등) 선택 |
| 2. 일반 로그인 요청 처리 | 이메일/비밀번호로 로그인 요청 시 `LoginFilter`에서 가로채고 인증 수행 |
| 3. OAuth2 로그인 요청 처리 | 클라이언트가 `/oauth2/authorization/{provider}` 요청 → Spring Security가 OAuth2 인증 처리 시작 |
| 4. OAuth2 서버 인증 성공 | OAuth2 제공자(Google 등)에서 사용자 인증 → 인증 코드 → 액세스 토큰 발급 |
| 5. OAuth2 사용자 정보 수신 | `CustomOAuth2UserService`가 사용자 정보 받아와 DB에 사용자 저장 or 갱신 |
| 6. 인증 객체 생성 | `CustomUserDetails` or `CustomOAuth2User` 객체 생성하여 Spring Security 인증 완료 처리 |
| 7. AccessToken, RefreshToken 발급 | `JWTUtil`을 통해 JWT 생성. AccessToken은 헤더로, RefreshToken은 `Set-Cookie`로 전송 |
| 8. RefreshToken 저장 | `Member` 객체에 refreshToken 저장 또는 DB 저장. (재발급 시 토큰 유효성 확인용) |
| 9. API 요청 시 토큰 포함 | 클라이언트가 이후 API 요청 시 `Authorization: Bearer <accessToken>` 헤더 포함 |
| 10. JWTFilter 처리 | AccessToken 추출 → 파싱 → 검증 → 인증 객체 생성 → `SecurityContextHolder`에 저장 |
| 11. 인증된 사용자 접근 | 인증된 사용자는 `@AuthenticationPrincipal` 또는 `SecurityContextHolder`로 정보 접근 |
| 12. AccessToken 만료 시 재발급 | RefreshToken을 쿠키에서 추출 → 서버에서 유효성 검사 → AccessToken 재발급 |
| 13. 로그아웃 처리 | RefreshToken 쿠키 삭제 + 서버/DB 저장된 토큰 제거 (`CookieUtil` 사용) |

---

### 🔐 주요 클래스 역할 요약

| 클래스명 | 역할 |
|----------|------|
| `LoginFilter` | 일반 로그인 요청 처리, 인증 성공 시 JWT 발급 |
| `CustomUserDetailsService` | 이메일로 사용자 조회, `UserDetails` 반환 |
| `CustomUserDetails` | 일반 로그인 사용자 인증 객체 |
| `CustomOAuth2UserService` | OAuth2 로그인 성공 후 사용자 정보 DB 저장/업데이트 |
| `CustomOAuth2User` | OAuth2 사용자 정보를 담은 인증 객체 |
| `JWTFilter` | 매 요청 시 AccessToken 검사 및 인증 처리 |
| `JWTUtil` | JWT 생성, 파싱, 검증 기능 담당 |
| `CookieUtil` | RefreshToken 쿠키 생성/삭제 도우미 |
| `MemberService` | 회원가입, 일반 로그인, 토큰 저장 및 검증 등 비즈니스 로직 담당 |
