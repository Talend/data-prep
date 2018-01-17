Feature: Test OS the export format

  @CleanAfter
  Scenario: Get the export format and verify the returned export format
    Given I upload the dataset "/data/6L3C.csv" with name "simpleCSVForExportFormat"
    And I create a preparation with name "simpleExportPrep", based on "simpleCSVForExportFormat" dataset
    When I get the export formats for the preparation "simpleExportPrep"
    Then I received for preparation the "simpleExportPrep" the right export format list :
      | XLSX |
      | CSV  |
