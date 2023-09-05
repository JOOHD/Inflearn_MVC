package hello.itemservice.web.form;

import hello.itemservice.domain.item.DeliveryCode;
import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.domain.item.ItemType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/form/items")
@RequiredArgsConstructor
public class FormItemController {

    private final ItemRepository itemRepository;

    /**
     * @ModelAttribute의 특별한 사용법
        등록 폼, 상세화면, 수정 폼에서 모두 서울, 부산, 제주라는 체크 박스를 반복해서 보여주어야한다.
        이렇게 하려면 각각의 컨트롤러에서 model.addAttribute(...)을 사용해서 체크박스를 구성하는 데이터를 반복해서 넣어야한다.
        그런데 밑에 처럼 해당 컨트롤러를 요청할 때 regions에서 반환한 값이 자동으로 model에 담기게 된다.(model 선언한 메소드)
     */
    @ModelAttribute("regions")
    public Map<String, String> regions() {
        Map<String, String> regions = new LinkedHashMap<>();
        regions.put("SEOUL", "서울");
        regions.put("BUSAN", "부산");
        regions.put("JEJU", "제주");
        return regions;

        //model.addAttribute("regions", regions);가 자동으로 들어간다.
    }

    /**
     * itemTypes 를 등록 폼, 조회, 수정 폼에서 모두 사용하므로 @ModelAttribute 의 특별한 사용법을
      적용하자. ItemType.values() 를 사용하면 해당 ENUM의 모든 정보를 배열로 반환한다. 예) [BOOK, FOOD,ETC]
     */
    @ModelAttribute("itemTypes")
    public ItemType[] itemTypes() {
        //ItemType[] values = ItemType.values();
        return ItemType.values();
    }

    @ModelAttribute("deliveryCodes")
    public List<DeliveryCode> deliveryCodes() {
        List<DeliveryCode> deliveryCodes = new ArrayList<>();
        deliveryCodes.add(new DeliveryCode("FAST", "빠른 배송"));
        deliveryCodes.add(new DeliveryCode("NORMAL", "일반 배송"));
        deliveryCodes.add(new DeliveryCode("SLOW", "느린 배송"));
        return deliveryCodes;
    }

    @GetMapping //회원 전체 조회
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "form/items";
    }

    @GetMapping("/{itemId}") //회원 조회
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);

//        Map<String, String> regions = new LinkedHashMap<>();
//        regions.put("SEOUL", "서울");
//        regions.put("BUSAN", "부산");
//        regions.put("JEJU", "제주");
//        model.addAttribute("regions",regions);
        return "form/item";
    }

    @GetMapping("/add") //상품 추가 페이지
    public String addForm(Model model) {
        model.addAttribute("item", new Item());

        //데이터를 넘겨주어야 한다.
//        Map<String, String> regions = new LinkedHashMap<>(); //hash map순서 보장 안됨, Linked 보장됨
//        regions.put("SEOUL", "서울"); // "SEOUL" 실제 값, "서울" 보여주기위한 값
//        regions.put("BUSAN", "부산");
//        regions.put("JEJU", "제주");
//        model.addAttribute("regions",regions);
        return "form/addForm";
    }

    @PostMapping("/add") //상품 저장
    public String addItem(@ModelAttribute Item item, RedirectAttributes redirectAttributes) {

        log.info("item.open={}", item.getOpen());
        log.info("item.regions={}", item.getRegions());
        log.info("item.itemType={}", item.getItemType());

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/form/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit") //상품 수정 페이지
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);

//        Map<String, String> regions = new LinkedHashMap<>(); //hash map순서 보장 안됨, Linked 보장됨
//        regions.put("SEOUL", "서울"); // "SEOUL" 실제 값, "서울" 보여주기위한 값
//        regions.put("BUSAN", "부산");
//        regions.put("JEJU", "제주");
//        model.addAttribute("regions",regions);
        return "form/editForm";
    }

    @PostMapping("/{itemId}/edit") //상품 수정
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/form/items/{itemId}";
    }

}

