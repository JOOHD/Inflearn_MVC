## 본문

### BeanValidation - 소개
    public class Item {

        private Long id;

        @NotBlank : 빈값 + 공백만 있는 경우를 허용하지 않는다.
        private String itemName;

        @NotNull  : null 허용하지 않는다.
        @Range(min = 1000, max = 1000000) 
        private Integer price;

        @NotNull
        @Max(9999)
        private Integer quantity;
        //...
    }

    ● BeanValidation 
    이런 검증 로직을 모든 프로젝트에 적용할 수 있게 공통화하고, 표준화 한 것이 BeanValidation 이다.
    먼저 BeanValidation 은 특정한 구현체가 아니라 검증 어노테이션과 여러 인터페이스의 모음이다. 마치 JPA가 표준 기술이고 그 구현체로 하이버네이트가 있는 것과 같다.

    ● 스프링 부트는 자동으로 글로벌 validator를 사용?
    LocalValidatiorFactoryBean을 글로벌 Validator로 등록한다. 이 Validator는 @NotNull 같은 어노테이션을 보고 검증을 수행한다. 이렇게 글로벌 Validator가 적용되어 있기 때문에, @Valid, @Validated만 적용하면 된다.
    검증 오류가 발생하면, FieldError, ObjectError를 생성해서 BindingResult에 담아둔다.

        ● 주의!
        Validator를 직접 등록하면 스프링 부트는 Bean Validator를 글로벌 Validator로 등록하지 않는다. 땨라서 어노테이션 기반의 빈 검증기가 동작하지 않는다. 

        ● 참고
        검증시 @Validated, @Valid 둘다 사용가능하다.
        @Validated는 스프링 전용 검증 어노테이션이고, @Valid는 자바 표준 검증 어노테이션이다. 두 기능 모두 동일하게 작동하지만, @Validated는 내부에 group 라는 기능을 포함하고 있따.

    ● 검증 순서
    1.@ModelAttribute 각각의 필드에 타입 변환 시도
        1) 성공하면 다음으로
        2) 실패하면 typeMismatch로 FieldError 추가
    2. Validator 적용
     
        ● 바인딩에 성공한 필드만 Bean Validation 적용
        BeanValidator는 바인딩에 실패한 필드는 BeanValidation을 적용하지 않는다. 타입 변환에 성공해서 바인딩에 성공한 필드여야 BeanValidation 적용이 의미가 있다.(모델 객체에 바인딩 받는 필드 값이 정상으로 들어와야 검증도 의미가 있다.)

        - @ModelAttribute -> 각각의 필드 타입 변환시도 -> 변환에 성공한 필드만 BeanValidation 적용
            ex) 
                - itemName에 문자 'A'입력 -> 타입 변환 성공 -> itemName 필드에 BeanValidation 적용
                - price에 문자 'A'입력 -> 'A'를 숫자 타입 변환 시도 실패 -> typeMissmatch FieldError 추가 -> price 필드는 BeanValidation 적용 x

    ● BeanValidation - 에러 코드
    - 오류 메시지를 좀 더 자세히 변경하고 싶으면 어떻게 해야 할끼?
      - NotBlan 라는 오류 코드를 기반으로 MessageCodesResolver를 통해 다양한 메시지 코드가 순서대로 생성된다.

      @NotBlank
        - NotBlank.item.itemName       
        - NotBlank.itemName
        - NotBlank.java.lang.String
        - NotBlank

    ● 메시지 등록
    errors.properties
    #Bean Validation 추가
    NotBlank={0} 공백x
    Range={0}, {2} ~ {1} 허용
    Max={0}, 최대 {1}

    - {0}은 필드명이고, {1}, {2}..은 각 어노테이션 마다 다르다.
    
    ● BeanValidation 메시지 찾는 순서
    1. 생성된 메시지 코드 순서대로 messageSource에서 메시지 찾기
    2. 어노테이션의 message 속성 사용 -> @NotBlank(message="공백! {0}")
        ex)
            @NotBlank(message = "공백은 입력할 수 없습니다.")
            private String itemName;
    3. 라이브러리가 제공하는 기본 값 사용 -> 공백일 수 없습니다.
 
