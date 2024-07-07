## 본문

### 메시지
    ● 정의
    여러 화면에 보이는 상품명, 가격, 수량 등, label에 있는 단어를 변경하려면 다음 화면들을 다 찾아가면서 모두 변경해샹 한다. 화면 수가 적으면 문제가 되지 않지만 화면이 수십개 이상이라면 수십개의 파일을 모두 고쳐야 한다.
    해당 HTML 파일에 메시지가 하드코딩 되어 있기 때문이다.

    이런 다양한 메시지를 한 곳에서 관리하도록 하는 기능을 메시지 기능이라 한다.

    ex) message.properties
        item = 상품
        item.id = 상품 ID
        item.itemName = 상품명
        item.price = 가격
        item.quantity = 수량

        각 HTML 들은 다음과 같이 해당 데이터를 Key 값으로 불러서 사용한다.
        addForm.html
        <label fo="itemName" th:text="#{item.itemName}"></>

### 국제화
    ● 정의
    메시지에서 설명한 메시지 파일(message.properties)을 각 나라별로 별도로 관리하면 서비스를 국제화 할 수 있다.

    ex) message_en.properties
        item = Item
        item.id = Item ID
        item.itemName = Item Name
        item.price = price
        item.quantity = quantity

    이렇게 영어를 사용하면 영어로, 한국어면 한국어로 설정하여 사용하면 된다.

    한국에서 접근한 것인지 영어에서 접근한 것인지는 인식하는 방법은 HTTP accept-language 헤더 값을 사용하거나 사용자가 직접 언어를 선택하도록 하고, 쿠키 등을 사용해서 처리하면 된다.

    메시지와 국제화 기능을 직접 구현할 수도 있겠지만, 스프링은 기본적인 메시지와 국제화 기능을 모두 제공한다. 
    그리고 타임리프도 스프링이 제공하는 메시지와 국제화 기능을 편리하게   통합해서 제공한다.

### Spring 메시지 소스 설정
    메시지 관리 기능을 사용하려면 스프링이 제공하는 MessageSource를 스프링 빈으로 등록하면 되는데, MessageSource는 인터페이스이다. 
    따라서 구현체인 ResourceBundleMessageSource를 스프링 빈으로 등록하면 된다.

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource meesageSource = new ResourceBundleMessageSource();

        messageSource.setBasenames("messages", "errors");
        messageSource.setDefaultEncoding("utf-8");
        return messageSource;
    }    

    - basename : 설정 파일의 이름을 지정한다.
      - message로 지정하면 messages.properties 파일을 읽어서 사용한다.
      - 추가로 국제화 기능을 적용하려면 message_en.properties, message_ko.properties와 같이 파일명 마지막에 언어 정보를 주면된다. 만약 찾을 수 있는 국제화 파일이 없으면 message.properties(언어 정보가 없는 파일명)를 기본으로 사용한다.
      - 파일의 위치는 /resources/message.properties에 두면 된다.
      - 여러 파일을 한번에 지정할 수 있다. 여기서는 message, errors 둘을 지정
    - defaultEncoding : 인코딩 정보를 지정한다. utf-8을 사용
  
    ● 스프링 부트를 사용하면 스프링 부트가 MessageSource를 자동으로 스프링 빈으로 등록한다.

    ● 스프링 부트 메시지 소스설정
    스프링 부트를 사용하면 다음과 같이 메시지 소스를 설정할 수 있다.
        spring.message.basename=messages, config.i18n.messages

    ● 스프링 부트 메시지 소스 기본 값
        spring.messages.basename=messages

        MessageSource를 스프링 빈으로 등록하지 않고, 스프링 부트와 관련된 별도의 설정을 하지 않으면 messages라는 이름으로 기본 등록된다.
        따라서 message_en.properties, messages_ko.properties 파일만 등록하면 자동으로 인식된다.

### 스프링 메시지 소스 사용
    ● MessageSource interfafce
    public interface MessageSource {

        String getMessage(String code, @Nullable Object[] args, @Nullable String defaultMessage, Locale locale);

        String getMessage(String code, @Nullable Object[] args, Locale locale) throws NoSuchMessageException;
    }        

    MessageSource 인터페이스를 보면 코드를 포함한 일부 파라미터로 메시지를 읽어오는 기능을 제공한다.

