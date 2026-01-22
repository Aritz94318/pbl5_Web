package edu.mondragon.we2.pinkAlert;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CrudRestDbApplicationTests {

    @MockBean
    private edu.mondragon.we2.pinkAlert.config.DataLoader dataLoader;

  
}