### ValidationItemControllerV3 - edit() 변경
    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute Item item, BindingResult bindingResult) {

        // 특정 필드 예외가 아닌 전체 예외
        if(item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000) {
                bindingtResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        if(bindingResult.hasErrors()) {
            return "validation/v3/editForm";
        }

        itemRepository.update(itemId, item);
        return "redirect:/validaton/v3/items/{itemId}";
    }

    - 검증 오류가 발생하면 editForm으로 이동하는 코드 추가.
  
    ● validation/v3/editForm.html 변경
    <!DOCTYPE HTML>
    <html xmlns:th="http://www.thymeleaf.org">
    <head>
        <meta charset="utf-8">
        <link th:href="@{/css/bootstrap.min.css}"
                 href="../css/bootstrap.min.css" rel="stylesheet">
        <style>
        .container {
            max-width: 560px;
        }
        .field-error {
            border-color: #dc3545;
            color: #dc3545;
        }
        </style>
    </head>
    
    <body>
    <div class=""container">
        <h2 th:text="#{page.updateItem}">상품 수정</>
    </>

    <form action="item.html" th:action th:object="${item}" method="post">
        <div th:if="${#fields.hasGlobalErrors()}">
            <p class="field-error" th:each="err : ${#fields.globalErrors()}" th:text="${err}"> 글로벌 오류 메시지</>        
        </>

        <div>
            <label for="price" th:text="#{label.item.price}">가격</>
            <input type="text" 
                   id="price" 
                   th:field="*{price}" 
                   th:errorclass="field-error" 
                   class="form-control">
            <div class="field-error" th:errors="*{price}">가격 오류</>
        </>            

        <button class="~" type="submit" th:text="#{button.save}">저장</>

        <button class="~"
                onclick="location.href='item.html'"
                th:onclick="|location.href='@{/validation/v3/items/{itemId}(itemId=${item.id})}'|"
                type="button" th:text="#{button.cancel}">취소
        </>
    </>        

    - .field-error, css 추가.
    - 글로벌 오류 메시지.
    - 상품명, 가격, 수량 필드에 검증 기능 추가.
  
### BeanValidation - 힌계
    ● 데이터를 등록할 때와 수정할 때는 요구사항이 다를 수 있다. 
    
    등록시 기존 요구사항
        - 타입 검증
          - 가격, 수량에 문자가 들어가면 검증 오류 처리
        - 필드 검증
          - 상품명 : 필수, 공백x
          - 가격 : 1000원 이상, 1백만원 이하
          - 수량 : 최대 9999
        - 특정 필드의 범위를 넘어서는 검증
          - 가격 * 수량의 합은 10,000원 이상.
    
    수정시 요구사항
    - 등록시에는 quantity 수량을 최대 9999R까지 등록할 수 있지만 수정시에는 수량을 무제한으로 변경할 수 있다.
    - 등록시에는 id에 값이 없어도 되지만, 수정시에는 id값이 필수이다.
    
    ● 참고
    domain 객체에 item의 id(@NotNull) 값은 항상 들어있도록 로직이 구성되어 있다. 그래서 검증하지 않아도 된다고 생각할 수 있다. 그러나 HTTP 요청은 언제든지 악의적으로 변경해서 요청할 수 있으므로 서버에서 항상 검증해야 한다. 예를 들어서 HTTP 요청을 변경해서 item의 id 값을 삭제하고 요청할 수도 있다. 따라서 최종 검증은 서버에서 진행하는 것이 안전하다.

    ● 문제
    등록시 화면이 넘어가지 않으면서 다음과 같은 오류를 볼 수 있다.
    id : rejected value [null];
    왜냐하면 등록시에는 id에 값이 없다. 따라서 @NotNull id를 적용한 것 때문에 검증에 실패하고 다시 폼 화면으로 넘어온다. 결국 등록 자체도 불가능하고, 수량 제한도 걸지 못한다.

    결과 적으로 item은 등록과 수정에서 검증 조건의 충돌이 발생하고, 등록과 수정은 같은 BeanValidation을 적용할 수 없다. 
    이 문제를 어떻게 해결할 수 있을까?

    ● 방법
    1) BeanValidation의 groups 기능을 사용한다.
    2) Item을 직접 사용하지 않고, ItemSaveForm, ItemUpdateForm 같은 폼 전송을 위한 별도의 모델 객체를 만들어서 사용한다.
    
    ● BeanValidation groups 기능 사용
    이런 문제를 해결하기 위해 BeanValidation 은 group라는 기능을 제공한다.
    예를 들어서 등록시 검증할 기능과 수정시에 검증할 기능을 각각 그룹으로 나누어 적용할 수 있다.

        ● Interface 생성
        - 저장용 group 
        - public interface SaveCheck {}

        - 수정용 group 
        - public interface UpdateCheck {} 

        ● item - group 적용
        @Data
        public class Item {

            @NotNull(groups = UpdateCheck.class) // 수정시에만 적용
            private Long id;

            @NotBlank(groups = {SaveCheck.class, UpdateCheck.class})
            private String itemName;

            @NotNull(groups = {SaveCheck.class, UpdateCheck.class})
            @Range(min = 1000, max = 1000000, groups = {SaveCheck.class, UpdateCheck.class})
            private Integer price;

            @NotNull(groups = {SaveCheck.class, UpdateCheck.class})
            @Max(value = 9999, groups = SaveCheck.class) // 등록시에만 적용
            private Integer quantity;
        }

        ● ValidationItemControllerV3 - 저장 로직에 SaveCheck Group 적용
        @PostMapping("/add")
        public String addItemV2(@Validated(SaveCheck.class) @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirecteAttributes) {}
        - addItemV2() 생성, SaveCheck.class 적용
        
        ● 참고 @Valid에는 group를 적용할 수 없다. 사실, group 기능은 잘 사용되지 않는다. 그 이유는 실무에서는 주로 다음에 등장하는 등록용 폼 객체와 수정용 폼 객체를 분리해서 사용하기 때문이다.

