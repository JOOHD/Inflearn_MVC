## 본문

### SpringBoot JWT UserDetails/UserDetailsService

    1. 시큐리티는 "~/login" 주소로 요청이 오면 가로채서 로그인을 진행한다.

    2. 로그인 진행이 완료되면 시큐리티_session을 만들고 SecurityContextHolder에 저장한다.
    (SecurityContextHolder = 시큐리티 인메모리 세션 저장소)

    3. 시큐리티가 갖고 있는 시큐리티_session에 들어갈 수 있는 Object는 정해져있다.
    (Object == Authentication 타입 객체)

    4. Authentication 안에 User 정보가 있어야 한다.

    5. 객체 타입은 UserDetails 타입 객체이다.  

    ● 정리

    - Security Session에 객체를 저장해준다.
    - session에 저장될 수 있는 객체는 Authentication 타입이다.
    - Authentication 객체는 User를 저장한다.
    - User 객체는 UserDetails 타입이다.
    - 따라서 우리 서비스는 UserDetails를 상속받는 User를 만들어야 한다.

    - Security_Session -> Authentication -> UserDetails 관계를 갖는 것이므로 차례로 접근해서 꺼내면 된다.
    - Security_Sessoin에 접근해서 Authentication을 꺼내고 거기서 UserDetails 타입 User를 꺼내면 우리가 원하는 User 객체를 꺼낼 수 있게 되는 것이다.
     
### UserDetails

    사용자의 정보를 담는 interface이다. Spring_Security에서 사용자의 정보를 불러오기 위해서 구현해야 하는 인터페이스로 기본 Override 메서드는 아래와 같다.


|    메소드    |   리턴타입   |   설명    |   기본값   |  
| ------------ | ----------- | --------- | ---------- |
| getAuthorities() | Collection<? extends GrantedAuthorities> | 계정의 권한 목록을 반환 |
| getPassword() | String | 계정의 비밀번호를 반환 |     |
| getUsername | String | 계정의 고유한 값을 반환(DB PK/ 중복없는 Email) |    |
| isAccountNotExpired() | boolean | 계정의 만료 여부 반환 | true(만료 안됨)  |
| isAccountNotLocked() | boolean | 계정의 잠김 여부 반화 | true(잠기지 않음) |
| isCredencialsNotExpired() | boolean | 비밀번호 만료 여부 반환 | true(만료 안됨) |
| isEnabled() | boolean | 계정의 활성화 여부 반환 | true(활성화 됨) |

### UserDetailsService

    UserSecurityServicee는 스프링 시큐리티 설정에 등록할 클래스이다.
    이 클래스는 스프링 시큐리티 로그인 처리의 핵심 부분이다.
    
    @RequiredArgsConstructor
    @Service
    public class UserSecurityService implements UserDetailsService {

        private final UserRepository userRepository;

        @Overrride
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            Optional<SiteUser> userOptional = userRepository.findByUsername(username);

            SiteUser user = userOptional.orElseThrow(() -> {
                throw new UsernameNotFoundException("사용자를 찾을 수 없습니다.");
            });

            List<GrantedAuthority> authorities = new ArrayList<>();
            if ("admin".equals(username)) {
                authorities.add(new SimpleGrantedAuthority(UserRole.ADMIN.getValue()));
            } else {
                authorities.add(new SimpleGrantedAuthority(UserRole.USER.getValue()));
            }

            return new User(user.getUsername(), user.getPassword(), authorities);
        }
    }

    UserSecurityService는 스프링 시큐리티가 제공하는 UserDetailsService interface를 구현(implements)해야 한다.
    UserDetailsService는 loadUserByUsername 메서드를 구현하도록 강제한다.
    해당 메서드는 사용자명으로 비밀번호를 조회하여 찾은 사용자 객체를 리턴한다.

    정확히는 UserDetailsService는 사용자의 정보를 가져오는 인터페이스이며, 사용자의 정보를 불러와서 UserDetails(반환 타입)으로 반환한다.

### PrincipalUserDetails

    - UserDetails implements class
    
    일반적으로 Spring Security에서 사용자 인증과 권한을 처리할 때, 
    사용되는 UserDetails 인터페이스의 구현체이다.
    이 클래스는 사용자 정보를 Spring Security가 인식할 수 있도록 사용자 객체를 감싸는 역할을 한다.

    Spring Security에서 사용자 인증 과정을 처리할 때, UserDetailsService를 통해서 사용자의 세부 정보를 가져온다. 
    이때 반환되는 객체는 UserDetails 인터페이스를 구현한 클래스여야 하며, 이 클래스가 바로 PrincipalUserDetails 이다.

    ● 목적
    
    1. UserDetails interface
        - Spring Security에서 authentication & authority에 필요한 사용자 정보를 제공하는 인터페이스이다.
        - 이 인터페이스는 인증된 사용자에 대한 필수 정보를 반환하도록 요구된다.
            ex) username, password, authority...

    2. UserDetailsService와 연계
        - UserDetailsService는 DB나 외부 시스템에서 사용자 정보를 조회하고, PrincipalUserDetails 객체를 반환하여 Spring Security에 의해 사용될 수 있도록 한다.

    ● 클래스의 구현

    public class PrincipalUserDetails implements UserDetails {

        private final Member member; // 실제 사용자 객체

        public PrincipalUserDetails(Member member) {
            this.member = member;
        }

        // 사용자 권한을 반환
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of(new SimpleGrantedAuthority(member.getRole().name()));
        }

        // 사용자 비밀번호를 반환
        @Override
        public String getPassword() {
            return member.getPassword();
        }

        // 사용자 이름(이메일 또는 ID)를 반환
        @Override
        public String getUsername() {
            return member.getEmail();
        }

        // 계정이 만료되었는지 여부를 반환 (true: 만료되지 않음)
        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        // 계정이 잠겨 있는지 여부를 반환 (true: 잠기지 않음)
        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        // 자격 증명이 만료되었는지 여부를 반환 (true: 만료되지 않음)
        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        // 계정이 활성화 상태인지 여부를 반환 (true: 활성화됨)
        @Override
        public boolean isEnabled() {
            return true;
        }

        // 추가적으로 member 객체에 접근할 수 있도록 getter 제공
        public Member getMember() {
            return member;
        }
    }    

    ● Spring Security flow

    1. 사용자가 로그인하면, Spring security는 UserDetailsService의 loadUserByUsername() 메서드를 호출하여 사용자 정보를 조회.

    2. loadUserByUsername() 메서드는 사용자 정보를 조회한 후, 이를 PrincipalUserDetails 객체로 감싸서 반환.

    3. SpringSecurity는 PrincipalUserDetails 객체에서 필요한 인증 정보(email, password, authority등)를 가져와 인증 과정을 처리한다.

    PrincipalUserDetails 클래스는 사용자 Entity(Member)의 정보를 Spring Security가 처리할 수 있도록, UserDetails 형태로 변환하는 역할을 한다.
    