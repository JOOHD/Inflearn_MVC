## 본문

### 쇼핑몰 장바구니 구현 

    ● 목적
    보유 금액이 부족한 경우, ajax 의 alert 메시지를 이용하여 금액 부족 구현 기능을 목표 잘 해결되지 않아, 관련 코드를 공부 겸 정리하고자 작성.

    ● 설정
    Order Entity & OrderItem Entity 생성
    - Order & OrderItem Entity 사이의 연관관계를 설정
    
### Order
    @Builder 
    @AllArgsConstructor
    @Data
    @Entity
    @Table(name = "order")
    public class Order {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY) 
        private int id;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "user_id")
        private User user; // 구매자

        @OneToMany(mappedBy = "order")
        private List<OrderItem> orderItems = new ArrayList<>();

        @DateTimeFormat(pattern = "yyyy-mm-dd")
        private LocalDate createDate; // 구매 날짜

        @PrePersist
        public void crerateDate() {
            this.createDate = LocalDate.now();
        }

        public stataic Order createOrder(User user, List<OrderItem> orderItemList) {
            Order order = new Order();
            order.setUser(user);
            for (OrderItem orderItem : orderItemList) {
                order.addOrderItem(orderItem);
            }
            order.setCreateDate(order.createDate);
            return order;
        }

        public static Order createOrder(User user) {
            Order order = new Order();
            order.setUser(user);
            order.setCreateDate(order.createDate);
            return order;
        }
    }    

### OrderItem
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Entity
    public class OrderItem {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "order_Id")
        private Order order;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "user_id")
        private User user; // 구매자

        private int itemId;        // 주문 상품 번호
        private String itemName;   // 주문 상품 이름
        private int itemPrice;     // 주문 상품 가격
        private int itemCount;     // 주문 상품 수량
        private int itemTotalPrice // 가격 * 수량

        @OneToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "saleItem_id")
        private SaleItem saleItem; // 주문상품에 매핑되는 판매상품

        private int isCancel; // 주문 취소 여부 (0:주문완료 / 1:주문취소)

        // 장바구니 전체 주문(람다식 적용)
        public static OrderItem createOrderItemFromCart(int itemId, User user, CartItem cartItem, SaleItem saleItem) {
            return OrderItem.builder()
                .itemId(itemId)
                .user(user)
                .itemName(cartItem.getItem().getName())
                .itemPrice(cartItem.getItem().getPrice())
                .itemCount(cartItem.getCount())
                .itemTotalPrice(cartItem.getItem().getPrice() * cartItem.getCount())
                .saleItem(saleItem)
                .build();
        }

        // 상품 개별 주문(람다식 사용)
        public static OrderItem crateOrderItemFromItem(int itemId, User user, Item item, int count, Order order, SaleItem saleItem) {
            return OrderItem.builder()
                .itemId(itemId)
                .user(user)
                .order(order)
                .itemName(item.getName())
                .itemPrice(item.getPrice())
                .itemCount(count)
                .itemTotalPrice(item.getPrice() * count)
                .saleItem(saleItem)
                .build();
        }
    }    

### Sale
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor     
    @Data
    public class Sale {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "seller_id")
        private User seller; // 판매자

        @OneToMany(mappedBy = "sale")
        private List<SaleItem> saleItems = new ArrayList<>();

        private int totalCount; // 총 판매 개수

        public static Sale createSale(User user) {
            Sale sale = new Sale();
            sale.setSeller(seller);
            sale.setTotalCount(0);
            return sale;
        }
    }

### SaleItem
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor    
    @Data
    public class SaleItem

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="sale_id")
    private Sale sale;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "seller_id")
    private User seller; // 판매자

    private int itemId; // 주문 상품 번호
    private String itemName; // 주문 상품 이름
    private int itemPrice; // 주문 상품 가격
    private int itemCount; // 주문 상품 수량
    private int itemTotalPrice; // 가격*수량

    @OneToOne(mappedBy = "saleItem")
    private OrderItem orderItem; // 판매 상품에 매핑되는 주문 상품

    private int isCancel; // 판매 취소 여부 (0:판매완료 / 1:판매취소)

    @DateTimeFormat(pattern = "yyyy-mm-dd")
    private LocalDate createDate; // 날짜

    @PrePersist
    public void createDate(){
        this.createDate = LocalDate.now();
    }

    // 장바구니 전체 주문(람다식 적용)
    public static SaleItem createSaleItem(int itemId, Sale sale, User seller, CartItem cartItem) {
        return saleItem.builder()
            .itemId(itemId)
            .sale(sale)
            .seller(seller)
            .itemName(cartItem.getItem().getName())
            .itemPrice(cartItem.getItem().getPrice())
            .itemCount(cartItem.getCount())
            .itemTotalPrice(cartItem.getItem().getPrice() * cartItem.getCount())
            .build();
    }

    // 상품 개별 주문
    public static SaleItem createSaleItem(int itemId, Sale sale, User seller, Item item, int count) {
        return SaleItem.builder()
            .itemId(itemId)
            .sale(sale)
            .seller(seller)
            .itemName(item.getName())
            .itemPrice(item.getPrice())
            .itemCount(count)
            .itemTotalPrice(item.getPrice() * count)
            .build();
    }

