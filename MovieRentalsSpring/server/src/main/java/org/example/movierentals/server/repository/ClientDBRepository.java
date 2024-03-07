package org.example.movierentals.server.repository;

import org.example.movierentals.common.domain.Client;
import org.example.movierentals.common.domain.exceptions.MovieRentalsException;
import org.example.movierentals.common.domain.exceptions.ValidatorException;
import org.example.movierentals.common.domain.validators.ClientValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public class ClientDBRepository implements Repository<Long, Client> {
    @Autowired
    private JdbcOperations jdbcOperations;

    ClientValidator validator = new ClientValidator();

    @Override
    public Optional<Client> findOne(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Client ID must not be null");
        }
        String query = "SELECT * FROM clients WHERE id = ?";
        List<Client> clients = jdbcOperations.query(query, new Object[]{id}, (resultSet, i) -> {
            Client client = new Client();
            try {
                setFieldsOnClient(resultSet, client);
            } catch (ValidatorException | SQLException e) {
                throw new MovieRentalsException(e);
            }
            return client;
        });

        return clients.isEmpty() ? Optional.empty() : Optional.of(clients.get(0));
    }

    @Override
    public Iterable<Client> findAll() {
        String query = "SELECT * FROM clients";
        try {
            return jdbcOperations.query(query, (resultSet, i) -> {
                Client client = new Client();
                setFieldsOnClient(resultSet, client);
                return client;
            });
        } catch (DataAccessException | ValidatorException e) {
            throw new MovieRentalsException(e.getMessage());
        }
    }

    private void setFieldsOnClient(ResultSet resultSet, Client client) throws SQLException {
        Long resultId = resultSet.getLong("id");
        client.setIdClient(resultId);

        String firstName = resultSet.getString("first_name");
        client.setFirstName(firstName);

        String lastName = resultSet.getString("last_name");
        client.setLastName(lastName);

        String dateOfBirth = resultSet.getString("date_of_birth");
        client.setDateOfBirth(dateOfBirth);

        String email = resultSet.getString("email");
        client.setEmail(email);

        boolean subscribe = resultSet.getBoolean("subscribe");
        client.setSubscribe(subscribe);

        try {
            validator.validate(client);
        } catch (ValidatorException e) {
            throw new ValidatorException("There are not valid clients. ");
        }
    }

    @Override
    public Optional<Client> save(Client entity) throws ValidatorException {
        if (entity == null) {
            throw new IllegalArgumentException("Client must not be null");
        }
        validator.validate(entity); // Validate the client

        String sqlQuery = "INSERT INTO clients " +
                "(first_name, last_name, date_of_birth, email, subscribe) values " +
                "(?,?,?,?,?)";

        int rowsAffected = jdbcOperations.update(sqlQuery,
                entity.getFirstName(),
                entity.getLastName(),
                entity.getDateOfBirth(),
                entity.getEmail(),
                entity.isSubscribe());

        return rowsAffected == 1 ? Optional.of(entity) : Optional.empty();
    }

    @Override
    public Optional<Client> delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }
        Optional<Client> clientToDelete = findOne(id);

        if (clientToDelete.isPresent()) {
            String sqlString = "DELETE FROM clients WHERE id = ?";
            int rowsAffected = jdbcOperations.update(sqlString, id);
            if (rowsAffected == 0) {

                return Optional.empty();
            }
        }
        return clientToDelete;

    }

    @Override
    public Optional<Client> update(Client entity) throws ValidatorException {
        if (entity == null) {
            throw new IllegalArgumentException("Client must not be null");
        }
        validator.validate(entity); // Validate the client before updating

        String sqlString = "UPDATE clients SET first_name = ?, last_name = ?, date_of_birth = ?, email = ?, subscribe = ? WHERE id = ?";

        int rowsAffected = jdbcOperations.update(sqlString,
                entity.getFirstName(),
                entity.getLastName(),
                entity.getDateOfBirth(),
                entity.getEmail(),
                entity.isSubscribe(),
                entity.getIdClient());

        return rowsAffected > 0 ? Optional.of(entity) : Optional.empty();
    }

}
