package mephi.services.user;

import mephi.services.ServiceResult;
import mephi.entities.user.LoginDto;
import mephi.entities.user.RegisterDto;
import mephi.entities.user.User;

import java.util.List;

public interface UserService {
    ServiceResult register(RegisterDto registerData);
    ServiceResult login(LoginDto loginData);
    List<User> getAll();
    ServiceResult delete(String id);
}
