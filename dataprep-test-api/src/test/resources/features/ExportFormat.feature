Feature: Test the export format

  @CleanAfter
  Scenario: Get the export format and verify the S3 export format list
    Given I upload the dataset "/data/6L3C.csv" with name "simpleCSVForExportFormat"
    And I create a preparation with name "simpleExportPrep", based on "simpleCSVForExportFormat" dataset
    When I get the export formats for the preparation "simpleExportPrep"
    Then I received the right "CSV" export format for preparation "simpleExportPrep"
    Then I received the right "XLSX" export format for preparation "simpleExportPrep"