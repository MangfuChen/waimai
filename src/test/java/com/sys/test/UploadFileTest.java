package com.sys.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

public class UploadFileTest {
    @Test
    public void test1(){
        String fileName = "error.jpg";
        String substring = fileName.substring(fileName.lastIndexOf("."));
        System.out.println(substring);
    }
}
