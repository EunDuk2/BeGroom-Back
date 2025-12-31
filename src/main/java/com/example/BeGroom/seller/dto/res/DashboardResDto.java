package com.example.BeGroom.seller.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResDto {

    @NotEmpty
    @Schema(example = "1234")
    private int orderCnt;

    @NotEmpty
    @Schema(example = "156")
    private int productCnt;

    @NotEmpty
    @Schema(example = "12345678")
    private long salesAmount;

}
