## 본문

### 개요

    entity, Dto 객체에 값을 넣어줄때, 룸복의 @Builder 사용하는 코드들이 많다.
    객체를 생성하기 위해서는 construct, java bean, builder 등을 사용할 수 있다. 그런데 왜 builder 패턴 사용 빈도가 많을까?

    우선 빌더 패턴이란 디자인패턴중 하나로, 생성과 표현의 분리다.
    클래스를 설계하다보면 필수로 받아야 할 인자들이 있고, 선택적으로 받아야할 인자들이 구분 되어있다.

    쉽게 말해 생성자에서 인자가 많을때 고려해 볼 수 있는 것이 바로, 이 빌더패턴이다.

### 점층적 생성자 패턴 (Telescoping Constructor Pattern)

    각 생성자를 오버로딩 해서 만드는 기초적인 방식이다.
    필수적으로 값이 있어야할 멤버변수를 위해 생성자에 매개변수를 넣는다.
    또한 선택적 인자를 받기위해 추가적인 생성자를 만든다.

    쉽게 구현한다는 장점이 있지만, 인자들이 많아질수록 생성자가 많아지고, 매개변수의 정보를 설명할 수 없으므로 어떤 객체에 어떤 인자가 들어갔는지 알기 어렵다.

    그래서 코드 수정(필드 추가 등)이 필요한 경우 수정이 복잡하다.

    public class Member {
        private String nickname; // 필수
        private String password; // 필수
        private Gender gender; // 선택

        public Member(String nickname) {
            this.nickname = nickname;
        }

        public Member(String nickname, String password) {
            this.nickname = nickname;
            this.password = password;
        }

        public Member(String nickname, String password, Gender gender) {
            this.nickname = nickname;
            this.password = password;
            this.gender = gender;
        }
    }

    Member member = new Member("joo", "123", F);
    - 매개변수의 어떤 위치에 어떤타입과 값을 넣어줘야하는지 개발자가 알고 있어야, 가독성도 떨어진다.

