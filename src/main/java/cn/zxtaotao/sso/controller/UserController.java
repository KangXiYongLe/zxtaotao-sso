package cn.zxtaotao.sso.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cn.zxtaotao.common.utils.CookieUtils;
import cn.zxtaotao.sso.pojo.User;
import cn.zxtaotao.sso.service.UserService;

@Controller
@RequestMapping("user")
public class UserController {
    
    private static final String COOKIE_NAME = "ZXTT_TOKEN";
    
    public static final String CART_COOKIE_NAME = "ZXTT_CART";
    
    @Autowired
    private UserService userService;
    
    private String sendUrl=null;
    
    /**
     * 转发请求到注册页面
     * @return
     */
    @RequestMapping(value="register",method=RequestMethod.GET)
    public String toRegister(){
        return "register";
    }
    
    /**
     * 注册校验
     * @param param
     * @param type
     * @return
     */
    @RequestMapping(value = "check/{param}/{type}", method = RequestMethod.GET)
    public ResponseEntity<Boolean> check(@PathVariable("param") String param,
            @PathVariable("type") Integer type) {
        try {
            Boolean boo = this.userService.check(param,type);
            if(boo==null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
            
            return ResponseEntity.ok(boo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
    /**
     * "@Valid"用hibernate的校验标签校验user的属性是否符合指定的规则
     * @param user
     * @param bindingResult 将校验结果封装到bindingResult对象
     * @return
     */
    @RequestMapping(value="doRegister",method=RequestMethod.POST)
    public ResponseEntity<Map<String, Object>> doRegister(@Valid User user,BindingResult bindingResult){
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            //校验参数
            if(bindingResult.hasErrors()){
                //校验到有错误
                List<String> msgs = new ArrayList<String>();
                List<ObjectError> allErrors = bindingResult.getAllErrors();
                for (ObjectError objectError : allErrors) {
                    msgs.add(objectError.getDefaultMessage());
                }
                result.put("status", "400");
                result.put("data", StringUtils.join(msgs,"|"));//将集合转化为用|分隔的字符串
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
            Boolean boo=this.userService.doRegister(user);
            if(boo){
                result.put("status", "200");
            }else{
                result.put("status", "500");
                result.put("data", "哈哈~~~");
            }
        } catch (Exception e) {
            result.put("status", "500");
            result.put("data", "哈哈~~~");
            e.printStackTrace();
        }
        return ResponseEntity.ok(result);
    }
    
    /**
     * 请求到登陆页面
     * @return
     */
    @RequestMapping(value="login", method=RequestMethod.GET)
    public String toLogin(String sendUrl){
        if(StringUtils.isNoneBlank(sendUrl)){
            this.sendUrl = sendUrl;
        }
        return "login";
    }

    /**
     * 执行登陆的业务逻辑，登陆成功后，将生成一个token(类实于sessionID),将其写入Cookie，响应的时候存到浏览器
     * @param username
     * @param password
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(method = RequestMethod.POST, value = "doLogin")
    public ResponseEntity<Map<String, Object>> doLogin(@RequestParam("username") String username,
            @RequestParam("password") String password, HttpServletRequest request,
            HttpServletResponse response) {
        Map<String, Object> result = new HashMap<String, Object>();
        try {
            String token = this.userService.doLogin(username,password);
            if(token==null){//登陆失败
                result.put("status", 400);
            }else{//登陆成功，将token写入cookie
                result.put("status", 200);
                CookieUtils.setCookie(request, response, COOKIE_NAME, token);
                //将Cookie中的商品信息插入数据库 TODO
            }
        } catch (Exception e) {
            e.printStackTrace();
            //捕抓到了异常，登录失败
            result.put("status", 400);
        }
        if(this.sendUrl!=null){
            result.put("sendUrl", this.sendUrl);
        }
        return ResponseEntity.ok(result);
    }
    
    /**
     * 根据token查询用户信息
     * @param token
     * @return
     */
    @RequestMapping(value="{token}",method=RequestMethod.GET)
    public ResponseEntity<User> queryUserByToken(@PathVariable("token")String token){
//        try {
//            User user = this.userService.queryUserByToken(token);
//            if(user==null){
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
//            }
//            return ResponseEntity.ok(user);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        User user = new User();
        user.setUsername("该服务已废弃，请访问querysso.zxtaotao.cn或dubbo中的服务");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(user);
    }
}
