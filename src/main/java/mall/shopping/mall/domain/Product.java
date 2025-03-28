package mall.shopping.mall.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
public class Product {


    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Long id;

    // 상품명
    private String name;

    // 상풍 설명
    private String description;

    // 상품 가격
    private int price;

    // 재고 수량
    private int stockQuantity;

    //카테고리
    private String category;

    private String imageUrl;

    @ManyToMany(mappedBy = "products")  // 매핑된 필드 이름을 사용하여 관계 반영
    private List<Order> orders;  // 여러 주문에 포함될 수 있음


    public Product(Long id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public Product() {
    }

    // 수정하려는 생성자
    public Product(String name, String description, int price, int stockQuantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }
}