### form
    <form class="card-body" th:action="@{/user/cart/checkout/{id}(id=${user.getId()})}" method="post">
        <h1 class="card-title pricing-cart-title" th:text="|${totalPrice}\|">$0</h1>
        <ul class="list-unstyled mt-3 mb-4">
            <li>잘못 담은 물건이 있는지 확인해보세요!</li>
        </ul>
        <button type="submit" class="">구매하기</button> 
    </form>

### Controller
    - 장바구니에 담은 상품의 재고가 주문량보다 적을 경우 예외처리.
    - 주문 총 가격보다 구매자의 잔액이 부족할 경우 예외처리
    - JS로 에러 메세지를 띄운다 -> 이 부분은 따로 다루겠습니다.
    - 판매자 수익 증가, 상품 재고 감소, 상품 판매량 증가 후 상품을 orderItem 과 saleItem에 담습니다.
    - 구매를 완료했으니, 장바구니 내역은 모두 삭제.

    // 장바구니 상품 전체 주문
    @Transactional
    @PostMapping("/user/cart/checkout/{id}")
    public String cartCheckout(@PathVariable("id") Integer id, @AuthenticationPrincipal PrincipalDetails, Model model) {
        // 로그인이 되어있는 유저의 id와 주문하는 id가 같아야 함
        if(principalDetails.getUser().getId() == id) {
            User user = userPageService.findUser(id);

            // 유저 카트 찾기
            Cart userCart = cartService.findUserCart(user.getId());

            // 유저 카트 안에 있는 상품들
            List<CartItem> userCartItems = cartService.allUserCartView(userCart);

            // 최종 결제 금액
            int totalPrice = 0;
            for (CartItem cartItem : userCartItem) {
                // 장바구니 안에 있는 상품의 재고가 없거나 재고보다 많이 주문할 경우
                if (cartItem.getItem().getStock() == 0 || cartItem.getItem().getStock() < cartItem.getCount()) {
                    return "redirect:/main";
                }
                totalPrice += cartItem.getCount * cartItem.getItem().getPrice();
            }

            int userCoin = user.getCoin();
            // 유저의 현재 잔액이 부족하다면
            if (userCoin < totalPrice) {
                return "redirect:/main";
            } else {
                // 유저 돈에서 최종 결제금액 빼야함
                user.setCoin(user.getCoin() - totalPrice);

                List<OrderItem> orderItemList = new ArrayList<>();

                for (CartItem cartItem : userCartItems) {
                    // 각 상품에 대한 판매자
                    User seller = cartItem.getItem().getSeller();

                    // 판매자 수익 증가
                    seller.setCoin(seller.getCoin() + (cartItem.getCount() * cartItem.getItem().getPrice()));

                    // 재고 감소
                    cartItem.getItem().setStock(cartItem.getItem().getStock() - cartItem.getCount());

                     // 상품 개별로 판매 개수 증가
                    cartItem.getItem().setCount(cartItem.getItem().getCount() + cartItem.getCount());

                    // sale, saleItem 에 담기
                    SaleItem saleItem = saleService.addSale(cartItem.getItem().getId(), seller.getId(), cartItem);

                    // order, orderItem 에 담기
                    OrderItem orderItem = orderService.addCartOrder(cartItem.getItem().getId(), user.getId(), cartItem, saleItem);

                    orderItemList.add(orderItem);
                } 

                orderService.addOrder(user, orderItemList);

                // 장바구니 상품 모두 삭제
                cartService.allCartItemDelete(id);
            }

            model.addAttribute("totalPrice", totalPrice);
            model.addAttribute("cartItems", userCartItems);
            model.addAttribute("user", userPageService.findUser(id));

            return "redirect:/user/cart/{id}";
        } else {
            return "redirect:/main";
        }
    }           