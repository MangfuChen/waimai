package com.sys.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sys.reggie.dto.SetmealDto;
import com.sys.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public  void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时删除套餐和菜品的关联数据
     * @param ids
     */
    public void removeWithDish(List<Long> ids);

    /**
     * 跟新套餐信息以及菜品信息
      * @param setmealDto
     */
    public void updateWithFlavor(SetmealDto setmealDto);
    /**
     * 根据id查询套餐信息，以及菜品信息
     */
    public SetmealDto getByIdWithFlavor(Long id);
}
