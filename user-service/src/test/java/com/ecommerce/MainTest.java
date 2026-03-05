package com.ecommerce;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
public class MainTest {

    @Test
    void contextLoads() {
    }

    @Test
    void mainTest() {
        assertDoesNotThrow(() -> Main.main(new String[]{}));
    }
}
