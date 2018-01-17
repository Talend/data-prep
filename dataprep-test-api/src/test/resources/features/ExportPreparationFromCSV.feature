Feature: Export Preparation from CSV

  Scenario: Create a preparation with one step from a CSV
    Given I upload the dataset "/data/6L3C.csv" with name "6L3C_dataset"
    And I create a preparation with name "6L3C_preparation", based on "6L3C_dataset" dataset
    And I add a step with parameters :
      | actionName      | uppercase        |
      | columnName      | lastname         |
      | columnId        | 0001             |
      | preparationName | 6L3C_preparation |

  Scenario: Verify transformation result
    # escape and enclosure characters should be given because they can be empty
    When I export the preparation with parameters :
      | exportType           | CSV              |
      | preparationName      | 6L3C_preparation |
      | csv_escape_character | "                |
      | csv_enclosure_char   | "                |
      | dataSetName          | 6L3C_dataset     |
      | fileName             | 6L3C_result.csv  |
    Then I check that "6L3C_result.csv" temporary file equals "/data/6L3C_default_export_parameters.csv" file

  Scenario: Verify transformation result with another escape char
    When I export the preparation with parameters :
      | exportType           | CSV              |
      | preparationName      | 6L3C_preparation |
      | dataSetName          | 6L3C_dataset     |
      | csv_escape_character | #                |
      | csv_enclosure_char   | "                |
      | fileName             | 6L3C_result.csv  |
    Then I check that "6L3C_result.csv" temporary file equals "/data/6L3C_processed_custom_escape_char.csv" file

  @CleanAfter
  Scenario: Verify transformation result with custom parameters
    When I export the preparation with parameters :
      | exportType           | CSV                               |
      | csv_fields_delimiter | -                                 |
      | csv_escape_character | #                                 |
      | csv_enclosure_mode   | all_fields                        |
      | csv_charset          | ISO-8859-1                        |
      | csv_enclosure_char   | +                                 |
      | preparationName      | 6L3C_preparation                  |
      | dataSetName          | 6L3C_dataset                      |
      | fileName             | 6L3C_result_with_custom_param.csv |
    Then I check that "6L3C_result_with_custom_param.csv" temporary file equals "/data/6L3C_exported_with_custom_param.csv" file
