@Unstable @Populate
Feature: Populate server with 1 prep with 200 col and 200 step

  Scenario: Populate server with 1 prep with 200 col and 200 step
    Given I upload a random dataset with 200 columns and 600 rows with name "TDP-5897"

#  Scenario: Populate server with 50 CSV - 100 preparation - 30 steps in each
#    Given I upload "50" times the dataset "/data/6L3C.csv" with name "simpleCSV"
#    Given I create "100" preparation "6L3C_preparation" with random dataset and "30" steps with parameters:
#      | column_name | firstname |
#      | column_id   | 0001      |
