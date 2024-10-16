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