package com.why.demo.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import com.why.demo.clients.RedisClient;
import com.why.demo.common.ResultData;
import com.why.demo.common.annotation.RepeatSubmit;
import com.why.demo.domain.vo.UserVO;
import com.why.demo.exception.YyException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Api(tags = "测试各种api")
@RestController
@RequestMapping("/hello")
public class HelloController {

    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @Autowired
    private RedisClient redisClient;

    @GetMapping("/key/page")
    @ApiOperation(value = "redis的key分页查询", notes = "根据匹配规则获取redis中的key集合，分页")
    public List<String> getKeysForPage(@ApiParam(value = "匹配规则", name = "pattern", required = true)
                               @RequestParam("pattern") String pattern,
                               @ApiParam(value = "页码，第几页", name = "pageNum", defaultValue = "1")
                               @RequestParam("pageNum") int pageNum,
                               @ApiParam(value = "条数，每页展示几条数据", name = "pageSize", defaultValue = "2")
                               @RequestParam("pageSize") int pageSize) {
        List<String> keys = redisClient.getKeysForPage(pattern, pageNum, pageSize);
        logger.info("pattern={}, pageNum={}, pageSize={}, keys={}", pattern, pageNum, pageSize, String.join(",", keys));
        return keys;
    }

    @GetMapping("/key/scan")
    @ApiOperation(value = "获取redis中的key", notes = "根据匹配规则获取redis中的key集合")
    public List<String> getKeys(@ApiParam(value = "匹配规则", name = "pattern", required = true)
                             @RequestParam("pattern") String pattern,
                        @ApiParam(value = "取出的数量", name = "count", required = true)
                        @RequestParam("count") long count) {
        List<String> keys = redisClient.scan(pattern, count);
        logger.info("pattern={}, count={}, keys={}", pattern, count, String.join(",", keys));
        return keys;
    }

    @PostMapping("/validate/code")
    @ApiOperation(value = "验证码", notes = "验证码")
    public void validateCode(@ApiParam(value = "每次请求验证码时的唯一编号", name = "uuid", required = true)
                                 @RequestParam("uuid") String uuid, HttpServletResponse response) throws IOException {
        //定义图形验证码的长、宽、验证码字符数、干扰线宽度
        ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(200, 100, 4, 4);
        //图形验证码写出，可以写出到文件，也可以写出到流
        captcha.write(response.getOutputStream());
        String code = captcha.getCode();
        redisClient.set(uuid, code, 60, TimeUnit.SECONDS);
    }

    @PostMapping("/login")
    @ApiOperation(value = "登录", notes = "验证码登录")
    public ResultData<String> login(@ApiParam(value = "uuid", name = "每次请求验证码时的唯一编号", required = true) @RequestParam("uuid") String uuid,
                      @ApiParam(value = "userInputCode", name = "用户输入的验证码", required = true) @RequestParam("userInputCode") String userInputCode,
                      @ApiParam(value = "username", name = "账号", required = true) @RequestParam("username") String username,
                      @ApiParam(value = "password", name = "密码", required = true) @RequestParam("password") String password) {

        //验证图形验证码的有效性，返回boolean值
        String code = redisClient.get(uuid);
        if (StringUtils.isBlank(code)) {
            throw new YyException("验证码已失效");
        }
        if (StringUtils.isBlank(userInputCode)) {
            throw new YyException("请输入验证码");
        }
        boolean success = StringUtils.equalsIgnoreCase(code, userInputCode);
        if (success) {
            return ResultData.success("登录成功");
        }
        return ResultData.fail(900001, "登录失败");
    }

    @PostMapping("/desensitize/detail")
    @ApiOperation(value = "测试数据脱敏功能", notes = "通过在vo类中的字段上添加注解实现数据脱敏")
    public ResultData<UserVO> desensitizeDetail(){
        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setName("张三疯");
        userVO.setAge(23);
        userVO.setEmail("977679300@qq.com");
        userVO.setIdNumber("411412199809082381");
        userVO.setMoney("1000.23");
        userVO.setMobilePhone("18939086561");
        userVO.setAddress("河南省商丘市睢县");
        userVO.setBankAccount("247184514301321352");
        userVO.setPassword("abc1234");
        userVO.setSecretKey("1984Kjskhda_7da");
        return ResultData.success(userVO);
    }

    @PostMapping("/desensitize/list")
    @ApiOperation(value = "测试数据脱敏功能", notes = "通过在vo类中的字段上添加注解实现数据脱敏")
    public ResultData<List<UserVO>> desensitizeList(){
        List<UserVO> list = new ArrayList<>();
        UserVO userVO = new UserVO();
        userVO.setId(1L);
        userVO.setName("张三疯");
        userVO.setAge(23);
        userVO.setEmail("977679300@qq.com");
        userVO.setIdNumber("411412199809082381");
        userVO.setMoney("1000.23");
        userVO.setMobilePhone("18939086561");
        userVO.setAddress("河南省商丘市睢县");
        userVO.setBankAccount("247184514301321352");
        userVO.setPassword("abc1234");
        userVO.setSecretKey("1984Kjskhda_7da");
        list.add(userVO);
        UserVO userVO2 = new UserVO();
        userVO2.setId(2L);
        userVO2.setName("牛二");
        userVO2.setAge(24);
        userVO2.setEmail("joiadgada@163.com");
        userVO2.setIdNumber("413466199912110023");
        userVO2.setMoney("9123414124.00");
        userVO2.setMobilePhone("15623815523");
        userVO2.setAddress("上海市闵行区浦江镇");
        userVO2.setBankAccount("174302756732431");
        userVO2.setPassword("passwd1234");
        userVO2.setSecretKey("243jiewd9832r56=sdfs");
        list.add(userVO2);
        return ResultData.success(list);
    }

    // 默认1s，方便测试查看，设置为10s
    @RepeatSubmit(expireTime = 10)
    @PostMapping("/idempotent")
    @ApiOperation(value = "测试接口幂等性", notes = "通过redis+注解解决接口幂等性")
    public ResultData<String> idempotent(@ApiParam(value = "name", required = true) @RequestParam("name") String name){
        logger.info("{}...{}...name=[{}]", Thread.currentThread().getName(), LocalDateTime.now(), name);
        return ResultData.success();
    }

    @PostMapping("/set")
    @ApiOperation(value = "redis设置string数据", notes = "set方法")
    public void setValue(
            @ApiParam(value = "redis的key", required = true) @RequestParam("key") String key,
            @ApiParam(value = "redis的value", required = true) @RequestParam("value") String value) {
        logger.info("使用redis存放数据时，key={}, value={}", key, value);
        redisClient.set(key, value);
    }

    @GetMapping("/get")
    @ApiOperation(value = "redis获取string数据", notes = "get方法")
    public String getValue(@ApiParam(value = "redis的key", required = true) @RequestParam("key") String key) {
        logger.info("使用redis获取数据时，key={}", key);
        String value = redisClient.get(key);
        logger.info("获取的value是:{}", value);
        return value;
    }
}
