/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/javascript.js to edit this template
 */
/* global Vue */

var accountApi = 'http://localhost:8090/api/newAccounts';

const app = Vue.createApp({

    data() {
        return {
            account: new Object()
        }
    },

    methods: {
        createAccount() {
            axios.post(accountApi, this.account)
                    .then(() => {
                        alert("Account has been created.");
                    })
                    .catch(error => {
                        console.error(error);
                        alert("An error occurred - check the console for details.");
                    });
        }

    }
});

// mount the page at the <main> tag - this needs to be the last line in the file
app.mount("main");

