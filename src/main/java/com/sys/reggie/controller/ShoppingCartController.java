package com.sys.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sys.reggie.common.BaseContext;
import com.sys.reggie.common.Result;
import com.sys.reggie.entity.ShoppingCart;
import com.sys.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    ShoppingCartService shoppingCartService;

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list(){
        //条件
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        //查询
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);
        return Result.success(shoppingCarts);
    }


    /**
     * 将菜品或套餐添加进购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info(shoppingCart.toString());
        //设置用户id，指定当前是那个用户购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //查询当前菜品或者是套餐，是否在购物车中
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        if(dishId!=null){
            //添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else{
            //添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //select * from shopping_cart  where user_id = ? and dish_id = ? limit 1
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        //如果已经存在，就在原来数量基础上加一
        if(cartServiceOne!=null){
            //已经存在，加一就行
            Integer number = cartServiceOne.getNumber();
            cartServiceOne.setNumber(number+1);
            shoppingCartService.updateById(cartServiceOne);
        }else{
            //不存在，则添加到购物车 ，数量默认1
            shoppingCartService.save(shoppingCart);
            shoppingCart.setCreateTime(LocalDateTime.now());
            cartServiceOne = shoppingCart;
        }

        return Result.success(cartServiceOne);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public Result<String> clean (){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(queryWrapper);
        return Result.success("购物车清空成功");
    }

    /**
     * 删除、减少单个购物车数据
     * @return
     */
    @PostMapping("/sub")
    public Result<String> sub(@RequestBody ShoppingCart shoppingCart){
        //判断是减少的是菜品还是套餐的数量
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        if(dishId!=null){
            //减少的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);

        }else{
            //减少的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        //在判断数量是否等于1 等于一删除
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        Integer number = cartServiceOne.getNumber();
        if(number >1){
            //菜品或者套餐拥有一个以上的记录
            cartServiceOne.setNumber(number-1);
            shoppingCartService.updateById(cartServiceOne);
        }else{
            //菜品或套餐只有一条记录
            shoppingCartService.removeById(cartServiceOne);
        }
        return Result.success("购物车减少成功");
    }
}
