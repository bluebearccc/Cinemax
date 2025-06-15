package com.bluebear.cinemax.service.customer;

import com.bluebear.cinemax.dto.CustomerDTO;
import com.bluebear.cinemax.entity.Account;
import com.bluebear.cinemax.entity.Customer;
import com.bluebear.cinemax.repository.AccountRepository;
import com.bluebear.cinemax.repository.CustomerRepository;
import com.bluebear.cinemax.service.moviefeedback.MovieFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private MovieFeedbackService movieFeedbackService;

    public CustomerDTO entityToDto(Customer customer) {
        if (customer == null) return null;

        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setAccountID(customer.getAccount() != null ? customer.getAccount().getId() : null);
        dto.setFullName(customer.getFullName());
        dto.setPhone(customer.getPhone());
        return dto;
    }

    public Customer dtoToEntity(CustomerDTO dto) {
        if (dto == null) return null;

        Customer customer = new Customer();
        customer.setId(dto.getId());
        customer.setFullName(dto.getFullName());
        customer.setPhone(dto.getPhone());

        if (dto.getAccountID() != null) {
            Optional<Account> optional = accountRepository.findById(dto.getAccountID());
            optional.ifPresent(customer::setAccount);
        }

        if (dto.getMovieFeedback() != null) {
            customer.setFeedbackList(dto.getMovieFeedback().stream().map(movieFeedbackDTO -> movieFeedbackService.fromDTO(movieFeedbackDTO)).collect(Collectors.toList()));
        }
        return customer;
    }

    public CustomerDTO save(CustomerDTO dto) {
        Customer entity = dtoToEntity(dto);
        Customer saved = customerRepository.save(entity);
        return entityToDto(saved);
    }

    public CustomerDTO findById(Integer id) {
        return customerRepository.findById(id)
                .map(this::entityToDto)
                .orElse(null);
    }

    public List<CustomerDTO> findAll() {
        List<CustomerDTO> dtos = new ArrayList<>();
        for (Customer c : customerRepository.findAll()) {
            dtos.add(entityToDto(c));
        }
        return dtos;
    }

    public void deleteById(Integer id) {
        customerRepository.deleteById(id);
    }

    public CustomerDTO getUserByAccountID(Integer accountId) {
        return customerRepository.findByAccount_Id(accountId)
                .map(this::entityToDto)
                .orElse(null);
    }
}