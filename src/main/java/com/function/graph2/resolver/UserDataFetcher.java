package com.function.graph2.resolver;

import java.util.List;

import com.function.graph2.model.User;
import com.function.graph2.service.UserService;

import graphql.schema.DataFetcher;

public class UserDataFetcher {

    private final UserService userService = new UserService();

    public DataFetcher<List<User>> getAllUsersFetcher() {
        return environment -> userService.getAllUsers();
    }

    public DataFetcher<String> createUserFetcher() {
        return environment -> {
            String name = environment.getArgument("name");
            String email = environment.getArgument("email");
            String password = environment.getArgument("password");
            User user = new User(name, email, password);
            userService.createUser(user);
            return "User created successfully";
        };
    }

    public DataFetcher<String> updateUserFetcher() {
        return environment -> {
            Long id = Long.valueOf(environment.getArgument("id"));
            String name = environment.getArgument("name");
            String email = environment.getArgument("email");
            String password = environment.getArgument("password");
            User user = new User(id, name, email, password);
            userService.updateUser(id, user);
            return "User updated successfully";
        };
    }

    public DataFetcher<String> deleteUserFetcher() {
        return environment -> {
            Long id = Long.valueOf(environment.getArgument("id"));
            userService.deleteUser(id);
            return "User deleted successfully";
        };
    }

    public DataFetcher<List<User>> getUsersByEmailFetcher() {
        return environment -> {
            String email = environment.getArgument("email");
            return userService.getUsersByEmail(email); // este método lo defines tú
        };
    }
}