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
            getIncludingRoles("AMIND")을 호출하면, 문자열 "ADMIN"이 Role.ADMIN으로 변환되고, Role.ADMIN의 getRoles() 메서드가 호출되어,
            ROLE_ADMIN이 반환된다.

    - addRole : 권한들이 모여있는 String에 추가하고 싶은 Role의 roles들을 추가하여 해당 권한들을 String으로 반환하는 함수.

    ● Role.getValueOf() vs toString()

      - 두 메서드 모두 enum과 문자열 간의 변환에 사용되지만, 목적이 다르다.

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

    