### 웹 어플리케이션에 메시지 적용하기
    ● messages.properties
        label.item=상품
        label.item.id=상품 ID
        label.item.itemName=상품명
        label.item.price=가격
        label.item.quantity=수량
        page.items=상품 목록
        page.item=상품 상세
        page.addItem=상품 등록
        page.updateItem=상품 수정
        button.save=저장
        button.cancel=취소  

    ● 타임리프 메시지 적용
    타임리프의 메시지 표현식 #{...}를 사용하면 스프링 메시지를 편리하게 조회.
    ex) #{label.item}이라고 하면 된다.

        - 렌더링 전
        <div th:text="#{label.item}"></>
        - 렌더링 후
        <div>상품</>

    ● addForm.html
    <html xmlns:th="http"//www.thymeleaf.org">
    <head>
        <meta charset="utf-8">
        <lind th:href="@{/css/bootstrap.min.css}"
            href="../css/bootstrap.min.css" rel="stylesheet">
    </head>

        <h2 th:text="#{page.addItem}">상품 등록</>
        <h4 class="mb-3">상품 입력</>

        <form action="item.html" th:action th:object="${item}" method="post">
            <div>
                <label for="itemName" th:text="#{label.item.itemName}">상품명</>
                <input type="text" id="itemName" th:field="*{itemName}">
            </>
        </>

        <button type="submit" th:text="#{button.save}">저장</>
        <button onclick="location.href='items.html'
                th:onclick="|location.href='@{/message/items}'|"
                type="button" 
                th:text="#{button.cancel}">취소</>

    - 위 addForm.html 코드를 application.properties의 설정 메시지와 #{}를 적용하여 렌더링하게 되면 application.properties의 문자가 나오게 된다.

### 메시지 설정 파일 작성
    ● properties 설정

      # message.properties
      label.item.itemName=상품명
      label.item.price=가격
      label.item.quantity=수량
      button.save=저장
      button.cancel=취소

      # error.properties
      required.itemName=상품명은 필수 입력 항목입니다.
      required.price=가격은 필수 입력 항목입니다.
      range.price=가격은 0 이상이어야 합니다.
      required.quantity=수량은 필수 입력 항목입니다.
      range.quantity=수량은 0 이상이어야 합니다.
      global.error=글로벌 오류가 발생했습니다.
      totalPriceMin=총 가격은 최소 {0}운 이상이어야 합니다.(현재가격: {1}원)
    
    ● 스프링 설정 파일 작성
    # application.properties
    spring.messages.basename=message, error

    ● Configuration class
    @Configuration
    pubilc class WebConfig implements WebMvcConfigurer {
        @Bean
        public ResourceBundleMessageSource messageSource() {
            messageSource.setBasenames("message", "error");
            messageSource.setDefaultEncoding("UTF-8");
            return messageSource;
        }
    }

    @Controller
    @RequestMapping("/items")
    public class ItemController {

        @Autowired
        private MessageSource messageSource;

        @GetMapping("/new")
        public String newItemForm(Model model) {
            return "itemForm";
        }

        @PostMapping("/save")
        public String saveItem(
                               @RequestParam String itemName, 
                               @RequestParam Integer price,
                               @RequestParam Integer quantity,
                               Model model,
                               Locale locale) {

            if (itemName == null || itemName.isEmpty()) {
                model.addAttribute("error", messageSource.getMessage("required.itemName", locale));
                return "itemForm";
            }

            if (price == null || price <= 0) {
                model.addAttribute("error", messageSource.getMessage("range.price", locale));
                return "itemForm";
            }

            if (quantity == null || quantity <= 0) {
                model.addAttribute("error", messageSource.getMessage("range.quantity", locale));
                return "itemForm";
            }

            // 아이템 저장 로직
            return "redirect:/items";
        }
    }

    <html xmlns:th="http://www.thymeleaf.org">
    <head>
        <meta charset="UTF-8">
        <title th:text="#{page.updateItem}">상품 수정</>
        <link rel="stylesheet" th:href="@{/css/bootstrap.min.css}">
    </>
    <body>
    <div>
        <h2 th:text="#{page.updateItem}">상품 수정</>
        <form action="#" th:action="@{/items/save}" th:object="${item}" method="post">
            <idv th:if="${error}" class="alert alert-danger" th:text="${error}"></>
            <div>        
                <label for="itemName" th:text="#{label.item.itemName}">상품명</>
                <input type="text" id="itemName" th:field="*{itemName}" class="form-control" th:errorclass="field-error">
                <div class="field-error" th:errors="*{itemName}">상품명 오류</>
            </>
            <button type="submit" class="btn btn-primary" th:text="#{button.save}">저장</button>
            <button type="button" class="btn btn-secondary" onclick="location.href='/items'" th:text="#{button.cancel}">취소</button>
        </>
    </>

    1) 메시지 설정 파일 작성  : 'message.properties'와 'error.properties' 파일에 메시지를 정의
    2) 스프링 설정 파일 작성 : 'application.properties'에서 메시지 소스를 설정
    3) 글로벌 메시지 설정 : 'WebConfig' 클래스에서 'ResourceBundleMessageSource'를 빈으로 등록
    4) 메시지 사용
          1) 컨트롤러에서 사용 : 'messageSource' 빈을 주입받아 메시지를 가져오고, 검증 로직에서 메시지를 사용한다.
          2) 타임리프 템플릿에서 사용 : 'th:text'와 th:errors'등을 사용하여 메시지를 출력한다.

    - 이와 같은 방식으로 글로벌 메시지와 오류 메시지를 중앙에서 관리하고, 다국어 지원이 가능 하도록 구현할 수 있다.                   
