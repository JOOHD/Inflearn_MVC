## 본문

### JVM 구조

    Java는 컴파일 방식과, 인터프리터 방식의 장점을 결합한 하이브리드 언어이다.

![compiler_runtime](../img/compiler_runtime.png)

    하이브리드 방식에는 작성된 원시코드를 이진파일로 컴파일하는 과정과, 실제 해당 코드가 실행되는 시점(Runtime)에서 JVM에서 이 이진파일에서 필요한 부분들을 한 줄씩 읽어(interprete) 실행된다.

    Runtime 시점에서 JVM을 통해 동작하여 자바는 운영체제로부터 독립적인 환경에서 실행 사능하다는 장점을 가지고 있다.

### JVM 동작 방식

![JVM_run](../img/JVM_run.png)

    1. 자바로 작성된 프로그램을 실행하면 JVM은 운영체제로부터 메모리를 할당 받는다.

    2. 자바 컴파일러가 자바 소스코드를 자바 바이트 코드(.class)로 컴파일

    3. 이 바이트 코드 파일을 JVM 내부 Class Loader를 통해 Runtime Data Area로 로딩한다.

        ● Class Loader
            - 자바는 실행 시점에 동적으로 클래스를 읽어오기 때문에, 실행 시점에서 코드가 JVM과 연결된다.

            - Java에서 소스를 작성한 파일을 Java -> 실행 시점에 Java로 부터 필요한 코드를 뽑아온 이후 이진 파일로 변환된 .class 파일들을 적재하는 곳이 Class Loader.
      
![RuntimeDataAreas](../img/RuntimeDataAreas.png)      

        ● Runtime Data Area

            - 실행 시점에 사용하는 데이터들은 운영체재로부터 할당받은 메모리 영역인 Runtime Data Area에 메소드 영역, 힙 영역, 스택 영역, PC 레지스터, 네이티브 메소드 스택 5가지로 구분.

            - Class Loader에서 로드 받은 .class 파일들을 메소드 영역에 저장.

    4. Runtime Data Area에 로딩된 .class 파일들을 Excution Engine을 통해 해석된다.   

        ● Execution Engine
            
            Runtime Data Area의 메소드 영역에 배치된 .class 파일들을 Execution Engine에 provide하여, 정의된 내용대로 바이트 코드를 실행시킵니다. (실제적으로 이진파일들이 실행되는 중요한 곳) 

    5. 해석된 바이트 코드는 Runtime Data Area의 각 영역에 배치되어 수행하며, 이 과정에서 Excution Engine에 의해 가비지 컬렉터가 동작하며, 스레드 동기화 또한 이루어집니다.

        ● GC(Garbage Collector)

        - 더 이상 사용하지 않는 메모리를 자동으로 회수해주는 역할
        - 이 덕분에 개발자는 별도로 사용하지 않는 메모리를 관리하지 않아도 됩니다.

        - 어떻게 작동하는가?
            - Heap 메모리 영역에 생성된 객체들 중에 참조되지 않은 객체들을 탐색 후 제거하는 역할을 하며 해당 역할을 하는 시간은 정학이 알 수 없다.

### Runtime Data Area의 각 영역에 어떤게 저장될까?

    1. 메소드 
    
    - 실행 시점에 사용되는 .class 파일들이 메소드 영역에 저장된다.
    - 뿐만 아니라 클래스 멤버 변수의 이름, 데이터 타입, 접근 제어자 정보와 같이 각종 필드 정보들과 메소드 정보 데이터 Type 정보, static 변수, final class, 상수 풀 등이 이 곳에 저장된다.

    2. Heap

![HeapArea](../img/HeapArea.png)  

    - new 키워드로 생성된 객체, 배열이 저장되는 곳
    
### Stack

    Stack의 경우에는 정적으로 할당된 메모리 영역이다.

    Stack에서는 Primitive 타입 (boolean, char, short, int, long, float, double) 의 데이터가 값이랑 같이할당이 되고,

    또 Heap 영역에 생성된 Object 타입의 데이터의 참조 값이 할당 된다.

    그리고 Stack 의 메모리는 Thread당 하나씩 할당 된다. 만약 새로운 스레드가 생성되면 해당 스레드에 대한 Stack이 새롭게 생성되고, 각 스레드 끼리는 Stack 영역을 접근할 수 가 없다.

### Heap    

    Heap의 경우에는 동적으로 할당된 메모리 영역이다.

    힙 영역에서는 모든 Object 타입의 데이터가 할당이 된다. (참고로 모든 객체는 Object 타입을 상속받는다.)

    Heap 영역의 Object를 가리키는 참조변수가 Stack에 할당이 된다. 어플리케이션에서의 모든 메모리 중에서 Stack에 쌓이는 애들 빼고는 전부 이 Heap 쌓인다고 보면 편할듯 하다..

    근데 보통 이 Heap 영역의 데이터들은 생명주기가 길다. 그 이유는 대부분 Object의 크기가 크고, 서로 다른 코드블럭에서도 공유가 되다 보니 그런것이다. 

    그리고 Heap 은 Stack 처럼 Thread 마다 하나씩있는게 아니라 여러개의 Thread가 있어도 힙은 단하나의 영역만 존재한다. 헷갈리지 말자.

### 예제 코드

    public class Main {

        public static void main(String[] args) {

            int age = 32;
            String name = "kang";

            List<String> skills = new ArrayList<>();
            skills.add("java");
            skills.add("js");
            skills.add("c++");

            test(skills);

        }

        public static void test(List<String> list) {
            String mySkill = list.get(0);
            list.add("python");
        }
        
    }

    ● int_age = 32; 가 메모리 영역에 할당되는.

![int_age](../img/int_age.png)   

    ● String name = "kang"; 가 메모리 영역에 할당되는.

![String_name](../img/String_name.png)

    String -> object를 가리키는 변수만 Stack에 쌓이고, String Object 자체는 Heap에 할당된다.

    ● List<String> skills = new ArrayList<>();

![List<String>_skills](../img/List_String_skills.png)

    skills 리스트가 ArrayList로 아직 값이 채워지지않고 생성만됐을 때 상태는 위와 같다.

    ● 여기서 값이 Add가 되면 이렇게 된다.
    skills.add("java");
    skills.add("js");
    skills.add("c++");

![skills_add](../img/skills_add.png)

    ● test(skills);
    public static void test(List<String> list) {
        String mySkill = list.get(0);
        list.add("python");
    }

![test_skills](../img/test_skills.png)

    메서드의 파라미터인 list는 Heap에 할당 되어 있는 List를 가르킬 것이고,
    mySkill은 Heap 영역의 "java" String을 참조할 것이다.
    그리고 list 3번 index에 "python"이라는 값을 연결 시킨다.