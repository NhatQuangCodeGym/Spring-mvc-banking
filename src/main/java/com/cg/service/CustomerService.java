package com.cg.service;

import com.cg.model.Customer;
import com.cg.model.Deposit;
import com.cg.model.Transfer;
import com.cg.model.Withdraw;

import java.util.List;


public interface CustomerService extends IGeneralService<Customer> {

    List<Customer> findAllByDeletedIsFalse();

    void incrementBalance(Long customerId, Deposit deposit);

    void reduceBalance(Long customerId, Withdraw withdraw);

    void transferBalance(Long senderId, Long recipientId, Transfer transfer);

    List<Customer> findAllByIdNot(Long id);

    List<Customer> findAllByIdNotAndDeletedIsFalse(Long id);


}
