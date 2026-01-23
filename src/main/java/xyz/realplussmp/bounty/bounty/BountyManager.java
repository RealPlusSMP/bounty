package xyz.realplussmp.bounty.bounty;

import xyz.realplussmp.bounty.database.Database;

import java.sql.*;
import java.util.*;

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

    public Map<UUID, Double> getAllBounties() {
        Map<UUID, Double> result = new LinkedHashMap<>();

        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT uuid, amount FROM bounties ORDER BY amount DESC");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                double amount = rs.getDouble("amount");
                result.put(uuid, amount);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public List<Map.Entry<UUID, Double>> getAllBountiesSortedByRecent() {
        List<Map.Entry<UUID, Double>> result = new ArrayList<>();

        try (Connection con = database.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT uuid, amount FROM bounties ORDER BY created_at DESC");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                double amount = rs.getDouble("amount");
                result.add(Map.entry(uuid, amount));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }
}