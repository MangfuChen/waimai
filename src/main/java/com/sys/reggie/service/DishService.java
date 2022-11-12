package com.sys.reggie.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.sys.reggie.dto.DishDto;
import com.sys.reggie.entity.Dish;

public interface DishService extends IService<Dish> {

    //新增菜品，同时插入口味数据   dish   dish_flavor
    public  void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);
    //更新菜品  与对应的口味信息
    public void updateWithFlavor(DishDto dishDto);
}
