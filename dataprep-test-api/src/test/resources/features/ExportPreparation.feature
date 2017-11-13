Feature: Export Preparation

  Scenario: Create a preparation with one step
    Given I upload the dataset "/data/3L3C.csv" with name "3L3C_dataset"
    And I create a preparation with name "3L3C_preparation", based on "3L3C_dataset" dataset
    And I add a step with parameters :
      | actionName      | uppercase        |
      | columnName      | lastname         |
      | columnId        | 0001             |
      | preparationName | 3L3C_preparation |

  Scenario: Verify transformation result
    And I export the preparation "3L3C_preparation" on the dataset "3L3C_dataset" and export the result in "3L3C_result.csv" temporary file.
    Then I check that "3L3C_result.csv" temporary file equals "/data/3L3C_processed.csv" file

  Scenario: Verify transformation result with another escape char
    When I export the preparation "3L3C_preparation" on the dataset "3L3C_dataset" and export the result with "#" as escape character in "3L3C_result.csv" temporary file.
    Then I check that "3L3C_result.csv" temporary file equals "/data/3L3C_processed_custom_escape_char.csv" file

  @CleanAfter
  Scenario: Verify transformation result with custom parameters
    When I export the preparation with custom parameters :
      | csv_fields_delimiter | -                |
      | csv_escape_character | #                |
      | csv_enclosure_mode   | all_fields       |
      | csv_charset          | UTF-8            |
      | csv_enclosure_char   | +                |
      | preparationName      | 3L3C_preparation |
      | dataSetName          | 3L3C_dataset     |
      | fileName             | 3L3C_result.csv  |
    Then I check that "3L3C_result.csv" temporary file equals "/data/3L3C_exported_with_custom_param.csv" file
