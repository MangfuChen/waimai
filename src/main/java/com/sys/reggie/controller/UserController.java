package com.sys.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sys.reggie.common.Result;
import com.sys.reggie.entity.User;
import com.sys.reggie.service.UserService;
import com.sys.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {
    //输入String类型的Redis配置器
    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    private UserService userService;

    /**
     * 移动端用户登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public Result<User> login(@RequestBody Map map ,HttpSession session){
        log.info(map.toString());
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //从session中获取验证码
        //Object codeInSession = session.getAttribute(phone);
        //从redis中获取验证码
        String codeInSession = redisTemplate.opsForValue().get(phone);
        //进行验证码验证
        if (codeInSession!=null && codeInSession.equals(code)){
            //成立登录成功

            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            //判断当前手机号对应的用户是否为新用户，如果是新用户就自动完成注册
            User user = userService.getOne(queryWrapper);
            if(user == null){
                //自动完成注册
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
                //注册后，拿到id
                user = userService.getOne(queryWrapper);
            }
            session.setAttribute("user",user.getId());

            //如果用户登录成功，删除redis验证码
            redisTemplate.delete(phone);
            return Result.success(user);
        }

        return Result.error("登录失败");
    }

    /**
     * 移动端发送手机短信
     * @param user
     * @param session
     * @return
     */
    @PostMapping("/sendMsg")
    public Result<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();

        if(StringUtils.isNotEmpty(phone)){
            //生成验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code+{}",code);


            //调用短信api  发送给用户手机

            //将验证码放入session
            //session.setAttribute(phone,code);
            //将生成的验证码缓存到redis ，有效期为1分钟
            redisTemplate.opsForValue().set(phone,code,1, TimeUnit.MINUTES);
            return Result.success("手机验证码发送成功");
        }


        return Result.error("手机验证码发送失败");
    }
}
