package com.sys.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sys.reggie.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}
