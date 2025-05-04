package mephi.repository.otp;

import mephi.entities.otp.Otp;
import mephi.entities.otp.OtpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OtpRepositoryImpl implements OtpRepository {
    private static final Logger logger = LoggerFactory.getLogger(OtpRepositoryImpl.class);

    private final Connection connection;

    public OtpRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<Otp> getByCondition(String condition) {
        List<Otp> otpFoundList = new ArrayList<>();

        try (Statement statement = this.connection.createStatement()) {
            String sql = String.format("select * from otp_codes where %s", condition);

            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                otpFoundList.add(
                    new Otp(
                        resultSet.getString("id"),
                        resultSet.getString("value"),
                        resultSet.getLong("created_at"),
                        OtpStatus.valueOf(resultSet.getString("status")),
                        resultSet.getString("user_id"),
                        resultSet.getString("operation_id")
                    )
                );
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return otpFoundList;
    }

    @Override
    public boolean create(Otp item) {
        String sql = "insert into otp_codes(id, value, created_at, status, user_id, operation_id) values (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            preparedStatement.setString(1, item.id());
            preparedStatement.setString(2, item.value());
            preparedStatement.setLong(3, item.createdAt());
            preparedStatement.setString(4, item.status().toString());
            preparedStatement.setString(5, item.userId());
            preparedStatement.setString(6, item.operationId());

            preparedStatement.execute();

            return true;
        }
        catch (SQLException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(Otp item) {
        String sql = "update otp_codes set status = ? where id = ?";
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            preparedStatement.setString(1, item.status().toString());
            preparedStatement.setString(2, item.id());

            preparedStatement.execute();

            return true;
        }
        catch (SQLException e) {
            logger.error(e.getMessage());
            return false;
        }
    }
}
