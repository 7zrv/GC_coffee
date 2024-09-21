package org.example.gc_coffee.product.requestDto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.example.gc_coffee.product.entity.Product;


@Getter
public class CreateProductRequestDto {


    @NotEmpty(message = "제품명을 입력해주세요.")
    private String productName;

    @NotBlank(message = "카테고리를 입력해주세요.")
    private String category;

    @NotNull(message = "가격을 입력해주세요.")
    private Long price;

    @NotEmpty(message = "제품 설명을 입력해주세요.")
    private String description;


    public Product toEntity(){
        return Product.builder()
                .productName(productName)
                .category(category)
                .price(price)
                .description(description)
                .build();
    }
}
