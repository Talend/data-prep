@EnvOS @EnvOnPremise @EnvCloud
Feature: Perform scenarios with DeleteAllEmptyColumns related action

  @CleanAfter
  Scenario Outline: Apply the DeleteAllEmptyColumns action and test export
    Given I upload the dataset "/data/dataset_with_empty_columns.csv" with name "dataset_with_empty_columns_dataset"
    Given I create a preparation with name "dataset_with_empty_columns_prep", based on "dataset_with_empty_columns_dataset" dataset
    Given I add a "delete_all_empty_columns" step identified by "deleteAllEmptyColumns" on the preparation "dataset_with_empty_columns_prep" with parameters :
      | column_id                    | 0000    |
      | scope                        | dataset |
      | action_on_columns_with_blank | <param> |
    Given I add a "delete_column" step identified by "deleteFirstname" on the preparation "dataset_with_empty_columns_prep" with parameters :
      | scope       | column    |
      | column_name | firstname |
      | column_id   | 0001      |
    Then I check that a step like "deleteAllEmptyColumns" exists in the preparation "dataset_with_empty_columns_prep"
    When I export the preparation with parameters :
      | exportType           | CSV                                |
      | preparationName      | dataset_with_empty_columns_prep    |
      | dataSetName          | dataset_with_empty_columns_dataset |
      | fileName             | <csv_name>                         |
      | csv_escape_character | "                                  |
      | csv_enclosure_char   | "                                  |
    Then I check that "<csv_name>" temporary file equals "/data/<csv_name>" file

    Examples:
      | param  | csv_name                                 |
      | keep   | dataset_without_empty_columns_keep.csv   |
      | delete | dataset_without_empty_columns_delete.csv |
