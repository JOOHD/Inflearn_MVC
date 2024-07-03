import javax.annotation.processing.Generated;

@Controller
public class HelloController {

    @GetMapping("hello")
    public String hello(Model model) {

        model.addAttribute("data", "hello!!");

        return "hello";
    }
}

<html> xmlns:th="http://www.thympleaf.org">
<head>
    <title>Hello</title>
    <meta http=equiv="content-Type" content="text/html; charset-UTF-8" />
</head>
<body>
<p th:text="'안녕하세요. ' + ${data}" >안녕하세요. 손님</p>
</body>
</html>

/*
web browser - localhost:8080/hello - tomcat server - HelloController - return:hello, model(data.hello) - viewResolver(templates/hello.html) - hello.html - web browser
-컨트롤러에서 리턴 값으로 문자를 반환하면 뷰 리졸버가 화면을 찾아서 처리한다.

-참고 : spring-boot-devtools 라이브러리를 추가하면,, html 파일을 컴파일만 해주면 서버 재시작 없이 View 파일 변경이 가능하다.
*/

// MVC와 템플릿 엔진
@Controller
public class HelloController {

    @GetMapping("hello-mvc")
    public String helloMvc(@RequestParam("name") String name, Model model) {

        model.addAttribute("name", name);

        return "hello-template";
    }
}

// view
<html xlms:th="http://www.thymeleaf.org">
<body>
<p th:text="'hello' + ${name}">hello! empty</p>
</body>
</html>


// web browser - localhost:8080/hello-mvc - tomcat - helloController(return:hello-template, model(name:spring)) - viewResolver - HTML(변환 후) - web browser

// API
@Controller
public class HelloController {

    @GetMapping("hello-string")
    @ResponseBody
    public String helloString(@RequestParam("name") String name) {
        return "hello" + name;
    }
}
// @ResponseBody를 사용하면 뷰 리졸버를 사용하지 않아도됨, 대신에 HTTP의 BODY에 문자 내용을 직접 반환

// @ResponsBody 객체 반환
@Controller
public class HelloController {

    @GetMapping("hello-api")
    @ResponseBody
    public Hello helloApi(@RequestParam("name") String name) {
        Hello hell = new Hello();
        hello.setName(name);
        return hello;
    }

    static class Hello {
        private String name;

        public String getName() {

        return name;

        }

        public void setName(String name) {

        this.name = name;

        }
    }
}

/*
web browser - localhost:8080/hello-api - tomcat - helloController(ResponseBody, return:hello(name:spring)) - HttpMessageConverter - name:spring - web browser

@ResponseBody를 사용
-HTTP의 BODY에 문자 내용을 직접 반환
-viewResolver 대신에 HttpMessagConverter가 동작
-기본 문자처리: StringHttpMessageConverter
-기본 객체처리: MappingJackson2HttpMessageConverter
-byte처리 등등 기타 여러 HttpMessageConverter가 기본으로 등록
*/

@SpringBootTest
@Transactional // 테스트 케이스에 이 애노테이션이 있으면, 테스트 시작 전에 트랜잭션을 시작하고, 
               // 테스트 완료 후에 항상 롤백한다. 이렇게 하면 DB에 데이터가 남지 않으므로 다음 테스트에 영향을 주지 않는다
class MemberServiceIntegrationTest {

    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;

    @Test
    public void 회원가입() throws Exception {

        //Given
        Member member = new Member
        member.setName("hello");

        //When
        Long saveId = memberService.join(member);

        //Then
        Member findMember = memberRepository.findById(saveId).get();
        asserEquals(member.getName(), findMember.getName());
    }

    @Test
    public void 중복_회원_예외() throws Exception {

        //Given
        Member member1 = new Member();
        member1.setName("spring");

        Member member2 = new Member();
        member2.setName("spring");

        //When
        memberService.join(member1);
        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> memberService.join(member2)); //예외가 발생해야된다.
        
        assertThat(e.getMessage()).isEqualTo("이미 존재하는 회원입니다.");
    }
}
///////////////////////////////////////////////////////////////////////////

public class JdbcTemplateMemberRepository implements MemberRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcTemplateMemberRepository(DataSource dataSource) {
        jdbcTemplate = new JdbcTemplate(datasource);
    }

    @Override
    public Member save(Member member) {
        SimplaJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
        jdbcInsert.withTableName("member").usingGeneratedKeyColumns("id");

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", member.getName());

        Number key = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(parameters));

        member.setId(key, longValue());

        return member;
    }

    @Override
    public Optional<Member> findById(Long id) {
        List<Member> result = jdbcTemplate.query("select * from member where id = ?", memberRowMapper(), id);

        return result.stream().findAny();
    }

    private RowMapper<Member> memberRowMapper() {
        return (res, rowNum) -> {
            Member member = new Member();
            member.setId(rs.getLong("id"));
            member.setName(rs.getString("name"));
            return member;
        }
    }
}

/**
build.gradle 파일에 JPA, h2 데이터베이스 관련 라이브러리 추가
dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    implementation 'org.springframework.boot:spring-boot-starter-web'
  //implementation 'org.springframework.boot:spring-boot-starter-jdbc'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'com.h2database:h2'
    testImplementation('org.springframework.boot:spring-boot-starter-test') {
        exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
    }
}

스프링 부트에 JPA 설정 추가
resources/application.properties
    spring.datasource.url=jdbc:h2:tcp://localhost/~/test
    spring.datasource.driver-class-name=org.h2.Driver
    spring.datasource.username=sa
    spring.jpa.show-sql=true
    spring.jpa.hibernate.ddl-auto=non

    주의!: 스프링부트 2.4부터는 spring.datasource.username=sa 를 꼭 추가해주어야 한다. 그렇지
    않으면 오류가 발생한다

    show-sql : JPA가 생성하는 SQL을 출력한다

    ddl-auto : JPA는 테이블을 자동으로 생성하는 기능을 제공하는데 none 를 사용하면 해당 기능을 끈다.
        - create 를 사용하면 엔티티 정보를 바탕으로 테이블도 직접 생성해준다. 해보자
 */

@Entity
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    //getter, setter...
}

//JPA 회원 리포지토리
public class JpaMemberRepository implements MemberRepository {

    private final EntityManager em;

    //생성자
    public JpaMemberRepository(EntityManager em) {
        this.em = em;
    }

    public Member save(Member member) {
        em.persist(member);
        return member;
    }

    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.ofNullable(member);
    }
        
    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
        .getResultList();
    }
        
    public Optional<Member> findByName(String name) {
        List<Member> result = em.createQuery("select m from Member m where 
        m.name = :name", Member.class)
                        .setParameter("name", name)
                        .getResultList();
                return result.stream().findAny();
    }

}