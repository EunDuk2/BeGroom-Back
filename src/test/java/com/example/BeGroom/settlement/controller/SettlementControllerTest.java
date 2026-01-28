package com.example.BeGroom.settlement.controller;

import com.example.BeGroom.auth.domain.UserPrincipal;
import com.example.BeGroom.settlement.service.SettlementService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SettlementController.class)
public class SettlementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SettlementService settlementService;

    @DisplayName("시작일 <= 종료일이면, 정상적으로 조회된다.")
    @Test
    void startDateIsSmallerThanEndDate() throws Exception {
        // given
        String startDate = "2026-01-01";
        String endDate = "2026-01-31";
        UserPrincipal userPrincipal = new UserPrincipal(1L, "goorm@test.com");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userPrincipal, null, Collections.emptyList());

        // when // then
        mockMvc.perform(
                get("/settlement/product")
                        .param("startDate", startDate)
                        .param("endDate", endDate)
                        .with(authentication(auth)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.statusCode").value("200"))
                        .andExpect(jsonPath("$.message").value("건별 정산 조회 성공"));

    }

    @DisplayName("시작일 < 종료일이면, 에러를 반환한다.")
    @Test
    void startDateIsBiggerThanEndDate() throws Exception {
        // given
        String startDate = "2026-01-31";
        String endDate = "2026-01-01";
        UserPrincipal userPrincipal = new UserPrincipal(1L, "goorm@test.com");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userPrincipal, null, Collections.emptyList());

        // when // then
        mockMvc.perform(
                get("/settlement/product")
                        .param("startDate", startDate)
                        .param("endDate", endDate)
                        .with(authentication(auth)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof IllegalArgumentException))
                .andExpect(result -> assertEquals("시작일은 종료일 이전이어야 합니다.", result.getResolvedException().getMessage()));
    }

    @DisplayName("page 정합성 검증: 페이지 번호가 음수면 400 에러를 반환한다.")
    @ParameterizedTest
    @ValueSource(strings = {
            "/settlement/product",
            "/settlement/period/daily",
            "/settlement/period/weekly",
            "/settlement/period/monthly",
            "/settlement/period/yearly"
    })
    void pageNumberWith(String url) throws Exception {
        // given
        UserPrincipal userPrincipal = new UserPrincipal(1L, "goorm@test.com");
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userPrincipal, null, Collections.emptyList());

        // when // then
        mockMvc.perform(
                        get(url)
                                .param("page", "-1")
                                .with(authentication(auth)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status_code").value("400"));

    }

}
