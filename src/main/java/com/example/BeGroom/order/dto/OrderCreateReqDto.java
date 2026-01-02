package com.example.BeGroom.order.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateReqDto {
    @ArraySchema(
            schema = @Schema(implementation = OrderProductReqDto.class
                    , example = """
                    [
                      {
                        "productId": 1,
                        "orderQuantity": 2
                      },
                      {
                        "productId": 3,
                        "orderQuantity": 1
                      }
                    ]
                    """)
    )
    private List<OrderProductReqDto> orderProductList;
}
