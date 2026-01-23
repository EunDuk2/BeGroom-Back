package com.example.BeGroom;

import com.example.BeGroom.common.security.JwtTokenFilter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    @MockitoBean
    protected JwtTokenFilter jwtTokenFilter;

}
