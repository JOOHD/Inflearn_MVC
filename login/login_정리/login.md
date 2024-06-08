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