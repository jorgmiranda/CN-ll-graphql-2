package com.function.graph2.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.function.graph2.connection.DatabaseConnection;
import com.function.graph2.model.Role;
import com.function.graph2.model.User;

public class UserService {
    // Crear un nuevo usuario
    public void createUser(User user) throws SQLException {
        String sql = "INSERT INTO usuarios (name, email, password) VALUES (?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.executeUpdate();
        }
    }

    // Obtener todos los usuarios
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM usuarios";

        try (Connection connection = DatabaseConnection.getConnection();
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Long id = rs.getLong("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String password = rs.getString("password");

                User user = new User(id, name, email, password);
                users.add(user);
            }
        }
        return users;
    }

    // Actualizar un usuario
    public void updateUser(Long id, User user) throws SQLException {
        String sql = "UPDATE usuarios SET name = ?, email = ?, password = ? WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setLong(4, id);
            stmt.executeUpdate();
        }
    }

    // Eliminar un usuario
    public void deleteUser(Long id) throws SQLException {
        String deleteRolesSql = "DELETE FROM user_roles WHERE user_id = ?";
        String deleteUserSql = "DELETE FROM usuarios WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false); // Iniciar transacción manual

            try (PreparedStatement deleteRolesStmt = connection.prepareStatement(deleteRolesSql);
                    PreparedStatement deleteUserStmt = connection.prepareStatement(deleteUserSql)) {

                // 1. Borrar de user_roles primero
                deleteRolesStmt.setLong(1, id);
                deleteRolesStmt.executeUpdate();

                // 2. Borrar de usuarios
                deleteUserStmt.setLong(1, id);
                deleteUserStmt.executeUpdate();

                connection.commit(); 
            } catch (SQLException e) {
                connection.rollback(); 
                throw e;
            } finally {
                connection.setAutoCommit(true); 
            }
        }
    }

    public List<User> getUsersByEmail(String email) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM usuarios WHERE email = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Long id = rs.getLong("id");
                String name = rs.getString("name");
                String password = rs.getString("password");

                User user = new User(id, name, email, password);

                // Agregar roles al usuario
                List<Role> roles = getRolesByUserId(id);
                user.setRoles(roles);

                users.add(user);
            }
        }
        return users;
    }

    // Buscar un usuario por su ID
    public User getUserById(Long id) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String name = rs.getString("name");
                String email = rs.getString("email");
                String password = rs.getString("password");

                User user = new User(id, name, email, password);

                // Agregar roles al usuario
                List<Role> roles = getRolesByUserId(id);
                user.setRoles(roles);

                return user;
            } else {
                return null; // No se encontró el usuario
            }
        }
    }

    // Obtener roles de un usuario
    private List<Role> getRolesByUserId(Long userId) throws SQLException {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT r.ID, r.ROLE_NAME FROM roles r " +
                "JOIN user_roles ur ON r.id = ur.role_id " +
                "WHERE ur.user_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
                PreparedStatement stmt = connection.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Long roleId = rs.getLong("id");
                String roleName = rs.getString("ROLE_NAME");
                roles.add(new Role(roleId, roleName));
            }
        }
        return roles;
    }
}
