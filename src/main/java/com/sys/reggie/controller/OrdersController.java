package com.sys.reggie.controller;

import com.sys.reggie.common.Result;
import com.sys.reggie.entity.Orders;
import com.sys.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService ;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public Result<String> submit(@RequestBody Orders orders){
        ordersService.submit(orders);
        return Result.success("下单成功");
    }
}
