@OnPremise
Feature: Dataset cache features

  @CleanAfter
  Scenario: Clean preparation dataset when updating a dataset used by a preparation
    Given I upload the dataset "/data/8L3C.csv" with name "8L3C_dataset"
    And I create a preparation with name "8L3C_preparation", based on "8L3C_dataset" dataset
    And I add a step with parameters :
      | actionName      | uppercase        |
      | columnName      | firstname        |
      | columnId        | 0001             |
      | preparationName | 8L3C_preparation |
    When I update the dataset named "8L3C_dataset" with data "/data/10L3C.csv"
    Then I check that the content of preparation "8L3C_preparation" equals "/data/10L3C_lastname_uppercase.csv" file which have ";" as delimiter
