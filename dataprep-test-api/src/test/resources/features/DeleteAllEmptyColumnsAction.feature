@EnvOS @EnvOnPremise @EnvCloud
Feature: Perform scenarios with DeleteAllEmptyColumns related action

  @CleanAfter
  Scenario Outline: Apply the DeleteAllEmptyColumns action and test export
    Given I upload the dataset "/data/dataset_with_empty_columns.csv" with name "dataset_with_empty_columns_dataset"
    Then I wait for the dataset "dataset_with_empty_columns_dataset" metadata to be computed
    And I create a preparation with name "dataset_with_empty_columns_prep", based on "dataset_with_empty_columns_dataset" dataset
    And I add a "delete_all_empty_columns" step identified by "deleteAllEmptyColumns" on the preparation "dataset_with_empty_columns_prep" with parameters :
      | column_id                    | 0000    |
      | scope                        | dataset |
      | action_on_columns_with_blank | <param> |
    When I add a "delete_column" step identified by "deleteFirstname" on the preparation "dataset_with_empty_columns_prep" with parameters :
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


  Scenario: Apply the DeleteAllEmptyColumns action after other actions creating empty columns and test export
    Given I upload the dataset "/data/dataset_with_empty_columns.csv" with name "dataset_with_empty_columns_dataset"
    And I create a preparation with name "dataset_with_empty_columns_prep_2", based on "dataset_with_empty_columns_dataset" dataset
    When I add a "split" step identified by "splitFirstname" on the preparation "dataset_with_empty_columns_prep_2" with parameters :
      | separator   | e         |
      | limit       | 4         |
      | column_name | firstname |
      | column_id   | 0001      |
    Then The preparation "dataset_with_empty_columns_prep_2" should contain the following columns:
      | id | firstname | firstname_split_1 | firstname_split_2 | firstname_split_3 | firstname_split_4 | lastname | empty col | phonenumber | blanck col | city | iscustomer | mix blanck/empty col | mix blanck/empty col2 | one pattern col |
    When I add a "delete_all_empty_columns" step identified by "deleteAllEmptyColumns" on the preparation "dataset_with_empty_columns_prep_2" with parameters :
      | column_id                    | 0000    |
      | scope                        | dataset |
      | action_on_columns_with_blank | delete  |
    Then I check that a step like "deleteAllEmptyColumns" exists in the preparation "dataset_with_empty_columns_prep_2"
    And The preparation "dataset_with_empty_columns_prep_2" should contain the following columns:
      | id | firstname | firstname_split_1 | firstname_split_2 | firstname_split_3 | lastname | phonenumber | city | iscustomer | one pattern col |
    When I export the preparation with parameters :
      | exportType           | CSV                                      |
      | preparationName      | dataset_with_empty_columns_prep_2        |
      | dataSetName          | dataset_with_empty_columns_dataset       |
      | fileName             | delete_all_empty_columns_after_split.csv |
      | csv_escape_character | "                                        |
      | csv_enclosure_char   | "                                        |
    Then I check that "delete_all_empty_columns_after_split.csv" temporary file equals "/data/delete_all_empty_columns_after_split.csv" file


  # - apply the DeleteAllEmptyColumns action after an action creating empty columns
  # - delete the first action
  # - recreate it
  # - and move it back to its original place
  # At each step, check that the actions are correctly applied, at least by checking the columns
  @CleanAfter
  Scenario: Do some manipulations on the actions surrounding a DeleteAllEmptyColumns action and check columns
    Given A preparation with the following parameters exists :
      | preparationName | dataset_with_empty_columns_prep_2  |
      | dataSetName     | dataset_with_empty_columns_dataset |
      | nbSteps         | 2                                  |
    When I remove the first action with name "split" on the preparation "dataset_with_empty_columns_prep_2"
    Then The preparation "dataset_with_empty_columns_prep_2" should contain the following columns:
      | id | firstname | lastname | phonenumber | city | iscustomer | one pattern col |
    When I add a "split" step identified by "anotherSplitFirstname" on the preparation "dataset_with_empty_columns_prep_2" with parameters :
      | separator   | e         |
      | limit       | 4         |
      | column_name | firstname |
      | column_id   | 0001      |
    Then The preparation "dataset_with_empty_columns_prep_2" should contain the following columns:
      | id | firstname | firstname_split_1 | firstname_split_2 | firstname_split_3 | firstname_split_4 | lastname | phonenumber | city | iscustomer | one pattern col |
