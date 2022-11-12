package com.sys.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sys.reggie.common.CustomException;
import com.sys.reggie.entity.Category;
import com.sys.reggie.entity.Dish;
import com.sys.reggie.entity.Setmeal;
import com.sys.reggie.mapper.CategoryMapper;
import com.sys.reggie.service.CategoryService;
import com.sys.reggie.service.DishService;
import com.sys.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    public DishService dishService;
    @Autowired
    public SetmealService setmealService;
    /**
     * 根据id删除分类，删除之前进行判断
     * @param id
     */
    @Override
    public void remove(Long id) {
        //添加查询条件 根据分类id查询
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(Dish::getCategoryId,id);

        //查询当前分类是否关联的菜品，如果关联，抛出异常
        int count = dishService.count(queryWrapper);
        if(count>0){
            //已经关联菜品--抛出异常
            throw  new CustomException("当前分类下关联的菜品，不能删除");
        }
        //查询当前分类是否关联了套餐
        LambdaQueryWrapper<Setmeal> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Setmeal::getCategoryId,id);

        int count1 = setmealService.count(queryWrapper1);
        if(count1>0){
            //已经关联套餐--抛出异常
            throw  new CustomException("当前分类下关联的套餐，不能删除");
        }
        //正常删除
        super.removeById(id);
    }
}
