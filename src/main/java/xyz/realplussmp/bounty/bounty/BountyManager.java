package xyz.realplussmp.bounty.bounty;

import xyz.realplussmp.bounty.database.Database;

import java.sql.*;
import java.util.UUID;

public class BountyManager {

    private final Database database;

    public BountyManager(Database database) {
        this.database = database;
    }

    public double getBounty(UUID uuid) {
        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT amount FROM bounties WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("amount");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setBounty(UUID uuid, double amount) {
        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "REPLACE INTO bounties(uuid, amount) VALUES (?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setDouble(2, amount);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeBounty(UUID uuid) {
        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM bounties WHERE uuid=?")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}