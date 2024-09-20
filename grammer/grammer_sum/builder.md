## 본문

### Builder pattern

    복잡한 객체의 생성 과정과 표현 방법을 분리하여 다양한 구성의 인스턴스를 만드는 생성 패턴이다. 
    생성자에 들어갈 매개 변수를 메서드로 하나하나 받아드리고 마지막에 통합 빌드해서 객체를 생성하는 방식이다.

    이해하기 쉬운 사례로 수제 햄버거를 들 수 있다. 
    수제 햄버거를 주문할때 빵이나 패티 등 속재료들은 주문하는 사람이 마음대로 결정된다.
    어느 사람은 치즈를 빼달라고 할 수 있고, 어느 사람은 토마토를 빼달라고 할 수 있다. 
    이처럼 선택적 속재료들을 보다 유연하게 받아 다양한 타입의 인스턴스를 생성할 수 있어, 클래스의 선택적 매개변수가 많은 상황에서 유용하게 사용된다.

### 점층적 생성자 패턴

    필수 매개변수와 함께 선택 매개변수를 0개, 1개, 2개... 받는 형태로, 우리가 다양한 매개변수를 입력받아 인스턴스를 생성하고 싶을 때, 사용하던 생성자를 오버로딩 하는 방식이다.

    ● Hambuger class

        class Hanbufer {
            // 필수 매개변수
            private int bun;
            private int patty;

            // 선택 매개변수
            private int cheese;
            private int lettuce;
            private int tomato;
            private int bacon;

            public Hamburger(int bun, int patty, int cheese, int lettuce, int tomato, int bacon) {
                this.bun = bun;
                this.patty = patty;
                this.cheese = cheese;
                this.lettuce = lettuce;
                this.tomato = tomato;
                this.bacon = bacon;
            }

            public Hamburger(int bun, int patty, int cheese) {
                this.bun = bun;
                this.patty = patty;
                this.cheese = cheese;
            }
        }

        ● main class

            public static void main(String[] args) {
                // 모든 재료가 있는 햄버거
                Hamburger hamburger1 = new Hamburger(2, 1, 2, 4, 6, 8);

                // 빵과 패티 치즈만 있는 햄버거
                Hamburger hamburger2 = new Hamburger(2, 1, 1);
            }

    하지만 이러한 방식은 클래스 인스턴스 필드들이 많으면 많을 수록 생성자에 들어갈 인자의 수가 늘어나 몇번째 인자가 어떤 필드였는지 햇갈릴 경우가 있다.

### Java bean pattern

    이러한 단점을 보완하기 위해 setter 메서드를 사용한 자바 빈 패턴이 고안 되었다.

    매개 변수가 없는 생성자로 객체 생성후 Setter 메소드를 이용해 클래스 필드의 초깃값을 설정하는 방식이다.

    ● Hambuger class (Java bean 적용)

        class Hamburger {
        // 필수 매개변수
        private int bun;
        private int patty;

        // 선택 매개변수
        private int cheese;
        private int lettuce;
        private int tomato;
        private int bacon;
        
        public Hamburger() {}

        public void setBun(int bun) {
            this.bun = bun;
        }

        public void setPatty(int patty) {
            this.patty = patty;
        }

        public void setCheese(int cheese) {
            this.cheese = cheese;
        }

        public void setLettuce(int lettuce) {
            this.lettuce = lettuce;
        }

        public void setTomato(int tomato) {
            this.tomato = tomato;
        }

        public void setBacon(int bacon) {
            this.bacon = bacon;
        }
    }

    ● main class

        public static void main(String[] args) {

            // 모든 재료가 있는 햄버거
            Hamburger hamburger1 = new Hamburger();
            hamburger1.setBun(2);
            hamburger1.setPatty(1);
            hamburger1.setCheese(2);
            hamburger1.setLettuce(4);
            hamburger1.setTomato(6);
            hamburger1.setBacon(8);

            // 빵과 패티 치즈만 있는 햄버거
            Hamburger hamburger2 = new Hamburger();
            hamburger2.setBun(2);
            hamburger2.setPatty(1);
            hamburger2.setCheese(2);

        }

    기존 생성자 오버로딩에서 나타났던 가독성 문제점이 사라지고 선택적인 파라미터에 대해 해당되는 Setter 메서드를 호출함으로써 유연적으로 객체 생성이 가능해졌다. 

    하지만 이러한 방식은 객체 생성 시점에 모든 값들을 주입 하지 않아 일관성 문제와 불변성 문제가 나타나게 된다.


    1. 일관성 문제
        필수 매개변수란 객체가 초기화될때 반드시 설정되어야 하는 값이다. 
        
        하지만 개발자가 실수로 set() 메서드를 호출하지 않았다면 이 객체는 일관성이 무너진 상태가 된다.(RuntimeException 발생)

    2. 불변성 문제    
        자바 빈즈 패턴의 Setter 메서드는 객체를 처음 생성할때 필드값을 설정하기 위해 존재하는 메서드이다.

        하지만 객체를 생성했음에도 여전히 외부적으로 Setter 메소드를 노출하고 있으므로, 협업 과정에서 언제 어디서나 누군가 Setter 메서드를 호출해 함부로 객체를 조작할 수 있게 된다.

        마치 완성된 햄버거에 중간에 치즈를 교체한다고 햄버거를 막 분리하는 것과 같은 이치이다.

