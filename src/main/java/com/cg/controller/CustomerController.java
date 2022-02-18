package com.cg.controller;


import com.cg.model.Customer;
import com.cg.model.Deposit;
import com.cg.model.Transfer;
import com.cg.model.Withdraw;
import com.cg.service.CustomerService;
import com.cg.service.DepositService;
import com.cg.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private DepositService depositService;

    @Autowired
    private TransferService transferService;

    @GetMapping
    public ModelAndView showListPage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("customer/list");

        List<Customer> customers = customerService.findAllByDeletedIsFalse();

        modelAndView.addObject("customers", customers);

        return modelAndView;
    }

    @GetMapping("/create")
    public ModelAndView showCreatePage() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("customer/create");
        modelAndView.addObject("customer", new Customer());
        modelAndView.addObject("script",null);
        modelAndView.addObject("success",null);
        modelAndView.addObject("error",null);
        return modelAndView;
    }

    @GetMapping("/deposit/{id}")
    public ModelAndView showDepositPage(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("customer/deposit");

        Optional<Customer> customer = customerService.findById(id);

        modelAndView.addObject("customer", customer.get());

        modelAndView.addObject("deposit", new Deposit());

        return modelAndView;
    }

    @GetMapping("/withdraw/{id}")
    public ModelAndView showWithdrawPage(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("customer/withdraw");

        Optional<Customer> customer = customerService.findById(id);

        modelAndView.addObject("customer", customer.get());

        modelAndView.addObject("withdraw", new Withdraw());

        return modelAndView;
    }

    @GetMapping("/transfer/{id}")
    public ModelAndView showTransferPage(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("customer/transfer");

        modelAndView.addObject("transfer", new Transfer());

        Optional<Customer> sender = customerService.findById(id);

        if (sender.isPresent()) {
            List<Customer> recipients = customerService.findAllByIdNot(id);

            modelAndView.addObject("recipients", recipients);
            modelAndView.addObject("sender", sender.get());
        }
        else {
            modelAndView.addObject("sender", new Customer());
        }

        return modelAndView;
    }

    @GetMapping("/delete/{id}")
    public ModelAndView showDeletePage(@PathVariable Long id) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("customer/delete");

        Optional<Customer> customer = customerService.findById(id);

        modelAndView.addObject("customer", customer.get());

        return modelAndView;
    }

    @PostMapping("/create")
    public ModelAndView doCreate(@Validated @ModelAttribute Customer customer, BindingResult bindingResult) {
        ModelAndView modelAndView = new ModelAndView();

        if(bindingResult.hasErrors()){
//            return new ModelAndView("error")
            modelAndView.addObject("script",true);
        }
        else {
            customerService.save(customer);

            modelAndView.setViewName("customer/create");
            modelAndView.addObject("customer", new Customer());

            customerService.save(customer);
        }

        return modelAndView;
    }

    @PostMapping("/deposit/{customerId}")
    public ModelAndView doDeposit(@PathVariable Long customerId, @ModelAttribute Deposit deposit) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("customer/deposit");

        customerService.incrementBalance(customerId, deposit);

        Optional<Customer> customer = customerService.findById(customerId);

        if (customer.isPresent()) {
            modelAndView.addObject("customer", customer.get());
        }
        else {
            modelAndView.addObject("customer", null);
        }

        return modelAndView;
    }

    @PostMapping("/withdraw/{customerId}")
    public ModelAndView doWithdraw(@PathVariable Long customerId, @ModelAttribute Withdraw withdraw) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("customer/withdraw");

        Optional<Customer> customer = customerService.findById(customerId);

        if (customer.isPresent()) {

            BigDecimal currentBalance = customer.get().getBalance();
            BigDecimal transactionAmount = withdraw.getTransactionAmount();

            if (currentBalance.compareTo(transactionAmount) >= 0) {
                customerService.reduceBalance(customerId, withdraw);
            }

            modelAndView.addObject("customer", customerService.findById(customerId).get());
        }
        else {
            modelAndView.addObject("customer", null);
        }

        return modelAndView;
    }

    @PostMapping("/transfer/{senderId}")
    public ModelAndView doTransfer(@PathVariable Long senderId, @ModelAttribute Transfer transfer) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("customer/transfer");

        modelAndView.addObject("transfer", new Transfer());

        Optional<Customer> sender = customerService.findById(senderId);

        if (sender.isPresent()) {

            BigDecimal senderBalance = sender.get().getBalance();

            BigDecimal transferAmount = transfer.getTransferAmount();
            int fees = 10;
            BigDecimal feesAmount = transferAmount.multiply(BigDecimal.valueOf(fees)).divide(BigDecimal.valueOf(100));
            BigDecimal transactionAmount = transferAmount.add(feesAmount);

            if (senderBalance.compareTo(transactionAmount) >= 0) {
                transfer.setFees(fees);
                transfer.setFeesAmount(feesAmount);
                transfer.setTransactionAmount(transactionAmount);
                transfer.setSender(sender.get());

                transferService.save(transfer);

                customerService.transferBalance(senderId, transfer.getRecipient().getId(), transfer);
            }

            List<Customer> recipients = customerService.findAllByIdNot(senderId);

            modelAndView.addObject("recipients", recipients);
            modelAndView.addObject("sender", customerService.findById(senderId).get());
        }
        else {
            modelAndView.addObject("sender", null);
        }

        return modelAndView;
    }

    @PostMapping("/delete/{customerId}")
    public ModelAndView doDelete(@PathVariable Long customerId) {
        ModelAndView modelAndView = new ModelAndView();

        Optional<Customer> customer = customerService.findById(customerId);

        if (customer.isPresent()) {
            customer.get().setDeleted(true);
            customerService.save(customer.get());
            return new ModelAndView("redirect:/customers");
        }
        else {
            modelAndView.setViewName("customer/delete");
        }

        return modelAndView;
    }
}
