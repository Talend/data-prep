Feature: Perform an OS Smoke Test

  Scenario: Upload a dataset
    Given I upload the dataset "/data/10L3C.csv" with name "10L3C_dataset"
    Then A dataset with the following parameters exists :
      | name  | 10L3C_dataset |
      | nbRow | 10            |

  Scenario: Create a preparation with steps
    Given I create a preparation with name "10L3C_preparation", based on "10L3C_dataset" dataset
    And I add a "uppercase" step identified by "stepUp" on the preparation "10L3C_preparation" with parameters :
      | column_name | firstname |
      | column_id   | 0001      |
    Then I check that a step like "stepUp" exists in the preparation "10L3C_preparation"
    Given I add a "change_date_pattern" step identified by "changeDate" on the preparation "10L3C_preparation" with parameters :
      | from_pattern_mode | unknown_separators |
      | pattern           | M/d/yy             |
#      | column_name       | date                |
      | column_id         | 0002               |
    Then I check that a step like "changeDate" exists in the preparation "10L3C_preparation"
    # this split should not create new column (as the separator character isn't present in the dataset dates)
    Given I add a "split" step identified by "dateSplit" on the preparation "10L3C_preparation" with parameters :
      | limit       | 2    |
      | separator   | ;    |
      | column_name | date |
      | column_id   | 0002 |
    Then I check that a step like "dateSplit" exists in the preparation "10L3C_preparation"

  Scenario: Update date split step
    Given I update the first step like "dateSplit" on the preparation "10L3C_preparation" with the following parameters :
      | separator               | other_string |
      | manual_separator_string | /            |
    Then I check that a step like "dateSplit" exists in the preparation "10L3C_preparation"
    Given I move the first step like "dateSplit" after the first step like "stepUp" on the preparation "10L3C_preparation"
    # TODO : Check step ancestor

  Scenario: Delete a column
    Given I add a "delete_column" step identified by "deleteDate" on the preparation "10L3C_preparation" with parameters :
      | scope       | column |
      | column_name | date   |
      | column_id   | 0002   |
    Then I check that a step like "deleteDate" exists in the preparation "10L3C_preparation"

  Scenario: Fail to move a step
    Given I fail to move the first step like "deleteDate" after the first step like "stepUp" on the preparation "10L3C_preparation"

  Scenario: Export and check the exported file
    # escape and enclosure characters should be given because they can be empty
    When I export the preparation with parameters :
      | exportType           | CSV               |
      | preparationName      | 10L3C_preparation |
      | dataSetName          | 10L3C_dataset     |
      | fileName             | acote.csv         |
      | csv_escape_character | "                 |
      | csv_enclosure_char   | "                 |
    Then I check that "acote.csv" temporary file equals "/data/10L3C_processed.csv" file

  Scenario: Move a preparation in a new folder
    Given A preparation with the following parameters exists :
      | preparationName | 10L3C_preparation |
      | dataSetName     | 10L3C_dataset     |
      | nbSteps         | 4                 |
    And I create a folder with the following parameters :
      | origin     | /          |
      | folderName | smoke/test |
    And I move the preparation "/10L3C_preparation" to "/smoke/test/10L3C_preparation"
    Then I check that the preparation "/smoke/test/10L3C_preparation" exists

  Scenario: Copy a preparation
    Given A preparation with the following parameters exists :
      | preparationName | /smoke/test/10L3C_preparation |
      | dataSetName     | 10L3C_dataset                 |
      | nbSteps         | 4                             |
    And I create a folder with the following parameters :
      | origin     | /          |
      | folderName | smoke/test |
    And I copy the preparation "/smoke/test/10L3C_preparation" to "/smoke/10L3C_preparation_Copy"
    Then I check that the preparation "/smoke/10L3C_preparation_Copy" exists
    And I check that the preparation "/smoke/test/10L3C_preparation" exists
    And I check that the preparations "/smoke/10L3C_preparation_Copy" and "/smoke/test/10L3C_preparation" have the same steps

  Scenario: Export and check the exported file
    # escape and enclosure characters should be given because they can be empty
    When I export the preparation with parameters :
      | exportType           | CSV                           |
      | preparationName      | /smoke/test/10L3C_preparation |
      | dataSetName          | 10L3C_dataset                 |
      | fileName             | 10L3C_result.csv              |
      | csv_escape_character | "                             |
      | csv_enclosure_char   | "                             |
    Then I check that "10L3C_result.csv" temporary file equals "/data/10L3C_processed.csv" file

  @CleanAfter
  Scenario: Remove original preparation after copying the preparation
    When I remove the preparation "/smoke/test/10L3C_preparation"
    Then I check that the preparation "/smoke/test/10L3C_preparation" doesn't exist
    When I export the preparation with parameters :
      | exportType           | CSV                           |
      | preparationName      | /smoke/10L3C_preparation_Copy |
      | dataSetName          | 10L3C_dataset                 |
      | fileName             | copied_10L3C_result.csv       |
      | csv_escape_character | "                             |
      | csv_enclosure_char   | "                             |
    Then I check that "copied_10L3C_result.csv" temporary file equals "/data/10L3C_processed.csv" file
