package com.sys.reggie.dto;

import com.sys.reggie.entity.Setmeal;
import com.sys.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
