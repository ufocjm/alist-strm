package fun.nibaba.alist.strm.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AlistStrimServiceTest {

    @Autowired
    private AlistStrimService alistStrimService;

    @Test
    public void test() {
        this.alistStrimService.execute();
    }

}
