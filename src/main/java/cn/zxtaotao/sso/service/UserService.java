package cn.zxtaotao.sso.service;

import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.zxtaotao.common.service.RedisService;
import cn.zxtaotao.sso.mapper.UserMapper;
import cn.zxtaotao.sso.pojo.User;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RedisService redisService;
    
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public Boolean check(String param, Integer type) {
        User record = new User();
        switch (type) {
        case 1:
            record.setUsername(param);
            break;
        case 2:
            record.setPhone(param);
            break;
        case 3:
            record.setEmail(param);
            break;

        default:
            return null;
        }
        
        return this.userMapper.selectOne(record) ==null;
    }

    public Boolean doRegister(User user) {
        user.setCreated(new Date());
        user.setUpdated(user.getCreated());
        user.setId(null);
        //对密码加密,使用MD5
        user.setPassword(DigestUtils.md5Hex(user.getPassword()));
        
        return this.userMapper.insertSelective(user)==1;
    }
    
    /**
     * 登陆逻辑：先通过用户名查询User对象，再对比密码是否相同。
     * @param username
     * @param password
     * @return
     * @throws Exception 
     */
    public String doLogin(String username, String password) throws Exception {
        User record = new User();
        record.setUsername(username);
        User user=this.userMapper.selectOne(record);
        if(user == null){
            return null;//如果用户不存在，就不需要生成token了
        }
        //校验密码是否相同
        if(!StringUtils.equals(DigestUtils.md5Hex(password), user.getPassword())){
            return null;
        }
        //根据用户名查到了用户，对比密码也一样，生成登陆的token(由系统当前事件毫秒数和用户名一起MD5生成)
        String token = DigestUtils.md5Hex(System.currentTimeMillis()+username);
        
        //将用户数据保存到redis中(用redis的存储，取代tomcat的session),键为"TOKEN_"+token,值为user对象序列化后的字符串
        this.redisService.set("TOKEN_"+token,MAPPER.writeValueAsString(user) , 60*30); 
        
        return token;
    }

    public User queryUserByToken(String token) {
        String key = "TOKEN_"+token;
        String jsonData = redisService.get(key);
        if(StringUtils.isEmpty(jsonData)){
            return null;
        }
        try {
            //刷新用户的生存时间，非常重要
            redisService.expire(key, 60*30);
           return MAPPER.readValue(jsonData, User.class);
        } catch (Exception e) {
            e.printStackTrace();
        } 
        return null;
    }
}
