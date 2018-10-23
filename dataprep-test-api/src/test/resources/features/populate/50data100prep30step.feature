@Unstable @Populate
Feature: Populate server with 50 CSV - 100 preparation - 30 steps in each

  Scenario: Populate server with 50 CSV - 100 preparation - 30 steps in each
    Given I upload "50" times the dataset "/data/6L3C.csv" with name "simpleCSV"
    Given I create "100" preparation "6L3C_preparation" with random dataset and "30" steps with parameters:
      | column_name | firstname |
      | column_id   | 0001      |
