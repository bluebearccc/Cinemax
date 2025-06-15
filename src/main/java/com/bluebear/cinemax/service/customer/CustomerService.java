package com.bluebear.cinemax.service.customer;

import com.bluebear.cinemax.dto.CustomerDTO;
import com.bluebear.cinemax.entity.Customer;

import java.util.List;

public interface CustomerService {
    CustomerDTO save(CustomerDTO dto);

    CustomerDTO findById(Integer id);

    List<CustomerDTO> findAll();

    void deleteById(Integer id);

    CustomerDTO getUserByAccountID(Integer accountId);

    CustomerDTO entityToDto(Customer customer);

    Customer dtoToEntity(CustomerDTO dto);
}
