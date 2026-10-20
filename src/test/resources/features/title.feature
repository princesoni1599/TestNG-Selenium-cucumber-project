Feature: Website title

  Scenario: Open Selenium website
    Given I open the Selenium website
    Then the page title contains "Selenium"
    Then Api response test