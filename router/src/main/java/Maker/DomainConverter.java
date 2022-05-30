/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Maker;

import domain.Account;
import domain.Customer;

/**
 *
 * @author keiranpatel
 */
public class DomainConverter {

    public Customer accountToCustomer(Account account) {
        Customer customer = new Customer();

        customer.setId(account.getId());
        customer.setCustomerCode(account.getUsername());
        customer.setEmail(account.getEmail());
        customer.setFirstName(account.getFirstName());
        customer.setLastName(account.getLastName());
        customer.setGroup("0afa8de1-147c-11e8-edec-2b197906d816");

        return customer;
    }
}
