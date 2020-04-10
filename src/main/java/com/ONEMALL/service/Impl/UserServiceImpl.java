package com.ONEMALL.service.Impl;

import com.ONEMALL.common.Const;
import com.ONEMALL.common.ServerResponse;
import com.ONEMALL.common.TokenCache;
import com.ONEMALL.dao.UserMapper;
import com.ONEMALL.pojo.User;
import com.ONEMALL.service.IUserService;
import com.ONEMALL.util.MD5Util;
import net.sf.jsqlparser.schema.Server;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.security.sasl.SaslServer;
import java.util.UUID;

@Service("UserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("User doesn't exist");
        }

        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);
        if(user == null){
            return ServerResponse.createByErrorMessage("Wrong password");
        }

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("Successfully login", user);
    }

    public ServerResponse<String> register(User user){
//        ServerResponse validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
//        if(!validResponse.isSussess()){
//            return validResponse;
//        }
        int resultCount = userMapper.checkUsername(user.getUsername());
        if(resultCount > 0){
            return ServerResponse.createByErrorMessage("Username exists");
        }
        resultCount = userMapper.checkEmail(user.getEmail());
        if(resultCount > 0){
            return ServerResponse.createByErrorMessage("Email exists");
        }
//        validResponse = this.checkValid(user.getEmail(), Const.EMAIL);
//        if(!validResponse.isSussess()){
//            return validResponse;
//        }
        user.setRole(Const.Role.ROLE_CUSTOMER);

        //MD5 encoding
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        resultCount = userMapper.insert(user);
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("Failed sign up");
        }
        return ServerResponse.createBySuccessMessage("Successfully signed up");

    }

    public ServerResponse<String> checkValid(String str, String type){
        if(StringUtils.isNotBlank(type)){
            if(Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUsername(str);
                if(resultCount > 0){
                    return ServerResponse.createByErrorMessage("Username exists");
                }
            }
            if(Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(str);
                if(resultCount > 0){
                    return ServerResponse.createByErrorMessage("Email exists");
                }
            }
        }else{
            return ServerResponse.createByErrorMessage("Param invalid");
        }
        return ServerResponse.createBySuccessMessage("Validation success");
    }

    public ServerResponse selectQuestion(String username){
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if(!validResponse.isSussess()){
            return ServerResponse.createByErrorMessage("User doesn't exist");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("Security question is empty");
    }

    public ServerResponse<String> checkAnswer(String username, String question, String answer){
        int resultCount = userMapper.checkAnswer(username,question, answer);
        if(resultCount > 0){
            //Question and answer correct
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("Wrong answer");
    }

    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken){
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("False param");
        }
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if(!validResponse.isSussess()){
            return ServerResponse.createByErrorMessage("User doesn't exist");
        }

        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("Token invalid or expired");
        }

        if(StringUtils.equals(forgetToken, token)){
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username,md5Password);
            if(rowCount > 0){
                return ServerResponse.createBySuccessMessage("Password updated");
            }
        }else{
            return ServerResponse.createByErrorMessage("Token invalid, please regain token for reset password");
        }
        return ServerResponse.createByErrorMessage("Failed update password");
    }

    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user){
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("Incorrect old password");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount > 0) {
            return ServerResponse.createBySuccessMessage("Password updated");
        }
        return ServerResponse.createByErrorMessage("Failed update password");
    }

    public ServerResponse<User> updateInformation(User user){
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount > 0){
            return ServerResponse.createByErrorMessage("Email already used");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount > 0){
            return ServerResponse.createBySuccess("Info updated", updateUser);
        }
        return ServerResponse.createByErrorMessage("Failed update info");
    }

    public ServerResponse<User>getInformation(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMessage("Current user not found");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    /**
     * Backend management
     */

    /**
     * Check user role.
     * @param user
     * @return
     */
    public ServerResponse checkAdminRole(User user){
        if(user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }
}
