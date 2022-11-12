package com.sys.reggie.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sys.reggie.common.Result;
import com.sys.reggie.entity.Employee;
import com.sys.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param req      响应
     * @param employee  获取其前台发送过来的数据
     * @return
     */
    @PostMapping("/login")
    public Result<Employee> login(HttpServletRequest req, @RequestBody Employee employee){
        /**
         * 登录需要判断
         *  1、密码要加密
         *  2、查询数据库
         *  3、密码对比
         *  4、查看员工状态
         *  5、登录成功返回
         */

        //加密 md5
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //查询数据库   条件构造器wrapper
        LambdaQueryWrapper<Employee> queryChainWrapper = new LambdaQueryWrapper<>();
        queryChainWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryChainWrapper);
        if (emp == null){
            //用户是否存在
            return  Result.error("用户不存在");
        }

        //比较密码
        if(!emp.getPassword().equals(password)){
            return  Result.error("密码错误");
        }

        //查看员工状态  0表示禁用 1表示可用
        if(emp.getStatus() == 0){
            return  Result.error("用户已被禁用");
        }

        //登录成功
        req.getSession().setAttribute("employee",emp.getId());
        emp.setPassword("");
        return Result.success(emp);
    }

    /**
     *  退出登录
     * @param session
     * @return
     */
    @PostMapping("/logout")
    public Result<String> logout(HttpSession session){
        //
        session.removeAttribute("employee");
        return Result.success("退出成功");
    }

    /**
     * page显示页面
     * 传入数据
     *      page ---当前页
     *      pagesize---当前显示多少条数据
     *      name----查询员工条件
     */
    @GetMapping("/page")
    public Result<Page> page(int page,int pageSize,String name){
        log.info("page={},pageSize={},name={}",page,pageSize,name);

        //构造分页条件
        Page pageInfo = new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper();
        //添加一个过滤条件
        queryWrapper.like(StringUtils.isNotBlank(name),Employee::getName,name);
        //添加排序体哦阿健
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo,queryWrapper);

        return Result.success(pageInfo);
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public Result<String> save(HttpSession session,@RequestBody Employee employee){
        log.info("新增员工，员工信息：{}",employee);
        //给员工设置一个初始密码 需要进行加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        employee.setCreateTime(LocalDateTime.now());//创建时间
//        employee.setUpdateTime(LocalDateTime.now());//更新时间

        //获取当前登录用户的id  强转一下
//        Long empId = (Long)session.getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);

        employeeService.save(employee);
        return Result.success("新曾员工成功");
    }

    /**
     * 根据id修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public Result<String> update(HttpSession session,@RequestBody Employee employee){
        log.info("222"+employee.toString());
        //设置更新时间，以及跟新的人
//        employee.setUpdateTime(LocalDateTime.now());
//        Long empId = (Long) session.getAttribute("employee");
//        employee.setUpdateUser(empId);
        Long id = Thread.currentThread().getId();
        log.info("线程id"+id.toString());
        employeeService.updateById(employee);
        return Result.success("员工信息修改成成功");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping ("/{id}")
    public Result<Employee> getById(@PathVariable Long id){
        Employee employee = employeeService.getById(id);
        if(employee !=null){
            return Result.success(employee);
        }
        return Result.error("没有查询到对应的员工信息");
    }
}
