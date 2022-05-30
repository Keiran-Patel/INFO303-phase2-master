/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package builder;

import Maker.DomainConverter;
import domain.Account;
import domain.Customer;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;

/**
 *
 * @author keiranpatel
 */
public class CustomerCreatesAccountBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        // create HTTP endpoint for receiving messages via HTTP
        from("jetty:http://localhost:8090/api/newAccounts?enableCORS=true")
                // make message in-only so web browser doesn't have to wait on a non-existent response
                .setExchangePattern(ExchangePattern.InOnly)
                .convertBodyTo(String.class)
                .log("${body}")
                .to("jms:queue:newAccount");

        from("jms:queue:newAccount")
                .unmarshal().json(JsonLibrary.Gson, Account.class)
                .log("${body}")
                .to("jms:queue:newAccountObject");

        from("jms:queue:newAccountObject")
                .bean(DomainConverter.class, "accountToCustomer(${body})")
                .log("${body}")
                .to("jms:queue:newCustomerObject");

        from("jms:queue:newCustomerObject")
                .marshal().json(JsonLibrary.Gson)
                .removeHeaders("*")
                .setHeader("Authorization", constant("Bearer KiQSsELLtocyS2WDN5w5s_jYaBpXa0h2ex1mep1a"))
                .setHeader(Exchange.CONTENT_TYPE).constant("application/json")
                .setHeader(Exchange.HTTP_METHOD, constant("POST"))
                // send it
                .to("https://info303otago.vendhq.com/api/2.0/customers?throwExceptionOnFailure=false")
                // handle response
                .choice()
                .when().simple("${header.CamelHttpResponseCode} == '201'") // change to 200 for PUT
                .convertBodyTo(String.class)
                .to("jms:queue:vend-customers-response")
                .otherwise()
                .log("ERROR RESPONSE ${header.CamelHttpResponseCode} ${body}")
                .convertBodyTo(String.class)
                .to("jms:queue:vend-customers-error")
                .endChoice();

        from("jms:queue:vend-customers-response")
                .setBody().jsonpath("$.data")
                .marshal().json(JsonLibrary.Gson)
                .log("${body}")
                .unmarshal().json(JsonLibrary.Gson, Customer.class)
                .log("${body}")
                .to("jms:queue:sendToAccountsService");

        from("jms:queue:sendToAccountsService")
                .toD("graphql://http://localhost:8082/graphql?query=mutation{addAccount(account: {id: \"${body.id}\", email: \"${body.email}\", username: \"${body.customerCode}\", firstName:\"${body.firstName}\", lastName: \"${body.lastName}\", group: \"${body.group}\"}) {id email username firstName lastName group}}")
                .log("GraphQL service called")
                .to("jms:queue:accounts-service-response");
    }
}
