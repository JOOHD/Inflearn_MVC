## 본문

### DI (Dependency Injection)

    정의 :

    의존성 주입은 객체 간의 의존 관계를 외부에서 주입하는 설계 패턴
    객체 스스로가 의존성을 생성하거나 관리하지 않고, 필요한 객체를 외부에서 주입받아 사용.

    핵심 :

    - new 키워드 없이 객체를 외부에서 주입받아 사용하는 방식     
    
    목적 :

    - 객체 간 결합도를 낮추고 재사용성을 높이며 테스트를 용의함.

### IoC (Inversion of Control)    

    정의 :

    제어의 역전은 객체의 생성, 생명주기 관리 등 제어권을 개발자가 아닌 프레임워크 or 컨테이너가 담당하도록 하는 개념.

    핵심 :

    - 제어권을 프레임워크에 위임하여 객체의 생성, 주입, 관리 등을 자동화
    - 개발자는 로직 구현에만 집중할 수 있다.

    역할 :

    - 객체 생성 : @Component, @Service, @Repository 등으로 등록된 객체를 스캔하여 생성.
    - 생명 주기 관리 : 객체의 생성부터 소멸까지 관리

    구현 방법 :

    1. 의존성 주입 (DI)
    - constructor, setter, field 주입

    1. 서비스 로케이터
    - IoC 컨테이너를 통해 필요한 객체를 요청하여 사용.

### IoC를 사용하는 코드 VS 사용하지 않는 코드

    ● 사용하지 않는 코드

    public class CoffeeService {

        private final CoffeeRepository coffeeRepository;

        public CoffeeService() {
            // CoffeeRepository 객체를 직접 생성
            this.coffeeRepository = new CoffeeRepository();
        }

        public void makeCoffee(String type) {
            coffeeRepository.save(type);
        }
    }

    public class CoffeeRepository {
        public void save(String type) {
            System.out.println(type + "coffee saved!");
        }
    }

    특징
    - CoffeeService 클래스에서 CoffeeRepository를 직접 생성(new 연산자 사용)
    - 강한 결합(타이트 커플링)으로 인해 객체 교체 및 테스트가 어려움.

    ● 사용한 코드

    // CoffeeRepository interface 변경
    public interface CoffeeRepository {
        void save(String type);
    }

    // CoffeeRepository의 구현체
    @Repository
    public class CoffeeRepositoryImpl implements CoffeeRepsitory {
        public void save(String type) {
            System.out.println(type + "coffee saved!");
        }
    }

    // CoffeeService 클래스에서 IoC 사용
    @Service
    public class CoffeeService {
        private final CoffeeRepository coffeeRepository;

        // 생성자 주입
        @Autowired
        public CoffeeService(CoffeeRepository coffeeRepository) {
            this.coffeeRepository = coffeeRepository;
        }

        public void makeCoffee(String type) {
            coffeeRepository.save(type);
        }
    }

    // Main 클래스에서 IoC 컨테이너 사용
    @SpringBootApplication
    public class CoffeeApplication {
        public static void main(String[] args) {
            ApplicationContext context = SpringApplication.run(CoffeeApplication.class, args);

            // IoC 컨테이너가 관리하는 CoffeeService 가져오기
            CoffeeService coffeeService = context.getBean(CoffeeService.class);
            coffeeService.makeCoffee("Espresso");
        }
    }

    특징
    - 객체 생성 및 주입은 Spring IoC Container 가 담당.
    - CoffeeService 와 CoffeeRepository 간 결합도가 낮아지고 테스트 및 유지보수가 쉬워짐

### DI 와 IoC의 관계

    - DI는 IoC의 한 구현 방법
    - IoC는 더 큰 개념으로, 객체 생성과 관리의 제어권을 외부로 넘기는 것.
    - DI는 IoC 컨테이너가 의존성을 주입하는 구체적인 방식.

### AOP (Aspect Oriented Programming)        

    

