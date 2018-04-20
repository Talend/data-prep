@EnvOS
Feature: Test OS the export format

  @CleanAfter
  Scenario: Get the export format and verify the returned export format
    Given I upload the dataset "/data/6L3C.csv" with name "simpleCSVForExportFormat"
    And I create a preparation with name "simpleExportPrep", based on "simpleCSVForExportFormat" dataset
    Then I check that "simpleExportPrep" available export formats are :
      | XLSX | CSV |