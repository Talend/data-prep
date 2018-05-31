@EnvOs @EnvOnPremise @EnvCloud
Feature: Perform scenarios with CreateNewColumn related action

  Scenario: Create a new column with generate sequence mode on a dataset
    Given I upload the dataset "/data/customers.csv" with name "customers_dataset"
    Given I create a preparation with name "customers_prep", based on "customers_dataset" dataset
    Given I add a "create_new_column" step identified by "createNewColumn" on the preparation "customers_prep" with parameters :
      | column_id               | 0002              |
      | mode_new_column         | sequence_mode     |
      | create_new_column_name  | myperfectsequence |
      | start_value             | 1                 |
      | step_value              | 1                 |
    Then I check that a step like "createNewColumn" exists in the preparation "customers_prep"

  @CleanAfter
  Scenario: Export customers_prep and check the exported file customers_prep_result.csv
    When I export the preparation with parameters :
      | preparationName      | customers_prep            |
      | dataSetName          | customers_dataset         |
      | exportType           | CSV                       |
      | fileName             | customers_prep_result.csv |
      | csv_escape_character | "                         |
      | csv_enclosure_char   | "                         |
    Then I check that "customers_prep_result.csv" temporary file equals "/data/customers_prep_exported.csv" file
