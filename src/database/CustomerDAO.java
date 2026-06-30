package database;

import logic.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomerDAO {

    private static final Logger LOGGER =
            Logger.getLogger(CustomerDAO.class.getName());

    private final Connection connection;

    public CustomerDAO(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }
        this.connection = connection;
    }

    public boolean save(Customer customer) {
        String sql =
                "INSERT INTO customers(pno, first_name, last_name) " +
                "VALUES (?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, customer.getpNo());
            stmt.setString(2, customer.getName());
            stmt.setString(3, customer.getSurname());
            return stmt.executeUpdate() == 1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to save customer pNo=" + customer.getpNo(), e);
            throw new DatabaseException("Failed to save customer", e);
        }
    }

    public Customer findByPNo(String pNo) {
        String sql = "SELECT * FROM customers WHERE pno = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, pNo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Customer(
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("pno")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find customer pNo=" + pNo, e);
            throw new DatabaseException("Failed to find customer " + pNo, e);
        }
        return null;
    }

    public boolean exists(String pNo) {
        String sql = "SELECT 1 FROM customers WHERE pno = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, pNo);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed existence check for pNo=" + pNo, e);
            throw new DatabaseException("Failed to check customer existence " + pNo, e);
        }
    }

    public boolean updateName(String pNo, String firstName, String lastName) {
        String sql =
                "UPDATE customers SET first_name = ?, last_name = ? " +
                "WHERE pno = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, pNo);
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                LOGGER.warning("Name update affected 0 rows for pNo=" + pNo);
                return false;
            }
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update name for pNo=" + pNo, e);
            throw new DatabaseException("Failed to update customer name " + pNo, e);
        }
    }

    public boolean delete(String pNo) {
        String sql = "DELETE FROM customers WHERE pno = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, pNo);
            int affected = stmt.executeUpdate();
            if (affected == 0) {
                LOGGER.warning("Delete affected 0 rows for pNo=" + pNo);
                return false;
            }
            LOGGER.info("Deleted customer pNo=" + pNo);
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete customer pNo=" + pNo, e);
            throw new DatabaseException("Failed to delete customer " + pNo, e);
        }
    }

    /**
     * Returns all customers, strictly ordered by first name, last name, and pno
     * to ensure index consistency for the GUI.
     */
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM customers ORDER BY first_name, last_name, pno";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                customers.add(new Customer(
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("pno")
                ));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get all customers", e);
            throw new DatabaseException("Failed to retrieve all customers", e);
        }
        return customers;
    }
}