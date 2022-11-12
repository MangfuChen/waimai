package com.sys.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sys.reggie.dto.DishDto;
import com.sys.reggie.entity.Dish;
import com.sys.reggie.entity.DishFlavor;
import com.sys.reggie.mapper.DishMapper;
import com.sys.reggie.service.DishFlavorService;
import com.sys.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j

public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    DishFlavorService dishFlavorService;
    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息dish
        this.save(dishDto);


        //重新处理数据
        Long dishId = dishDto.getId();
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());

        //口味数据 dish_flavor
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息，从dish表查询
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish,dishDto);

        //查询当前菜品对应的口味，dish_flavor
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 更新菜品与口味
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {

        //跟新dish表基本信息
        this.updateById(dishDto);
        //清理当前菜品对应的口味数据 dish_flavor
        LambdaQueryWrapper<DishFlavor> queryWrapper  = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());

        dishFlavorService.remove(queryWrapper);
        //添加提交过来的口味数据
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item)->{
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }
}
