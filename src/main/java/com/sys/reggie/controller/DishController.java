package com.sys.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sys.reggie.common.Result;
import com.sys.reggie.dto.DishDto;
import com.sys.reggie.entity.Category;
import com.sys.reggie.entity.Dish;
import com.sys.reggie.entity.DishFlavor;
import com.sys.reggie.service.CategoryService;
import com.sys.reggie.service.DishFlavorService;
import com.sys.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.websocket.server.PathParam;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto){
        log.info("添加菜品"+dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        return Result.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page, int pageSize, String name){
        //分页构造器
        Page<Dish> pageInfo = new Page(page,pageSize);
        Page<DishDto> dishDtoPage = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.like(name!=null,Dish::getName,name);
        //排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行分页查询
        dishService.page(pageInfo,queryWrapper);


        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");

        //处理数据   遍历records查询到的数据，循环遍历 到每一个categoryId 在查询分类
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category!=null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);

            }
            return dishDto;
        }).collect(Collectors.toList());



        dishDtoPage.setRecords(list);


        return Result.success(dishDtoPage);
    }


    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<DishDto> getByid(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);

        return Result.success(dishDto) ;
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public Result<String> update(@RequestBody DishDto dishDto){
        log.info("修改菜品"+dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        return Result.success("修改菜品成功");
    }


    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
//    @GetMapping("/list")
//    public Result<List<Dish>> list(Dish dish){
//        //条件构造器
//        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//        //添加条件，查询菜品状态   状态为1是起售卖  的菜品
//        queryWrapper.eq(Dish::getStatus,1);
//        //排序
//        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//
//        List<Dish> list = dishService.list(queryWrapper);
//        return Result.success(list);
//    }
    @GetMapping("/list")
    public Result<List<DishDto>> list(Dish dish){
        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询菜品状态   状态为1是起售卖  的菜品
        queryWrapper.eq(Dish::getStatus,1);
        //排序
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);
        List<DishDto> dishDtos = list.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category!=null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //当前菜品id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        return Result.success(dishDtos);
    }

    /**
     * 修改菜品的停售与起售  通过statu来判断
     * @param statu     0为停售，1为起售
     * @param ids
     * @return
     */
    @PostMapping("/status/{statu}")
    public Result<String> changeStatus(@PathVariable Integer statu,@RequestParam List<Long> ids){
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        for (Long id:ids) {
            Dish dish = new Dish();
            dish.setId(id);
            dish.setStatus(statu);
            dishService.updateById(dish);
        }

        return Result.success("状态修改成功");
    }
}
