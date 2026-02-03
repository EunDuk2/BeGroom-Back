package com.example.BeGroom.common.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

@Component
public class TestJwtTokenProvider implements CommandLineRunner {

    private final JwtTokenProvider jwtProvider;

    public TestJwtTokenProvider(JwtTokenProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public void run(String... args) throws Exception {
        if (args.length > 0 && args[0].equals("generate-tokens")) {
            int totalTokens = 100000;
            String fileName = "user-tokens.csv";

            try (FileWriter writer = new FileWriter(fileName, StandardCharsets.UTF_8)) {
                writer.write("email,password,token\n");

                for (long i = 1L; i <= totalTokens; i++) {

                    String email = "user" + i + "@begroom.com";
                    String password = "1234";

                    String token = jwtProvider.createToken(i, email, "USER");

                    writer.write(String.format("%s,%s,%s\n", email, password, token));

                    if (i % 10000 == 0) {
                        System.out.println(i + " 개의 토큰 생성 완료");
                    }
                }
            }
            System.out.println(totalTokens + "개의 토큰이 " + fileName + "에 저장되었습니다!");
        }
    }
}
