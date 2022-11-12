package com.sys.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sys.reggie.common.CustomException;
import com.sys.reggie.dto.SetmealDto;
import com.sys.reggie.entity.Setmeal;
import com.sys.reggie.entity.SetmealDish;
import com.sys.reggie.mapper.SetmealMapper;
import com.sys.reggie.service.SetmealDishService;
import com.sys.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐基本信息，操作setmeal
        this.save(setmealDto);
        //保存套餐和菜品的关联信息，操作setmeal_dish
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //数据处理  前台传递过来的数据 套餐与菜品联系表没有套餐的id 需要自己添加
        setmealDishes.stream().map(item->{
            item.setSetmealId(setmealDto.getId());
           return item;
        }).collect(Collectors.toList());

        //执行保存
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐，同时删除套餐和菜品的关联数据
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //select count(*) from setmeal where id in (...) and status =1   判断id中是否有在售卖的东西吗
        //判断是否停售   确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids).eq(Setmeal::getStatus,1);

        int count = this.count(queryWrapper);
        //不可以删除，抛出异常
        if(count>0){
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        //如果可以删除，先删除套餐表中的数据---setmeal
        this.removeByIds(ids);

        //删除关系表中的数据---setmeal_dish
        //delete from setmeal_dish where setmeal_id in ...
        LambdaQueryWrapper<SetmealDish> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(queryWrapper1);
    }

    /**
     * 更新套餐与关联的套餐与菜品表
     * setmeal ---setmeal_dish
     * @param etmealDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(SetmealDto etmealDto) {

        //跟新setmeal表基本信息s
        this.updateById(etmealDto);
        //清理当前套餐与对应的菜品数据 setmeal_dish
        LambdaQueryWrapper<SetmealDish> queryWrapper  = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,etmealDto.getId());

        setmealDishService.remove(queryWrapper);
        //添加提交过来的套餐与对应的菜品数据  setmeal_dish
        List<SetmealDish> flavors = etmealDto.getSetmealDishes();

        flavors = flavors.stream().map((item)->{
            item.setSetmealId(etmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        setmealDishService.saveBatch(flavors);
    }

    /**
     * 根据id查询套餐信息，以及菜品信息
     */
    @Override
    public SetmealDto getByIdWithFlavor(Long id) {
        //自身查出的数据不完整 缺少东西
        Setmeal setmeal = this.getById(id);
        //前端需要的内容
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);

        //查询套餐对应的菜品表
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        setmealDto.setSetmealDishes(list);

        return setmealDto;
    }
}
