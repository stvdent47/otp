package mephi.repository.user;

import mephi.entities.user.User;
import mephi.entities.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserRepositoryImpl implements UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepositoryImpl.class);

    private final Connection connection;

    public UserRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<User> getByCondition(String condition) {
        List<User> usersFoundList = new ArrayList<>();

        try (Statement statement = this.connection.createStatement()) {
            String sql = String.format("select * from users where %s", condition);

            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                usersFoundList.add(
                    new User(
                        resultSet.getString("id"),
                        UserRole.valueOf(resultSet.getString("role")),
                        resultSet.getString("username"),
                        resultSet.getString("password")
                    )
                );
            }

        }
        catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return usersFoundList;
    }

    @Override
    public List<User> getAll() {
        List<User> userList = new ArrayList<>();

        try (Statement statement = this.connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select * from users where role = 'user'");

            while (resultSet.next()) {
                userList.add(
                    new User(
                        resultSet.getString("id"),
                        UserRole.valueOf(resultSet.getString("role")),
                        resultSet.getString("username"),
                        resultSet.getString("password")
                    )
                );
            }

        }
        catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return userList;
    }

    @Override
    public boolean create(User user) {
        String sql = "insert into users(id, username, password, role) values(?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.id());
            preparedStatement.setString(2, user.username());
            preparedStatement.setString(3, user.password());
            preparedStatement.setString(4, user.role().toString());

            preparedStatement.execute();

            return true;
        }
        catch (SQLException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(String id) {
        String sql = "delete from users where id = ?";
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            preparedStatement.setString(1, id);

            int rowAffected = preparedStatement.executeUpdate();

            return rowAffected > 0;
        }
        catch (SQLException e) {
            logger.error(e.getMessage());
            return false;
        }
    }
}
