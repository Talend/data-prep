Feature: Exporting preparation on XLSX format

  @CleanAfter
  Scenario: Verify phone number transformation
    Given I upload the dataset "/data/phoneNumber.csv" with name "phoneNumber_dataset"
    And I create a preparation with name "phoneNumber_preparation", based on "phoneNumber_dataset" dataset
    And I add a step with parameters :
      | actionName      | format_phone_number     |
      | regionCode      | FR                      |
      | formatType      | international           |
      | mode            | constant_mode           |
      | columnName      | phoneNumber             |
      | columnId        | 0002                    |
      | preparationName | phoneNumber_preparation |
    When I export the preparation with parameters :
      | exportType           | XLSX                         |
      | preparationName      | phoneNumber_preparation      |
      | dataSetName          | phoneNumber_dataset          |
      | fileName             | phoneNumber_result.xlsx      |
    Then I check that XLSX "phoneNumber_result.xlsx" temporary file equals "/data/phoneNumber_formatFrench.xlsx" file
