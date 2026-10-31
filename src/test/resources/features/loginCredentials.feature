Feature: Login Credentials

Scenario: Login with credentials
    Given I open the login website
    Then Enter "standard_user" & "secret_sauce"
    Then click on submit button
