package edu.mondragon.we2.pinkalert;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CrudRestDbApplicationTests {

    @MockBean
    private edu.mondragon.we2.pinkalert.config.DataLoader dataLoader;

    @Test
    void contextLoads() {
    }
}
