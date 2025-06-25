package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ProfileDao;
import org.yearup.models.Profile;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MySqlProfileDao extends MySqlDaoBase implements ProfileDao {
    public MySqlProfileDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Profile create(Profile profile) {
        String sql = "INSERT INTO profiles (user_id, first_name, last_name, phone, email, address, city, state, zip) " +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setInt(1, profile.getUserId());
            ps.setString(2, profile.getFirstName());
            ps.setString(3, profile.getLastName());
            ps.setString(4, profile.getPhone());
            ps.setString(5, profile.getEmail());
            ps.setString(6, profile.getAddress());
            ps.setString(7, profile.getCity());
            ps.setString(8, profile.getState());
            ps.setString(9, profile.getZip());

            ps.executeUpdate();

            return profile;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves the profile information for a specific user ID.
     *
     * @param userId the ID of the user whose profile should be retrieved
     * @return the Profile object populated from the database, or null if not found
     */
    @Override
    public Profile getByUserId(int userId) {
        String sql = """
                SELECT *
                FROM profiles
                WHERE user_id = ?
                """;
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {

                // Create a new Profile object and populate it with data from the result set
                Profile profile = new Profile();
                profile.setUserId(rs.getInt("user_id"));
                profile.setFirstName(rs.getString("first_name"));
                profile.setLastName(rs.getString("last_name"));
                profile.setPhone(rs.getString("phone"));
                profile.setEmail(rs.getString("email"));
                profile.setAddress(rs.getString("address"));
                profile.setCity(rs.getString("city"));
                profile.setState(rs.getString("state"));
                profile.setZip(rs.getString("zip"));

                return profile;
            }
            return null; // No profile found for this userId
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Profile profile) {

        String sql = """
                UPDATE profiles
                SET first_name = ?
                , last_name = ?
                , phone = ?
                , email = ?
                , address = ?
                , city = ?
                , state = ?
                ,zip = ?
                WHERE user_id = ?
                """;

        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            // Set new values from the Profile object
            ps.setString(1, profile.getFirstName());        //first_name = ?
            ps.setString(2, profile.getLastName());        //last_name = ?
            ps.setString(3, profile.getPhone());          //phone = ?
            ps.setString(4, profile.getEmail());         //email = ?
            ps.setString(5, profile.getAddress());      //address = ?
            ps.setString(6, profile.getCity());        //city = ?
            ps.setString(7, profile.getState());      //state = ?
            ps.setString(8, profile.getZip());       //zip = ?
            ps.setInt(9, profile.getUserId());      //user_id = ?

            // Execute the update
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
