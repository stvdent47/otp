package mephi.services.user;

import mephi.repository.user.UserRepository;
import mephi.services.ServiceResult;
import mephi.entities.user.LoginDto;
import mephi.entities.user.RegisterDto;
import mephi.entities.user.User;
import mephi.entities.user.UserRole;
import mephi.services.jwt.JwtService;
import mephi.services.password.PasswordService;

import java.util.List;
import java.util.UUID;

public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private boolean isAdminCreationAllowed() {
        return this.userRepository.getByCondition("role = 'admin'").isEmpty();
    }

    @Override
    public ServiceResult register(RegisterDto registerData) {
        if (
            registerData.role().equalsIgnoreCase("admin") &&
            !this.isAdminCreationAllowed()
        ) {
            return new ServiceResult(false, "a user with an admin role already exists");
        }

        boolean result = this.userRepository.create(
            new User(
                UUID.randomUUID().toString(),
                UserRole.valueOf(registerData.role()),
                registerData.username(),
                PasswordService.getPasswordHash(registerData.password())
            )
        );

        return result
            ? new ServiceResult(true, null)
            : new ServiceResult(false, "something went wrong");
    }

    @Override
    public ServiceResult login(LoginDto loginData) {
        if (
            loginData.username() == null ||
            loginData.password() == null
        ) {
            return new ServiceResult(false, "invalid credentials");
        }

        User user = this.userRepository.getByCondition(
            String.format(
                "username = '%s'",
                loginData.username()
            )
        ).getFirst();

        boolean isPasswordValid = PasswordService.verifyPasswordHash(
            loginData.password(),
            user.password()
        );

        if (!isPasswordValid) {
            return new ServiceResult(false, "invalid credentials");
        }

        String jwt = JwtService.createJwt(user.id(), new String[]{user.role().toString()});

        return new ServiceResult(true, jwt);
    }

    @Override
    public List<User> getAll() {
        return this.userRepository.getAll();
    }

    @Override
    public ServiceResult delete(String id) {
        return this.userRepository.delete(id)
            ? new ServiceResult(true, null)
            : new ServiceResult(false, "something went wrong");
    }
}
