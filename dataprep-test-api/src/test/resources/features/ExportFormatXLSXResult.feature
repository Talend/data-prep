Feature: Exporting preparation on XLSX format

  @CleanAfter
  Scenario: Verify phone number transformation
    Given I upload the dataset "/data/phoneNumber.csv" with name "phoneNumber_dataset"
    And I create a preparation with name "phoneNumber_preparation", based on "phoneNumber_dataset" dataset
    And I add a "format_phone_number" step on the preparation "phoneNumber_preparation" with parameters :
      | region_code      | FR                      |
      | format_type      | international           |
      | mode             | constant_mode           |
      | column_name      | phoneNumber             |
      | column_id        | 0002                    |
    When I export the preparation with parameters :
      | exportType           | XLSX                         |
      | preparationName      | phoneNumber_preparation      |
      | dataSetName          | phoneNumber_dataset          |
      | fileName             | phoneNumber_result.xlsx      |
    Then I check that "phoneNumber_result.xlsx" temporary file equals "/data/phoneNumber_formatFrench.xlsx" file

  @CleanAfter
  Scenario: Verify phone number on dataset transformation
    Given I upload the dataset "/data/phoneNumberScopeDataset.csv" with name "phoneNumberScopeDataset_dataset"
    And I create a preparation with name "phoneNumberScopeDataset_preparation", based on "phoneNumberScopeDataset_dataset" dataset
    And I add a "format_phone_number" step on the preparation "phoneNumberScopeDataset_preparation" with parameters :
      | scope           | dataset                               |
      | region_code     | US                                    |
      | format_type     | international                         |
      | mode            | constant_mode                         |
    When I export the preparation with parameters :
      | exportType      | CSV                                   |
      | preparationName | phoneNumberScopeDataset_preparation   |
      | dataSetName     | phoneNumberScopeDataset_dataset       |
      | fileName        | phoneNumberScopeDataset_result.csv    |
    Then I check that "phoneNumberScopeDataset_result.csv" temporary file equals "/data/phoneNumberScopeDataset_formatUs.csv" file

