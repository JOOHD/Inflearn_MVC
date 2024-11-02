## 본문

### 용어 정리

    ● ApplicationContext 
    - 통상적 스프링 컨테이너
    - 빈을 관리, 검색해주는 것 외에도 여러 기능 

    ● AppConfig 
    - bean 설정/등록(@Configuration/@Bean을 사용하여, Spring 객체 정의), 어노테이션 기반 자바 설정 클래스
    
    ● BeanFactory 
    - bean 관리/조회(IoC container)
    - 스프링 컨테이너 최상위 interface
    - getBean() 제공
  
    -> Application(스프링 컨테이너)가 BeanFactory(bean 관리)를 확장하여 더 많은 기능을 제공하고, AppConfig(bean 등록)의 설정을 기반으로 관리하게 된다.

### 1. Spring Container 생성

    ApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);

    ● ApplicationContext 는 스프링 컨테이너 인터페이스
    스프링 컨테이너는 XML을 기반으로 만들 수 있고, 어노테이션 기반 자바 설정 클래스로 만들 수 있다.
    -> AppConfig 를 사용한 방식이 어노테이션 기반 자바 설정 클래스로 스프링 컨테이너를 생성한 방식.

    - AnnotationConfigApplicationContext는 ApplicationContext Inferface의 구현체
  
    스프링 컨테이너를 부를 때, BeanFactory, ApplicationContext를 구분해서 이야기하지만, BeanFactory를 직접 사용하는 경우는 거의 없기 때문에 통상적으로 ApplicationContext를 스프링 컨테이너라 한다.

![spring_container](../img/spring_container.png)    

    - 스프링 컨테이너를 생성할 때는 구성 정보를 지정해주어야 한다.

### 2. Spring bean 등록

![spring_bean](../img/spring_bean.png)  

    스프링 컨테이너는 파라미터로 넘어온 설정 클래스 정보를 사용해서 스프링 빈을 등록한다.
    @Bean 어노테이션이 붙어있는 메소드들을 순차적으로 스프링 빈에 등록한다.
    (AppConfig도 스프링 빈으로 등록된다.)

    빈 이름은 @Bean(name="...")을 통해 직접 부여할 수 있지만 일반적으로 메소드 이름을 사용한다.
    (빈 이름은 중복되면 안된다. 다른 빈이 무시되거나 설정에 따라 기존 빈을 덮어버리는 오류가 발생함.)

### 3. 의존관계 설정

![spring_dependency](../img/spring_dependency.png)

    스프링 컨테이너는 설정 정보를 참고해서 DI를 수행함.
    단순 자바코드를 호출하는 것과 같지만 차이가 있다. 

    - 빈을 생성하고, DI를 하는 단계가 나누어져있다. 자바코드로 스프링빈을 등록하면 생성자를 호출하면서 DI도 한번에 처리된다.

### 4. Spring bean 조회

    public class ApplicationContextInfoTest {
        AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext(AppConfig.class);

        @Test
        @DisplayName("모든 빈 출력")
        void findAllBean()
        {
            String[] beanDefinitionNames = ac.getBeanDefinitionNames();
            for (String beanDefinitionName : beanDefinitionNames) 
            {
                Object obj = ac.getBean(beanDefinitionName);
                System.out.println("name = " + beanDefinitionName + " object = " + bean);
            }
        }

        @Test
        @DisplayName("애플리케이션 빈 출력")
        void findApplicationBean()
        {
            String[] beanDefinitionNames = ac.getBeanDefinitionNames();
            for (String beanDefinitionName : beanDefinitionNames)
            {
                BeanDefinition beanDefinition = ac.getBeanDefinition(beanDefinitionName);

                // Role Role_APPLICATION : 직접 등록한 애플리케이션 빈
                // Role Role_INFASTRUCTURE : 스프링 내부에서 사용하는 모든 빈
                if (BeanDefinition.getRole() == BeanDefinition.ROLE_APPLICATION)
                {
                    Object bean = ac.getBean(beanDefinitionName);
                    System.out.println("name = " + beanDefinitionName + " object = " + bean);
                }
            }
        }
    }

    -> 모든 빈 출력
    ac.getBeanDefinitionName() : 스프링에 등록된 모든 빈 이름을 조회
    ac.getBean() : Bean 이름으로 Bean 객체 조회

    -> 애플리케이션 빈 출력
    스프링이 내부에서 사용하는 빈은 제외하고, 내가 등록한 빈만 조회
    getRole()로 구분 가능
    - getRole() == ROLE_APPLICATION : 일반적으로 사용자가 정의한 Bean
    - getRole() == ROLE_INFRASTRUCTURE : 일반적으로 사용자가 정의한 Bean

    스프링 빈 조회 - 기본
    스프링 컨테이너에서 스프링 빈을 찾는 가장 기본적인 조회 방법
    ac.getBean(name, type)
    ac.getBean(type)

    조회 대상 스프링 빈이 없으면 예외 발생
    - NoSuchBeanDefinitionException : No bean named 'xxxxxx' available
  
### BeanFactory와 ApplicationContext

![container_extends](../img/container_extends.png)

### 정리

    1. ApplicationContext 는 BeanFactory 의 기능을 상속받는다.
    2. ApplicationContext 는 빈 관리기능 + 편리한 부가 기능을 제고.
    3. BeanFactory 를 직접 사용할 일은 거의 없다. 부가기능이 포함된 ApplicationContext 를 사용한다.
    4. BeanFactory 나 ApplicationContext 를 스프링 컨테이너라고 한다.



