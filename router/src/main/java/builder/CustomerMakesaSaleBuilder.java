/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package builder;

import Maker.CustomerUpdater;
import Maker.GroupConverter;
import domain.Customer;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

/**
 *
 * @author keiranpatel
 */
public class CustomerMakesaSaleBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // from new-sale
        //grabs attributes, ie id,group and more 
        from("jms:queue:new-sale")
                .setProperty("customer_id").jsonpath("$.customer.id")
                .setProperty("customer_group").jsonpath("$.customer.customer_group_id")//grab id, entire customer
                .setProperty("customer_firstName").jsonpath("$.customer.first_name")
                .setProperty("customer_lastName").jsonpath("$.customer.last_name")
                .setProperty("customer_email").jsonpath("$.customer.email")
                .setProperty("customer_code").jsonpath("$.customer.customer_code")
                .log("${body}")
                .to("jms:queue:sale");

        //extract customer and store in property
        //extract group and store in property
        //extract customer ID and store in property
        //send sale to sales service (REST POST) step 2
        from("jms:queue:sale")
                //.log("POST to sales API: {$body}")
                .removeHeaders("*")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .to("http://localhost:8083/api/sales")
                .convertBodyTo(String.class)
                .to("jms:queue:http-response");

        //send GET to sales service for summary for customer based on ID step 3
        from("jms:queue:http-response")
                .removeHeaders("*")
                .setBody().constant(null)
                .setHeader(Exchange.HTTP_METHOD, constant("GET"))
                .toD("http://localhost:8083/api/sales/customer/${exchangeProperty.customer_id}/summary")
                .convertBodyTo(String.class)
                .setProperty("summary_group").jsonpath("$.group")
                .setProperty("summary_group").method(GroupConverter.class, "crasToVend(${exchangeProperty.summary_group})")
                .to("jms:queue:summary-response");
        //content-based-router has group changed
        //thus if changed, get cusomer out of the property
        //send changed Group mutation to accounts service
        from("jms:queue:summary-response")
                .choice().when().simple("${exchangeProperty.customer_group} == ${exchangeProperty.summary_group}")
                .to("jms:queue:no-update-required")
                .otherwise()
                .to("jms:queue:update-required");

        from("jms:queue:update-required")
                .multicast().to("jms:queue:update-vend-customer", "jms:queue:update-accounts-customer");
        //mutation with grapgh QL 
        from("jms:queue:update-accounts-customer")
                .log("Group changed - Updating accounts service")
                .toD("graphql://http://localhost:8082/graphql?query=mutation{changeGroup(id: \"${exchangeProperty.customer_id}\", newGroup: \"${exchangeProperty.summary_group}\") { id   email    username    firstName    lastName    group  }}").to("jms:queue:change-group-response")
                .to("jms:queue:update-accounts-response");
        //send PUT to Vend using .bean
        from("jms:queue:update-vend-customer")
                .log("Group changed - Updating Vend service")
                .bean(CustomerUpdater.class, "updateCustomer(${exchangeProperty.customer_id}, ${exchangeProperty.customer_firstName}, ${exchangeProperty.customer_lastName},${exchangeProperty.customer_code},${exchangeProperty.customer_email}, ${exchangeProperty.summary_group})")
                .marshal().json(JsonLibrary.Gson)
                //.log("${body}")
                .removeHeaders("*")
                .setHeader(Exchange.HTTP_METHOD, constant("PUT"))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setHeader("Authorization", constant("Bearer KiQSsELLtocyS2WDN5w5s_jYaBpXa0h2ex1mep1a"))
                .toD("https://info303otago.vendhq.com/api/2.0/customers/${exchangeProperty.customer_id}")
                .to("jms:queue:update-vend-response");
    }
}
