package com.skillexchange.dao;

import com.skillexchange.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface UserDao {

    User create(User user, String passwordHash) throws SQLException;

    Optional<User> findById(int id) throws SQLException;

    Optional<User> findByEmail(String email) throws SQLException;

    String findPasswordHashByEmail(String email) throws SQLException;

    List<User> findAll() throws SQLException;

    List<User> findCandidateMentors(int excludingUserId) throws SQLException;
}