### Java Bean Pattern

    가장 익숙한 getter/setter 를 이용하여 객체를 생성할때 필드를 주입하는 방식이다.

    public class Member {
        private String nickname;
        private String password;
        private Gender gender;

        public Member() {
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setGender(Gender gender) {
            this.gender = gender;
        }
    }

    자바 빈즈패턴을 사용하면 가독성이 어느정도 해결된다. 
    하지만 코드양이 늘어나는 단점이 존재하고, 가장 문제가 되는점은 setter 메소드를 통해 값이 계속 변할 수 있기 떄문에, 객체일관성이 깨진다는 것이다.
    객체일관성이 깨진다는 것은 한번 객체를 생성할때 그 객체가 변할 여지가 있다는 것이다. (쓰레드간의 공유 가능한 상태가 존재한다는 것이기 대문에 immutable 객체를 생성할 수 없다.)

    Member member = new Member();
    member.setName("joo");
    member.setPassword("123");
    member.setGender("F");

    위 코드처럼 코드양이 늘어나고, set()메소드를 통해 값이 계속 변한다.
    이를 보완하고자 나온 기능이 빌더패턴이다.

### Builder Pattern 

    정보들은 Java Bean 패턴 처럼 받되, 데이터 일관성을 위해 정보들을 다 받은 후에 객체를 생성한다.

    빌더 패턴을 적용하면 다음과 같은 장점들이 있다.
    1. 불필요한 생성자의 제거
    2. 데이터의 순서에 상관없이 객체생성 가능
    3. 명시적 선언으로 이해하기가 쉽고, 각 인자가 어떤 의인지 알기 쉽다.(가독성)
    4. setter 메서드가 없으므로 변경 불가능한 객체를 만들수 있다.(객체불변성)
    5. 한번에 객체를 생성하므로 객체 일관성이 깨지지 않는다.
    6. build() 함수가 null인지 체크해주므로 검증이 가능하다. 
    (안 그러면 set하지않은 객체에대해 get을 하게되는 경우, nullPointerException 발생 등등의 문제가 생긴다.)

    빌더 패턴은 다음과 같이 만들어진다.
    - Member 클래스 내부에 빌더클래스 생성.
    - 각 멤버 변수별 메서드를 작성, 각 메소드는 변수에 값을 set하고 빌더객체를 리턴한다.
    - build() 메서드는 필수 멤버변수의 null 체크를 하고, 지금까지 set된 builder를 바탕으로 Member 클래스의 생성자를 호출하고 인스턴스를 리턴.

    // Gender Enum
    enum Gender {
        M, F; // 남성(M), 여성(F)
    }

    // Member 클래스
    public class Member {
        private String nickname; // 필수
        private String password; // 필수
        private Gender gender;   // 선택

        // Builder 클래스
        public static class Builder {
            private final String nickname; // 필수
            private final String password; // 필수
            private Gender gender;         // 선택

            // 필수 변수는 생성자로 받음
            public Builder(String nickname, String password) {
                this.nickname = nickname; // "minji"로 설정
                this.password = password; // "1234"로 설정
            }

            // 선택적 필드를 설정하는 메서드
            public Builder gender(Gender gender) {
                this.gender = gender; // Gender.F로 설정
                return this; // 메서드 체이닝을 위해 this를 반환
            }

            // Member 객체를 생성하는 메서드
            public Member build() {
                return new Member(this); // Builder의 필드를 Member로 전달
            }
        }

        // Builder 객체를 통해 Member를 생성
        private Member(Builder builder) {
            this.nickname = builder.nickname; // "minji"를 nickname으로 설정
            this.password = builder.password; // "1234"를 password로 설정
            this.gender = builder.gender;     // Gender.F를 gender로 설정
        }

        // 간단한 toString 메서드 추가 (출력용)
        @Override
        public String toString() {
            return "Member{" +
                    "nickname='" + nickname + '\'' +
                    ", password='" + password + '\'' +
                    ", gender=" + gender +
                    '}';
        }
    }

// Main 클래스
public class Main {
    public static void main(String[] args) {
        // Member 객체 생성
        Member memberEntity = new Member.Builder("minji", "1234") // nickname: "minji", password: "1234"
                .gender(Gender.F) // gender: Gender.F (여성)
                .build(); // Member 객체 생성

        // 생성된 Member 객체 출력
        System.out.println(memberEntity); // 출력: Member{nickname='minji', password='1234', gender=F}
    }
}

    ● 설명

    1. Builder 객체 생성 시 필수 피드 설정
    - new Member.Builder("joo", "1234"); 호출 시, Builder 생성자가 nickname, password 필수 값을 Builder 객체의 필드로 저장한다.
    
        this.nickname = nickname;
        this.password = password;

    - 위에 코드와 같이 Builder 객체 내부에 nickname, password가 필수 필드로 저장된다.

    2. 선택 필드 설정
    - 선택 필드인 gender 는 필요에 따라 설정할 수 있다.
        - Builder 패턴에서는 필수적으로 받아야 하는 필드들은 생성자 매개변수로 받고, 선택적인 필드들은 메서드로 설정할 수 있도록 하는 것이 일반적이다.

        builder.gender(Gender.F);

    - gender() 메서드는 Builder의 gender 필드를 설정한 후, this를 반환하여 다른 필드들도 추가로 설정할 수 있도록 한다. 여기서도 gender 는 Builder 클래스에 저장된다.

    3. build() 호출로 최종 Member 객체 생성
    - 최종적으로 build() 메서드를 호출하면 new Member(this)를 통해 Builder 객체가 가진 필드 값들을 Member의 생성자에 전달하여 Member 객체를 생성한다.   

        // Member 생성자, Builder 객체를 받아 필드 초기화
        private Member(Builder builder) {
            this.nickname = builder.nickname;
            this.password = builder.password;
            this.gender = builder.gender;
        }

    여기서 builder.nickname, builder.password, builder.gender 에 저장된 값들이 Member 클래스의 필드로 옮겨지는 구조이다.

    하지만 빌더 패턴도 역시 단점이 존재한다. 객체를 생성하려면 우선 빌더 객쳋를 생성해야 하고, 보다시피 다른 패턴들보다 많은 코드를 요구하기 때문이다. 인자가 충분하지 않은 상황에서 이용할 필요가 있따.                

### Lombok @Builder

    빌더 어노테이션을 사용하면 builder class 를 직접 만들지 않아도 롬복이 지원해주는 어노테이션 하나로 클래스를 생성할 수 있다. 클래스 또는 생성자 위에 @Builder 어노테이션을 붙여주면 빌더 패턴 코드가 빌드 된다. 생성자 상단에 선언 시, 생성자에 포함된 필드만 빌더에 포함된다.

    import lombok.Builder;

    @Builder
    public class Member {
        private String nickname;
        private String password;
        private Gender gender;
    }
    
    
    public static void main(String[] args) {
            Member memberEntity = Member.builder()
                    .nickname("minji")
                    .password("123")
                    .gender(Gender.F)
                    .build();
    }

    - 하지만 변수의 개수가 2개 이하이거나, 변경 가능성이 없을 경우에는 빌더 패턴의 장점을 누릴 수 없을테니 사용하지 않아도 될 것 같다.

### project 적용

    // Option class
    @Getter
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @AllArgsConstructor
    @Entity
    @Builder(toBuilder = true)
    public class Option extends BaseTimeEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Integer id;

        @NotNull
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "OPTION_CATEGORY_ID")
        private OptionCategory optionCategory;

        @NotNull
        @Column(unique = true)
        private String name;

        @NotNull
        private Integer price;
    }

    Option modifyOption = Option.builder()
                .optionCategory(optionCategory)
                .name(request.getName())
                .price(request.getPrice())
                .build();

    optionRepository.save(modifyOption.toBuilder()
                .id(optionId.getId())
                .build());

    ● 설명

    modifyOption 객체의 값들을 기반으로 새로운 객체를 생성하면서, 특정 필드(id)의 값을 optionId.getId()로 변경해 저장하려는 패턴이다.

    .toBuilder()  
    - modifyOption 객체를 복사한 빌더를 생성하여, 기존 데이터를 모두 Builder에 담고, 특정 필드를 변경하는 역할을 한다.

    .id(optionId.getId()) 
    - id 필드의 값을 optionId.getId()로 변경한다.
    - 기존 modifyOption의 id 값이 optionId.getId()로 덮어씌워진다.
    
    .build()
    - 모든 필터를 설정한 후, build() 메서드를 호출하여 새로운 Option 객체를 생성한다.
    - 생성된 객체는 optionRepository.save(...)에 전달되어 저장된다.

