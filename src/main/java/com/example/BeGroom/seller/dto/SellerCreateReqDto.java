package com.example.BeGroom.seller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerCreateReqDto {

    @NotEmpty
    @Schema(example = "begroom@groom.com")
    private String email;

    @NotEmpty
    @Schema(example = "begroom")
    private String name;

    @NotEmpty
    @Schema(example = "1234")
    private String password;

    @Schema(example = "01012341234")
    private String phoneNumber;

}
