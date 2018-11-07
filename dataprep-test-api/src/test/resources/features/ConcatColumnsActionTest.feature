# would fail while ConcatColumns action is desable
@EnvOnPremise @EnvCloud
Feature: Export all data from a preparation

  Scenario: add a multi_columns action
    Given I upload the dataset "/data/10L3C.csv" with name "10L3C_dataset"
    Then I wait for the dataset "10L3C_dataset" metadata to be computed
    Given I create a preparation with name "10L3C_prep", based on "10L3C_dataset" dataset
    Given I add a "concat_columns" step identified by "concatColumns" on the preparation "10L3C_prep" with parameters :
      | column_ids | ["0000","0001"]  |
      | scope      | multi_columns    |
    Then I check that a step like "concatColumns" exists in the preparation "10L3C_prep"

  Scenario: Export 10L3C_prep preparation and check the exported file 10L3C_result.csv
  # escape and enclosure characters should be given because they can be empty
    When I export the preparation with parameters :
      | preparationName      | 10L3C_prep       |
      | exportType           | CSV              |
      | fileName             | 10L3C_result.csv |
      | csv_escape_character | "                |
      | csv_enclosure_char   | "                |
    Then I check that "10L3C_result.csv" temporary file equals "/data/10L3C_ConcatColumns.csv" file