### Form 전송 객체 분리 
    실무에서는 groups를 잘 사용하지 않는데, 그 이유가 다른 곳에 있다. 바로 등록시 폼에서 전달하는 데이터가 Item 도메인 객체와 딱 맞지 않기 때문이다.

    실무에서는 회원 등록시 회원과 관련된 데이터만 전달받은 것이 아니라 약관 정보도 추가로 받는 등 Item과 관계없는 수 많은 부가 데이터가 넘어온다.
    그래서 보통 Item을 직접 전달받는 것이 아니라, 복잡한 폼의 데이터를 컨트롤러까지 전달할 별도의 객체를 만들어서 전달한다. 

    예를 들면 ItemSaveForm이라는 폼을 전달하는 전용 객체를 만들어서 @ModelAttribute로 사용한다. 이것을 통해 컨트롤러에서 폼 데이터를 전달 받고, 이후 컨트롤러에서 필요한 데이터를 사용해서 Item을 생성한다.

        ● 폼 데이터 전달을 위한 별도의 객체 사용
        HTML Form -> ItemSaveForm -> Controller -> Item 생성 -> Repository
            - 장점 : 전송하는 폼 데이터가 복잡해도 거기에 맞춘 별도의 폼 객체를 사용해서 데이터를 전달 받을 수 있다. 보통 등록과, 수정용으로 별도의 폼 객체를 만들기 때문에 검증이 중복되지 않는다.
            - 단점 : 컨트롤러에서 Item 객체를 생성하는 변환 과정이 추가된다.

    ● 기존 ITEM
    @Data
    public class Item {
        private Long id;
        private String itemName;
        private Integer price;
        private Integer quantity;
    }

    ● ItemSaveForm - 저장용 
    @Data
    public class ItemSaveForm {
        @NotBlank
        private String itemName;
        @NotNull
        @Range(min = 1000, max = 1000000)
        private Integer price;
        @NotNull
        @Max(value = 9999)
        private Integer quantity;
    }

    ● ItemUpdateForm - 수정용
    @Data
    public class ItemUpdateForm {
        @NotNull
        private Long id;
        @NotBlank
        private String itemName;
        @NotNull
        @Range(min = 1000, max = 1000000)
        private Integer price;
        //수정에서는 수량은 자유롭게 변경할 수 있다.
        private Integer quantity;
    }

    ● VlidationItemControllerV4
    @Slf4j
    @Controller
    @RequestMapping("/validation/v4/items")
    @RequiredArgsConstructor
    public class ValidationItemControllerV4 {

        private final ItemRepository itemRepository;

        @GetMapping
        public String items(Model model) {
            List<Item> items = itemRepository.findAll();
            model.addAttribute("items", items);
            return "validation/v4/items";
        }

        @GetMapping("/{itemId}")
        public String item(@PathVariable long itemId, Model model) {
            Item item = itemRepository.findById(itemId);
            model.addAttribute("item", item);
            return "/validation/v4/item";
        }

        @PostMapping("/add")
        public String addItem(@Validated @ModelAttribute("item") ItemSaveForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

            // 특정 필드 예외가 아닌 전체 예외
            if (form.getPrice() != null && form.getQuantity() != null) {
                int resultPrice = form.getPrice() * form.getQuantity();
                if(resultPrice < 10000) {
                    bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
                }
            }

            if (bindingResult.hasErrors()) {
                return "/validation/v4/addForm";
            }

            // 성공 로직
            Item item = new Item();
            item.setItemName(form.getItemName());
            item.setPrice(form.getPrice());
            item.setQuantity(form.getQuantity());

            Item savedItem = itemRepository.save(item);
            redirectAttributes.addAttribute("itemId", savedItem.getId());
            redirectAttributes.addAttribute("status", true);
            return "redirect:/validation/v4/items/{itemId}";
        }
    }