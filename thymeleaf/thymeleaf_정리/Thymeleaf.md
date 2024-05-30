# 본문

### thymeleaf - 특징
    ● 서버 사이드 HTML 렌더링 (SSR)
        - 타임리프는 백엔드 서버에서 HTML을 동적으로 렌더링 하는 용도로 사용.
    ● 네츄럴 템플릿
        - 순수 HTML을 그대로 유지하면서 뷰 템플릿도 사용할 수 있는 타임리프의 특징을 네츄럴 템플릿이라 한다.
    ● 스프링 통합 지원
        - 스프링과 자연스럽게 통합되고, 스프링의 다양한 기능을 편리하게 사용할 수 있게 지원한다.

### thymeleaf - 기본식 표현
    ● 텍스트 - text, utext
        - 기본적으로 HTML 태그의 속성에 기능을 정의해서 동작한다.
        - HTML 컨텐츠에 th:text를 사용하면 된다.
        - 직접 데이터를 출력하고 싶으면 [[...]]를 사용하면 된다.

        @Controller
        @RequestMapping("/basic")
        public class BasicController {
            @GetMapping("/text-basic")
            public String textBasic(Model model) {
                model.addAttribute("data", "Hello Spring");
                return "basic/text-basic";
            }
        }
        
        <li>th:text 사용 <span th:text="${data}"></span></li>
        <li>컨텐츠 안에서 직접 출력하기 = [[${data}]]</li> 

    ● HTML Entity & Escape    
        - 웹 브라우저는 < 를 html 태그로 인식한다. 따라서 문자로 표현할 수 있는 방법이 필요한데 이것을 html 엔티티라 한다.
        - 그리고 이렇게 html에서 사용하는 특수 문자를 html 엔티티로 변경하는 것을 'escape'라 한다.
        - escape 기능을 사용하지 않으려면, th:inline="none"
        - 타임리프가 제공하는 이스케이프 문자
          - th:text 
          - [[...]]  
          - < -> &lt; 
          - > -> &gt;
    
    ● 변수 - SpringEL
        - 타임리프에서 변수를 사용할 때는 변수 표현식을 사용한다.
          - ${...} 

        @Data
        static class User {
            private String username;
            private int age;
        }

        @GetMapping("/variable")   
        public String variable(Model model) {

            User userA = new User("userA", 10);
            User userB = new User("userB", 20);

            List<User> list = new ArrayList<>();
            list.add(userA);
            list.add(userB);

            Map<String, User> map = new HashMap<>();
            map.put("userA", userA);
            map.put("userB", userB);

            model.addAttribute("user", userA);
            model.addAttribute("users", list);
            model.addAttribute("userMap", map);

            return "basic/variable";
        }

        <h1>SpringEL 표현식</h1>

        <ul>Object
            <li>${user.username} = <span th:text="${user.username}"></span></ul>
            <li>${user['username']} = <span th:text="${user['username']}"></span></li>
            <li>${user.getUsername()} = <span th:text="${user.getUsername()}"></span></li>
        </ul>

        <ul>List
            <li>${users[0].username} = <span th:text="${users[0].username}"></span></li>
            <li>${users[0]['username']} = <span th:text="${users[0]['username']}"></span></li>
            <li>${users[0].getUsername()} = <span th:text="$
            {users[0].getUsername()}"></span></li>
        </ul>

        <ul>Map
            <li>${userMap['userA'].username} =  <span th:text="$
            {userMap['userA'].username}"></span></li>
            <li>${userMap['userA']['username']} = <span th:text="${userMap['userA']
            ['username']}"></span></li>
            <li>${userMap['userA'].getUsername()} = <span th:text="$
            {userMap['userA'].getUsername()}"></span></li>
        </ul>

    ● 지역 변수 선언
        - th:with 를 사용하면 지역 변수를 선언해서 사용 가능.
        <h1>지역 변수 - (th:with)</h1> 
        <div th:with="first=${username[0]}">
            <span th:text="${first.usernam}">

    ● 자바8 날짜
        -타임리프는 날짜인 LocalDate, LocalDateTime, Instant를 사용한다.        
        - <span th:text="${#temporals.format(localDateTime, 'yyyy-MM-dd HH:mm:ss')}></span>

        @GetMapping("/date")
        public String date(Model model) {
            modle.addAttribute("localDateTime", LocalDateTime.now());
            return "basic/date";
        }

        <h1>LocalDateTime</h1>
        <ul>
            <li>default = <span th:text="${localDateTime}"></span></li>
            <li>yyyy-MM-dd HH:mm:ss = <span th:text="${$temporals.format(localDateTime, 'yyyy-MM-dd HH:mm:ss')}"></span></li>
        </ul>

    ● URL 링크
        - 타임리프에서 URL을 생성할 때는 @{...} 문법을 사용하면 된다.
        @GetMapping("/link")
        public String link(Model model) {
            model.addAttribute("param1", "data1");
            model.addAttribute("param2", "data2");
            return "basic/link";
        }

        <h1>URL 링크</h1>
        <ul>
            <li><a th:href="@{/hello}">basic url
            <li><a th:href="@{/hello/(param1=${param1}, param2=${param2})}">hello query param
            <li><a th:href="@{/hello/{param1}/{param2}(param1=${param1}, param2=${param2})}">path variable
            <li><a th:href="@{/hello/{param1}{param2}(param1=${param1}, param2=${param2})}">path variable + query parameter 
        </ul>

        - 쿼리 파라미터
          - @{/hello(param1=${param1}, param2=${param2})}
          - /hello?param1=data1&param2=data2
        - 경로 변수
          - @{/hello/{param1}/{param2}(param1=${param1}, param2=${param2})}
          - /hello/data1/data2
          - URL 경로상에 변수가 있으면 () 부분은 경로 변수로 처리된다.  
        - 경로 변수 + 쿼리 파라미터
          - @{/hello/{param1}(param1=${param1}, param2=${param2})}
          - /hello/data1?param2=data2  
          - 경로 변수와 쿼리 파라미터를 함께 사용할 수 있다.
    
    ● 리터럴
        - 리터럴은 코드상에 고정된 값을 말한다.
        - 타임리프에서 문자 리터럴은 항상 ' (작은 따옴표)로 감싸야 한다.
          - <span th:text="'hello'"> 
          - 그러나 항상 따옴표로 감싸는 일은 귀찮다. 그래서 공백 없이 쭉 이어진다면 하나의 의미있는 토큰으로 인지해서 생략 가능하다.
          
        <ul>  
            <li>'hello' + ' world!' = <span th:text="'hello' + ' world!'"></span></li>
            <li>'hello world!' = <span th:text="'hello world!'"></span></li>
            <li>'hello ' + ${data} = <span th:text="'hello ' + ${data}"></span></li>
            <li>리터럴 대체 |hello ${data}| = <span th:text="|hello ${data}|"></span></li>
        </ul>

    ● 반복
        - th:each 를 사용한다.    
         
        @GetMapping("/each")
        public String each(Model model) {
            addUsers(model);
            return "basic/each";
        }

        private void addUsers(Model model) {
            List<User> list = new ArrayList<>();
            list.add(new User("userA", 10));
            list.add(new User("userB", 20));
            list.add(new User("userC", 30));

            model.addAttribute("users", list);
        }

        <tr>
            <th>username
            <th>age
        </tr>
        <tr th:each="user : ${users}">
            <td th:text="${user.username}">username</td>            
            <td th:text="${user.age}">0</td>            
        </tr>

        - <tr th:each="user : ${users}">
        - 반복시 오른쪽 컬렉션(${users})의 값을 하나씩 꺼내서 왼쪽 변수(user)에 담아서 태그를 반복 실행한다.

### 타임리프 스프링 통합
    ● 타임리프 스프링 통합 추가 기능
        - 편리한 폼 관리를 위한 추가 속성
          - th:object(기능 강화, 폼 커맨드 객체 선택) 
          - th:field, th:errors, th:errorclass
        - 폼 컴포넌트 기능
          - checkbox, redio button, List
           
    ● 입력 폼 처리
        - th:object : 커맨드 객체를 지정한다.
        - *{...} : 선택 변수 식이라고 한다. th:object에서 선택한 객체에 접근
        - th:field
          - HTML 태그의 id, name, value 속성을 자동으로 처리해준다.
      
        - 렌더링 전
            <input type="text" th:field="*{itemName}" />
        - 렌더링 후
            <input type="text" id="itemName" name="itemName" th:value="*{itemName}" />