# 본문

### 서비스 제공 흐름
![service_flow](./servlet_img/service_flow.png)

### Item - 상품 객체
    @Data
    public class Item {

        private Long id;
        private String itemName;
        private Integer price;
        private Integer quantity;
    }

### ItemRepository - 상품 저장소
    @Repository
    public class ItemRepoitory {

        private static final Map<Long, Item> store = new HashMap<>();
        private static long sequence = 0L;

        public Item save(Item item) {
            item.setId(++sequence);         // primary key
            store.put(item.getId(), item);  // insert
            return item;
        }

        public Item findById(Long id) {
            return store.get(id);
        }

        public List<Item> findAll() {
            return new ArrayList<>(store.values());
        }

        public void update(Long itemId, Item updateParam) {
            Item findItem = findById(itemId);
            findItem.setItemName(updateParam.getItemName());
            findItem.setPrice(updateParam.getPrice());
            findItem.setQuantity(updateParam.getQuantity());
        }

        public void clearStore() {
            store.clear();
        }
    }

## 상품 목록 - thymeleaf    
### BasicItemController
    @Controller
    @RequestMapping("/basic/items")
    @RequiredArgsConstructor
    public class BasicItemController {

        private final ItemRepository itemRepository;

        @GetMapping
        public String itmes(Model model) {
            List<Item> items = itemRepository.findAll();
            model.addAttribute("items", items);
            return "basic/items";
        }
    }

    ● 컨트롤러 로직은 itemRepository에서 모든 상품을 조회한 다음에 모델에 담는다. 그리고 뷰 템플릿을 호출한다.

    ● @RequiredArgsConstructor
        -final이 붙은 멤버변수만 사용해서 생성자를 자동으로 만들어준다.

        pupblic BasicItemController(ItemRepository itemRepository) {
            this.itemRepository = itemRepository;
        }

        ● 이렇게 생성자가 딱 1개만 있으면 스프링이 해당 생성자에 @Autowired로 의존관계를 주입.
        ● 따라서 final 키워드를 빼면 안된다. 그러면 ItmeRepository 의존관계가 주입이 안된다.

