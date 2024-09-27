## 본문

### @NoArgsConstructor (access = AccessLevel.PROTECTED)

    - '아무런 매개변수가 없는 생성자를 생성하되 다른 패키지에 소속된 클래스는 접근을 불허한다.' 라는 뜻이다.

### Post Entity

    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED) // 생성자를 통해서 값 변경 목적으로 접근하는 메시지들 차단
    @Entity
    public class Post {
        @Id
        @Column(name = "post_id")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String title;

        private String content;

        private String hashTag;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "user_id")
        private User user;

        public void setUser(User user){
            // 기존 user와의 관계를 제거
            if (this.user != null) {
                this.user.getPostList().remove(this);
            }
            this.user = user;
            user.getPostList().add(this);
        }
    }

    즉, 아래와 같은 생성자 코드를 생성해 준다는 것이다.
        ex)
            protected Post() {}

### 왜 접근 권한을 Protected 일까?

    - Entity의 'Proxy 조회' 때문이다.
        
        정확히는 엔티티의 연관 관계에서 지연 로딩의 경우에는 실제 엔티티가 아닌 프록시 객체를 통해서 조회를 한다.

        프록시 객체를 사용하기 위해서 JPA 구현체는, 실제 엔티티의 기본 생성자를 통해 프록시 객체를 생성하는데, 이 때 접근 권한이 private이면 프록시 객체를 생성할 수 없는 것이다.

        이 때, 즉시 로딩으로 구현하게 되면, 접근 권한과 상관없이 프록시 객체가 아닌 실제 엔티티를 생성하므로 문제가 생기지 않는다.