## 본문

### SpringBoot - paging(@PageableDefault 적용)

### 1.Item Entity
    @Entity
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class Item {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        private String description;

        private double price;

        private int stock;
    }

### 2.ItemRepository interface
    @Repository
    public interface ItemRepository extends JpaRepository<Item, Long> {

        // Optional: 검색어로 아이템 필터링 기능을 추가할 수 있습니다.
        Page<Item> findByNameContaining(String keyword, Pageable pageable);
    }

### 3.ItemService interface
    public interface ItemService {
        Page<Item> getItems(Pageable pageable);
        Page<Item> searchItems(String keyword, Pageable pageable);
    }

### 4.ItemServiceImpl
    @Service
    public class ItemServiceImpl implements ItemService {

        private final ItemRepository itemRepository;

        @Autowired
        public ItemServiceImpl(ItemRepository itemRepository) {
            this.itemRepository = itemRepository;
        }

        @Override
        public Page<Item> getItems(Pageable pageable) {
            return itemRepository.findAll(pageable);
        }

        @Override
        public Page<Item> searchItems(String keyword, Pageable pageable) {
            return itemRepository.findByNameContaining(keyword, pageable);
        }
    }

### 5.ItemController
    @RequireArgsConstructor
    @Controller
    public class ItemController {

        private final ItemService itemService;

        @GetMapping("/itemss")
        public String listItems(@PageableDefault(
                                    page = 0, 
                                    size = 10, 
                                    sort = "id", 
                                    direction = Sort.Direction.ASC) 
                                    Pageable pageable,
                                @RequestParam(required = false) 
                                    String searchKeyword,
                                Model model) 
        {
            Page<Item> items;
            if(searchKeyword != null && !searchKeyword.isEmpty()) {
                items = itemService.searchItems(searchKeyword, pageable);
            } else {
                items = itemService.getItems(pageable);
            }

            model.addAttribute("items", items);
            model.addAttribute("currentPage", pageable.getPageNumber());
            model.addAttribute("totalPages", items.getTotalPages());
            model.addAttribute("searchKeyword", searchKeyword);
            
            return "itemList"; // Thymeleaf 템플릿 이름 (예: itemList.html)
        }
    }    

### 6.itemList.html (Thymleaf 템플릿)    
    <!DOCTYPE html>
    <html xmlns:th="http://www.thymeleaf.org">
    <head>
        <title>Items</title>
    </head>
    <body>
        <h1>Item List</h1>

        <form th:action="@{/items}" method="get">
            <input type="text" name="searchKeyword" th:value="${searchKeyword}" placeholder="Search items">
            <button type="submit">Search</button>
        </form>

        <table>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Description</th>
                    <th>Price</th>
                </tr>
            </thead>
            <tbody>
                <tr th:each="item : ${items}">
                    <td th:text="${item.id}">1</td>
                    <td th:text="${item.name}">Item Name</td>
                    <td th:text="${item.description}">Item Description</td>
                    <td th:text="${item.price}">100.00</td>
                </tr>
            </tbody>
        </table>

        <div>
            <span th:text="'Page ' + ${currentPage + 1} + ' of ' + ${totalPages}">Page 1 of 10</span>
            <div>
                <a th:href="@{/items(page=${currentPage - 1}, size=${items.size}, searchKeyword=${searchKeyword})}"
                th:if="${currentPage > 0}">Previous</a>
                <a th:href="@{/items(page=${currentPage + 1}, size=${items.size}, searchKeyword=${searchKeyword})}"
                th:if="${currentPage + 1 < totalPages}">Next</a>
            </div>
        </div>
    </body>
    </html>

###     