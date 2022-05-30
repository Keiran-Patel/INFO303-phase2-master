/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Maker;

import domain.Customer;

/**
 *
 * @author keiranpatel
 */
public class CustomerUpdater {

    public Customer updateCustomer(String id, String firstName, String lastName, String customerCode, String email, String group) {
        Customer newcustomer = new Customer();
        newcustomer.setId(id);
        newcustomer.setCustomerCode(customerCode);
        newcustomer.setFirstName(firstName);
        newcustomer.setLastName(lastName);
        newcustomer.setEmail(email);
        newcustomer.setGroup(group);

        return newcustomer;
    }
}
