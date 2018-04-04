Feature: Perform scenarios with DeleteAllEmptyColumns related action

  Scenario: Apply the DeleteAllEmptyColumns action
    Given I upload the dataset "/data/dataset_with_empty_columns.csv" with name "dataset_with_empty_columns_dataset"
    Given I create a preparation with name "dataset_with_empty_columns_prep", based on "dataset_with_empty_columns_dataset" dataset
    Given I add a "delete_all_empty_columns" step identified by "deleteAllEmptyColumns" on the preparation "dataset_with_empty_columns_prep" with parameters :
      | column_id                    | 0000     |
      | scope                        | dataset  |
      | action_on_columns_with_blank | delete   |
    Then I check that a step like "deleteAllEmptyColumns" exists in the preparation "dataset_with_empty_columns_prep"

  Scenario: Export and check the exported file
    When I export the preparation with parameters :
      | exportType           | CSV                                    |
      | preparationName      | dataset_with_empty_columns_prep        |
      | dataSetName          | dataset_with_empty_columns_dataset     |
      | fileName             | dataset_with_empty_columns_result.csv  |
      | csv_escape_character | "                                      |
      | csv_enclosure_char   | "                                      |
    Then I check that "dataset_with_empty_columns_result.csv" temporary file equals "/data/dataset_without_empty_columns.csv" file