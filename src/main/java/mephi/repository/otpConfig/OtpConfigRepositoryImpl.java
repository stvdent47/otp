package mephi.repository.otpConfig;

import mephi.entities.otp.OtpConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OtpConfigRepositoryImpl implements OtpConfigRepository {
    private static final Logger logger = LoggerFactory.getLogger(OtpConfigRepositoryImpl.class);

    private final Connection connection;

    public OtpConfigRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<OtpConfig> getAll() {
        List<OtpConfig> otpConfigList = new ArrayList<>();

        try (Statement statement = this.connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("select * from otp_config");

            while (resultSet.next()) {
                otpConfigList.add(
                    new OtpConfig(
                        resultSet.getString("id"),
                        resultSet.getInt("length"),
                        resultSet.getLong("expiration")
                    )
                );
            }
        }
        catch (SQLException e) {
            logger.error(e.getMessage());
        }

        return otpConfigList;
    }

    @Override
    public boolean update(OtpConfig item) {
        String sql = "update otp_config set length = ?, expiration = ? where id = ?";
        try (PreparedStatement preparedStatement = this.connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, item.length());
            preparedStatement.setLong(2, item.expiration());
            preparedStatement.setString(3, item.id());

            preparedStatement.execute();

            return true;
        }
        catch (SQLException e) {
            logger.error(e.getMessage());
            return false;
        }
    }
}