### project (가정 = 데이터 삽입)

    Option 객체를 생성하고, 생성된 modifyOption 객체를 toBuilder() 메서드를 통해 일부 필드(id)를 변경하여 새롭게 저장하는 방식이다.

    // 가정 : Request 객체에서 전달받은 값
    String optionCategory = "음료";  
    String optionName = "아메리카노"; 
    int optionPrice = 3000;

    // 초기 ID 값이 없는 상태로 Option 객체 생성
    Option modifyOption = Option.builder()
            .optionCategory(optionCategory) // optionCategory: "음료"
            .name(optionName)               // name: "아메리카노"
            .price(optionPrice)             // price: 3000
            .build();                       // -> Option 객체가 생성되며, ID는 아직 없음

    // 생성된 modifyOption 객체 상태 예시
    // modifyOption: Option(optionCategory="음료", name="아메리카노", price=3000, id=null)

    // ID가 있는 OptionId 객체로부터 ID를 가져옴
    Long newId = 101L; // ex) optionId.getId()의 결가가 101이라고 가정

    // modifyOption 객체의 모든 필드를 복사한 새로운 Builder 객체를 생성하고, ID를 업데이트
    Option udpateOption = modifyOption.toBuilder() // modifyOption 값을 복사하여 Builder 생성
            .id(newId)      // id: 101로 설정
            .build();       // -> ID가 설정된 새로운 Option 객체가 셍성됨

    // updatedOption: Option(optionCategory="음료", name="아메리카노", price=3000, id=101)

    // 저장을 위한 코드
    optionRepository.save(updateOption);    // updatedOption을 DB에 저장.

    ● 설명

    1. Option 객체 생성

    - 처음 modifyOption 을 생성할 때, optionCategory, name, price 필드는 주어딘 값으로 초기화되지만, id 필드는 설정되지 않은 상태이다.
    - modifyOption : Option(optionCategory="음료", name="아메리카노", price=3000, id=null)

    2. ID 변경을 위한 toBuilder 사용

    - modifyOption.toBuilder() 는 modifyOption의 상태를 그대로 복사한 Builder 객체를 반환한다. 이 빌더 객체에서 id 필드를 새 값(101)으로 설정한다.

    3. 새 Option 객체 생성 및 저장

    - build() 가 호출되면, ID가 101로 설정된 새로운 Option 객체 updateOption이 생성된다.
    - 최종 객체 : Option(optionCategory="음료", name="아메리카노", price=3000, id=101)  