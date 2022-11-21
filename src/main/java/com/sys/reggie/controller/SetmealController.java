package com.sys.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sys.reggie.common.Result;
import com.sys.reggie.dto.SetmealDto;
import com.sys.reggie.entity.Category;
import com.sys.reggie.entity.Setmeal;
import com.sys.reggie.service.CategoryService;
import com.sys.reggie.service.SetmealDishService;
import com.sys.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "setmealCache",allEntries = true)//清除掉setmealCache所有数据
    public Result<String> save(@RequestBody SetmealDto setmealDto){
        log.info("套餐信息"+setmealDto.toString());
        setmealService.saveWithDish(setmealDto);

        return Result.success("新增套餐成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result<Page> page(int page,int pageSize,String name){
        //分页构造器
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        //需要给前端的分页数据
        Page<SetmealDto> dtoPage = new Page<>();



        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Setmeal::getName,name);
        //排序条件 跟新时间 降序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行查询
        setmealService.page(pageInfo,queryWrapper);


        //将pageinfo的数据给dtopage
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        //处理records
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map(item->{
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return Result.success(dtoPage);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "setmealCache",allEntries = true)//清除掉setmealCache所有数据
    public Result<String> delete(@RequestParam List<Long> ids){
        log.info("id+{}",ids);
        setmealService.removeWithDish(ids);
        return Result.success("套餐数据删除成功");
    }

    /**
     * 修改套餐内容
     * @param setmealDto
     * @return
     */
    @PutMapping
    @CacheEvict(value = "setmealCache",allEntries = true)//清除掉setmealCache所有数据
    public Result<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithFlavor(setmealDto);
        return Result.success("修改套餐数据成功");
    }


    /**
     * 根据id 查询套餐与菜品表对应的数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<SetmealDto> getByid(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getByIdWithFlavor(id);
        return Result.success(setmealDto);
    }

    /**
     * 修改状态与批量修改状态
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    @CacheEvict(value = "setmealCache",allEntries = true)//清除掉setmealCache所有数据
    public Result<String> updateStatus(@PathVariable Integer status,@RequestParam List<Long> ids){

        for (Long id: ids) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(id);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }

        return Result.success("修改套餐状态成功");
    }

    @GetMapping("/list")
    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    public Result<List<Setmeal>> list(Setmeal setmeal){
        log.info(setmeal.toString());
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> setmealList = setmealService.list(queryWrapper);
        return Result.success(setmealList);
    }
}
