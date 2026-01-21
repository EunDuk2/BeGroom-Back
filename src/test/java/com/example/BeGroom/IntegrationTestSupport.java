package com.example.BeGroom;

import com.example.BeGroom.common.security.JwtTokenFilter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "jwt.expirationAt=3600000",
        "jwt.secretKeyAt=YmV5b25kYWNjZXNzdG9rZW5zZWNyZXRrZXliZXlvbmRhY2Nlc3N0b2tlbnNlY3JldGtleWJleW9uZGFjY2Vzc3Rva2Vuc2VjcmV0a2V5"
})
public abstract class IntegrationTestSupport {

    @MockitoBean
    protected JwtTokenFilter jwtTokenFilter;

}
