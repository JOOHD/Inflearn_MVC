dㅇ## 본문

### 검증 요구사항
    지금까지 만든 웹 애플리케이션은 폼 입력시 숫자를 문자로 작성하거나해서 검증 오류가 발생하면 오류 화면으로 이동한다. 이렇게 되면 사용자는 처음부터 해당 폼으로 다시 이동해서 입력을 해야 한다.
    아마다 이런 서비스라면 사용자는 금방 떠나버릴 것이다. 웹 서비스는 폼 입력시 오류가 발생하면, 고객이 입력한 데이터를 유지한 상태로 어떤 오류가 발생했는지 친절하게 알려주어야 한다.

    컨트롤러의 중요한 역할중 하나는 HTTP 요청이 검증하는 것이다. 

    ● 참고 : 클라이언트 검증, 서버 검증
    - 클라이언트 검증은 조작할 수 있으므로 보안에 취약한다.
    - 서버만으로 검증하면, 즉각적인 고객 사용성이 부족하다.
    - 둘을 적절히 섞어서 사용하되, 최종적으로 서버 검증은 필수
    - API 방식을 사용하면 API 스펙을 잘 정의해서 검증 오류를 API 응답 결과에 잘 넘겨주어야 한다.

![validation1](./validation/validation_img/validation1.png)
    - 사용자가 상품 등록 폼에서 정상 범위의 데이터를 입력하면, 서버에서는 검증로직이 통과하고, 상품을 저장하고, 상품 상세 화면으로 redirect한다.

![validation2](./validation/validation_img/validation2.png)
    - 고객이 상품 등록 폼에서 상품명을 입력하지 않거나, 가격, 수량 등이 너무 작거나 커서 검증 범위를 넘어서면, 서버 검증 로직이 실패해야 한다.
    이렇게 검증에 실패한 경우 고객에게 다시 상품 등록 폼을 보여주고, 어떤 값을 입력한지 알려주어야 한다.

### validationItemControllerV1 - addItem() 수정
    @PostMapping("/add")    
    public String addItem(@ModelAttribute Item item, RedirectAttributes redirectAttributes, Model model) {

        // 검증 오류 결과를 보관
        Map<String, String> errors = new HashMap<>();

        // 검증 로직
        if(@StringUtils.hasText(item.getItemName())) {
            errors.put("itemName", "상품 이름은 필수입니다.");
        }

        - 검증시 오류가 발생하면 errors에 담아둔다. 이때 어떤 필드에서 오류가 발생했는지 구분하기 위해 오류가 발생한 필드명을 key로 사용한다. 이후 뷰에서 이 데이터를 사용해서 고객에게 친절한 오류 메시지를 출력할 수 있다.

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            errors.put("price", "가격은 1,000 ~ 1,000,000 까지 허용합니다.");
        }

        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            errors.put("quantity", "수량은 최대 9,999 까지 허용됩니다.");
        }

        // 특정 필드가 아닌 복합 룰 검증
        if (item.getPirce() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPirce < 10000) {
                errors.put("globalError", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPirce);
            }
        }

        - 특정 필드를 넘어서는 오류를 처리해야 할 수도 있다. 이때는 필드 이름을 넣을 수 없으므로 globalError 라는 key를 사용한다.

        // 검증에 실패하면 다시 입력 폼으로
        if (!errors.isEmpty()) {
            model.addAttribute("errors", errors);
            return "validation/v1/addForm";
        }

        - 만약 검증에서 오류 메시지가 하나라도 있으면 오류 메시지를 출력하기 위해 model 에 errors 를 담고, 입력 폼이 있는 뷰 템플릿으로 보낸다.
        // 성공 로직
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect://validation/v1/items/{itemId}";
    }

    v1/addForm.html
    <form action="item.html" th:action th:object="${item}" method="post">
        <div th:if="${errors?.containskey('globalError')}>
            <p class="field-error" th:text="${errors{'globalError'}}">전체 오류 메시지</>
        </div>

        <div>
            <label for="itemName" th:text="#{label.item.itemName}">상품명</>
            <input type="text" 
                   id="itemName" th:field="*{itemName}"
                   th:class="${errors?.containskey('itemName')} ? 'form-controlfield-error' : 'form-control'"
            class="form-control" placeholder="이름을 입력하세요">
            <div class="field-error" th:if="${errors?.containskey('itemName')}" th:text="${errors['itemName']}">
        </div>

    ● 참고 safe navigation operator
    만약 여기에서 errors가 null 이라면 어떻게 될까?
    등록폼에 진입한 시점에는 errors가 없다.
    errors.containKey() 를 호출하는 순가 NullPointerException 이 발생.

    error?. 은 error가 null 일때, NullPointerException이 발생하는 대신, null을 반환하는 문법이다.
    th:if 에서 null 은 실패로 처리되므로 오류 메시지가 출력되지 않는다.

    ● 필드 오류 처리
    <input type="text" th:classappend="${errors?.containsKey('itemName')} ? 'field-error' : _" class="form-control">

        - classappend를 사용해서 해당 필드에 오류가 있으면 field-error 라는 클래스 정보를 더해서 폼의 색깔을 빨간색으로 강조한다. 만약 값이 없으면
        _(Nd-Operation)을 사용해서 아무것도 하지 않는다.

    ● 필드 오류처리 - 메시지
    <div class="field-error" th:if="${errors?.containkey('itemName')}"
    th:text="${errors['itemName']}"> 상품명 오류

    ● 정리
    - 만약 검증 오류가 발생하면 입력 폼을 다시 보여준다.
    - 검증 오류들을 고객에게 친절하게 안내해서 다시 입력할 수 있게 한다.
    - 검증 오류가 발생해도 고객이 입력한 데이터가 유지된다.
  
    ● 남은 문제점
    1) 뷰 템플릿에서 중복 처리가 많다. 뭔가 비슷하다.
    2) 타입 오류 처리가 안된다. Item의 price, quantity 같은 숫자 필드는 타입이 Integer 이므로 문자 타입으로 설정하는 것이 불가능하다. 숫자 타입에 문자가 들어오면 오류가 발생한다. 그런데 이러한 오류는 스프링MVC에서 컨트롤러에 진입하기도 전에 예외가 발생하기 때문에, 컨트롤러가 호출되지 않고, 400예외가 발생하면서 오류 페이지를 띄워준다.
    3) Item의 price에 문자를 입력하는 것 처럼 타입 오류가 발생해도 고객이 입력한 문자를 화면에 남겨야 한다. 만약 컨트롤러가 호출된다고 가정해도
    Item의 price는 Integer이므로 문자를 보관할 수가 없다.
    결국 문자는 바인딩이 불가능하므로 고객이 입력한 문자가 사라지게 되고, 고객은 본인이 어떤 내용을 입력해서 오류가 발생했는지 이해하기 어렵다.
    4) 결국 고객이 입력한 값도 어딘가에도 별도로 관리가 되어야 한다.
 
