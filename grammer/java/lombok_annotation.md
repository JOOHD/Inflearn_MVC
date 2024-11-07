## 본문

### lombok annotation 

    어노테이션 기반으로 코드 자동완성 기능을 제공하는 라이브러리.

    Spring, Spring Boot 로 Web 개발을 하다보면 반복되는 코드가 자주 등장하며 가독성을 떨어트린다.

    예를 들어보면 Getter, Setter, ToString, Constructor(생성자)가 대표적인 예제일 것이다. 

### @NoArgsConstructor

    : 파라미터가 없는 디폴트 생성자 생성.

    ● 주의점

    - 필드들이 final로 생성되어 있는 경우에는 필드를 초기화할 수 없기 때문에, 생성자를 만들 수 없어 에러가 발생한다.
    이 때, @NoArgsConstructor(force = true)옵션을 이용해 final 필드를 O, false, null 등으로 강제 초기화시켜 생성자를 만들 수 있다.

    - @NonNull 같이 필드에 제약조건이 설정되어 있는 경우, 생성자 내 null-check 로직이 생성되지 않는다.
    후에 초기화를 진행하기 전까지 null-check 로직이 발생하지 않는 점을 염두해야 한다.

    ex)

        @NoArgsConstructor
        public class User {
            private String name;
            private int age;

            // Lombok이 자동으로 아래와 같은 기본 생성자를 생성해줍니다.
            // public User() {
            //     this.name = null;
            //     this.age = 0;
            // }
        }

### @RequiredArgsConstructor

    : final & @NonNull 마크된 필드만을 파라미터로 받는 생성자를 자동 생성.
    (일반 필드는 포함되지 않으므로, 해당 필드들은 기본값으로 초기화됩니다.)

    ● 주의점
    
    - 파라미터의 순서는 클래스에 있는 필드 순서에 맞춰 생성자가 생성.
    - @NonNull 필드들은 null-check 가 추가적으로 생성되며, @NonNull이 마크되어 있어도 파라미터에서 null 값이 들어온다면 생성자에서 NullPointerException이 발생한다.

    ex)

        @RequiredArgsConstructor
        public class Member {
            private final String nickname;
            private final String password;
            private Gender gender;  // 일반 필드

            // Lombok에 의해 자동 생성되는 생성자
            // public Member(String nickname, String password) {
            //     this.nickname = nickname;
            //     this.password = password;
            // }
        }

    - 이 경우, nickname, password 필드는 final로 선언되어 있어, 생성자에서 반드시 초기화해야 한다.

    - 하지만 gender 필드는 final이 아니기 때문에 생성자에서 초기화하지 않고 기본값인 null로 초기화된다.
  
    ex)
        Member member = new Member("joo", "1234");  // gender는 null로 초기화

### @AllArgsConstructor

    : 클래스의 모든 필드 값을 파라미터로 받는 생성자를 자동으로 생성.
    (클래스의 모든 필드를 한 번에 초기화할 수 있다.)

    ex)

        @AllArgsConstructor
        public class Member {
            private String nickname;
            private String password;
            private Gender gender;

            // Lombok에 의해 자동 생성되는 생성자
            // public Member(String nickname, String password, Gender gender) {
            //     this.nickname = nickname;
            //     this.password = password;
            //     this.gender = gender;
            // }
        }     

    - 이 경우, Member 객체를 생성할 때, name, age 값을 모두 매개변수로 전달해야 한다ㅏ.
    
    ex)
        Member member = new Member("joo", "1234", Gender.F);