package org.example.movierentals.server.service;

import org.example.movierentals.common.IClientService;
import org.example.movierentals.common.domain.Client;
import org.example.movierentals.common.domain.exceptions.ClientNotFoundException;
import org.example.movierentals.common.domain.exceptions.MovieRentalsException;
import org.example.movierentals.server.repository.ClientDBRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class SClientServiceImpl implements IClientService {
    private final ClientDBRepository clientRepository;

    public SClientServiceImpl(ClientDBRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public Iterable<Client> getAllClients() throws MovieRentalsException {
        return clientRepository.findAll();
    }

    @Override
    public void addClient(Client client) {
        clientRepository.save(client);
    }

    @Override
    public Client getClientById(Long id) {
        Optional<Client> clientOptional = clientRepository.findOne(id);
        if (clientOptional.isPresent()) {
            return clientRepository.findOne(id).get();
        } else {
            throw new ClientNotFoundException("There is no Client with Id: " + id);
        }
    }

    @Override
    public void updateClient(Client client) {
        clientRepository.update(client);
    }

    @Override
    public void deleteClientById(Long id) {
        Optional<Client> clientToDeleteOpt = clientRepository.delete(id);

        if (!clientToDeleteOpt.isPresent()) {
            throw new MovieRentalsException("Client with ID " + id + " not found.");
        }
    }

    @Override
    public Iterable<Client> filterClientsByKeyword(String keyword) {
        try {
            Iterable<Client> clients = clientRepository.findAll();
            return StreamSupport.stream(clients.spliterator(), false)
                    .filter(client -> client.getFirstName().toLowerCase().contains(keyword.toLowerCase())
                            || client.getLastName().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toSet());
        } catch (MovieRentalsException e) {
            throw new MovieRentalsException(e.getMessage());
        }
    }
}
