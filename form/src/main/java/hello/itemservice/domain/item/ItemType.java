package hello.itemservice.domain.item;

public enum ItemType {

    BOOK("도서"), FOOD("음식"), ETC("기타");

    private final String description;

    ItemType(String description) { //위에 있는 BOOK, FOOD, ETC
        this.description = description;
    }

    public String getDescription() { //위에 있는 "도서", "음식", "기타"
        return description;
    }
}
