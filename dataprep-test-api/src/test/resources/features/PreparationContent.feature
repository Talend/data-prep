@EnvOS @EnvOnPremise @EnvCloud
Feature: Open preparation multiple times

  @CleanAfter
  Scenario: Open 101 time a preparation
    Given I upload the dataset "/data/8L3C.csv" with name "8L3C_dataset"
    And I wait for the dataset "8L3C_dataset" metadata to be computed
    And I create a preparation with name "8L3C_preparation", based on "8L3C_dataset" dataset
    Then I check that I can load "101" times the preparation with name "8L3C_preparation"