### Builder pattern

    빌더 패턴은 이러한 문제들을 해결하기 위해 별도의 Builder 클래스를 만들어 메소드를 통해 step - by - step으로 값을 입력받은 후에 최종적으로 build() 메소드로 하나의 인스턴스를 생성하여 리턴하는 패턴이다.

    빌더 패턴 사용법을 잠시 살펴보면, StudentBuilder 빌더 클래스의 메서드를 체이닝(Chaining) 형태로 호출함으로써 자연스럽게 인스턴스를 구성하고 마지막에 build() 메서드를 통해 최종적으로 객체를 생성하도록 되어있음을 볼 수 있다.

    ● main class (builder pattern 적용)

        public static void main(String[] args) {

        // 생성자 방식
        Hamburger hamburger = new Hamburger(2, 3, 0, 3, 0, 0);

        // 빌더 방식
        Hamburger hamburger = new Hamburger.Builder(10)
            .bun(2)
            .patty(3)
            .lettuce(3)
            .build();
        }

    빌더 패턴을 이용하면 더 이상 생성자 오버로딩 열거를 하지 않아도 되며, 데이터의 순서에 상관없이 객체를 만들어내 생성자 인자 순서를 파악할 필요도 없고 잘못된 값을 넣는 실수도 하지 않게 된다.

### 빌더 패턴 구현

    빌더 패턴 구현 자체는 난이도가 어렵지 않다.

    ● student class

        class Student {
            private int id;
            private String name;
            private String grade;
            private String phoneNumber;

            public StudentBuilder id(int id) {
                this.id = id;
                return this;
            }

            public StudentBuilder name(String name) {
                this.name = name;
                return this;
            }

            public StudentBuilder grade(String grade) {
                this.grade = grade;
                return this;
            }

            public StudentBuilder phoneNumber(String phoneNumber) {
                this.phoneNumber = phoneNumber;
                return this;
            }
        }

    ● 특징 
    
    1. set 단어 빼주기
    
    이때 가독성을 좋게 하면서도 기존 Setter 외의 다른 특성을 가지고 있는 점을 알리기 위해서 set 단어는 빼주고 심플하게 멤버 이름으로만 메서드명을 지언준다.

    2. return this
    
    각 Setter 함수 마지막 반환 구문인 return this 부분이다. 
    여기서 this 란 StudentBuilder 객체 자신을 말한다. 즉, 빌더 객체 자신을 리턴함으로써 메서드 호출 후 연속적으로 빌더 메서드들을 체이닝하여 호출할 수 있게 된다.

        ex) new StudentBuilder().id(값).name(값)

    3. build 메서드 구성
    
    최종 Student 객체를 만들어주는 build 메서드를 구성해준다.
    빌더 클래스의 필드들을 Student 생성자의 인자에 넣어줌으로써 멤버 구성이 완료된 Student 인스턴스를 얻게 된다.

    ● Student class (build 메서드 구성)

        class StudentBuilder {
            private int id;
            private String name;
            private String grade;
            private String phoneNumber;

            public StudentBuilder id(int id) { ... }

            public StudentBuilder name(String name) { ... }

            public StudentBuilder grade(String grade) { ... }

            public StudentBuilder phoneNumber(String phoneNumber) { ... }

            public Student build() {
                return new Student(id, name, grade, phoneNumber); // Student 생성자 호출
            }
        }
    
### 빌더 클래스 실행

    public static void main(String[] args) {

        Student student = new StudentBuilder()
                .id(20240920)
                .name("아무개")
                .grade("Senior")
                .phoneNumber("010-0000-0000")
                .build();

        System.out.println(student);
    }

    결과 : Student {id = '20240920', name=아무개, grade=Senior}


    이렇게 생성자나 setter 메서드를 사용하여 필드를 초기화할 수 있지만, 많은 필드를 가진 객체의 경우 초기화 코드가 길어지고 가독성이 떨어질 수 있습니다. 이때 빌더 패턴을 사용하면 이러한 문제를 해결할 수 있습니다. 각 방법은 상황에 따라 장단점이 있으므로, 적절한 방법을 선택하여 사용할 수 있습니다.

    @Data
    public class AdminProductDto {
        private Integer id;
        private Integer productCategoryId;
        private String name;
        private String description;
        private Integer price;
        private SoldOutStatus soldOutStatus;
        private String picture;

        // 기본 생성자는 Lombok이 자동으로 생성
    }

    정적 코드

    AdminProductDto productDto = new AdminProductDto();
    productDto.setName("아메리카노");
    productDto.setDescription("맛있는 커피");
    productDto.setPrice(2800);
    productDto.setSoldOutStatus(SoldOutStatus.NOT_SOLD_OUT);

    동적 코드

    @RestController
    @RequestMapping("/products")
    public class ProductController {

        @PostMapping
        public ResponseEntity<AdminProductDto> createProduct(@RequestBody AdminProductDto productDto) {
            // 이곳에서 productDto에 대한 비즈니스 로직 처리
            // 예: 데이터베이스에 저장

            return ResponseEntity.ok(productDto);
        }
    }

    {
        "name": "아메리카노",
        "description": "맛있는 커피",
        "price": 2800,
        "soldOutStatus": "NOT_SOLD_OUT"
    }