### ValidationItemControllerV2 - addItemV2 (BindingResult)
    @PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다."));
        }
    }

    ● addItemV1 와 비교 
    - errors -> bindingResult 로 바꾼다.
  
    ● 필드 오류 - FieldError
    bindingResult.addError(new FieldError("item", "itemName", "상품~~"));
        - public FieldError(String objectName, String field, String defaultMessage) {}
            - objectName : @ModelAttribute 이름
            - field : 오류가 발생한 필드 이름
            - defaultMessage : 오류 기본 메시지
    
    ● 글로벌 오류 - ObjectError
    bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
        - public ObjectError(String objectName, String defaultMessage) {}
            - objectName : @ModelAttribute의 이름
            - defaultMessage : 오류 기본 메시지
    
    v2/addForm.html
    <form action="item.html" th:action th:object="${item}" method="post">
        <div th:if="${#field.hasGlobalErrors()}">
            <p class="field-error" 
               th:each="err : ${#fields.globalErrors()}" 
               th:text="${err}">글로벌 오류 메시지</>
            </p>   
        </div>
        
        <div>
            <label for="intemName" th:text="#{label.item.itemName}"> 상품명</>
            <input type="text" id="itemName" 
            th:field="*{itemName}"     
            th:errorclass="field-error" 
            class="form-control" >               
        </div>

        <div class="field-error" 
            th:errors="*{itemName}">상품명 오류
        </div>

        ● v1/addForm.html 과 비교
        - ${errors?.containskey('globalError')} -> "${#fields.hasGlobalErrors()}, errors?. 코드가 #fields. 로 바뀌었다.
      
        ● 코드 분석 및 흐름
        1) 입력 필드 바인딩 : 'th:field="*{itemName}"'는 모델의 'itemNanme' 필드와 바인딩되어 있다. 사용자가 이 필드에 값을 입력하면 서버로 전송된다.
        2) 검증 오류 발생 시 'th:errorclass' 자동 : 폼이 제출되고 서버 측에서 검증을 수행할 때, 'itemName' 필드에 오류가 발생하면 'th:errorclass="field-error"'가 작동하여 'input' 태그에 'field-error' 클래스가 추가 된다. 이를 통해 css로 오류 필드를 시각적으로 강조할 수 있다.
        3) 검증 오류 메시지 출력 : 'th:error="*{itemName}"' 속성은 'itemName' 필드에 검증 오류가 있는 경우에만 해당 'div' 태그를 렌더링한다. 오류 메시지를 해당 'div' 태그 안에 표시할 수 있다.
     
            ●  정리
            1) th:field 는 모델 객체의 필드와 바인딩 된다.
            2) th:errorclass 는 검증 오류가 있는 경우 지정된 클래스를 추가하여 시각적으로 강조.
            3) th:errors 는 해당 필드에 오류가 있는 경우 오류 메시지를 출력
            
            따라서 입력 필드에 오류가 발생하면 'th:errorclass'가 작동하여 'field-error' 클래스를 추가하고, 'th:errors' 속성이 있는 'div' 태그가 렌더링 되어 오류 메시지를 표시하게 된다. 이를 통해 사용자는 어떤 필드에 오류가 생겼는지 명확하게 알 수 있따.

        ● 타임리프 스프링 검증 오류 통합 기능
        - 타임리프는 스프링의 BindingResult 를 활용해서 편리하게 검증 오류를 표현하는 기능을 제공한다.
        - #fields : #fields 로 BindingResult 가 제공하는 검증 오류에 접근할 수 있다.
        - th:errors : 해당 필드에 오류가 있는 경우에 태그를 출력한다. th:if의 편의 버젼이다.
        - th:errorclass : th:field에서 지정한 필드에 오류가 있으면 class 정보를 추가한다.
      
        ● 글로벌 오류 처리
        <div th:if="${#fields.hasGlobalErrors()}">
            <p class="field-error" th:each="err : ${#fields.globalErrors()}" th:text="${err}">전체 오류 메시지</>
        
        ● 필드 오류 처리
        <input type="text" id="itemName" th:field="*{itemName}"
            th:errorclass="field-error" 
            class="form-control" placeholder="이름을입력하세요">상품명 오류
        </div>