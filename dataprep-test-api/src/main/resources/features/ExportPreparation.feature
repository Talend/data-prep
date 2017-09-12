Feature: Export Preparation

  Scenario: Verify transformation result
    Given I upload the dataset "/data/simpleCSV.csv" with name "simpleCSV"
    And I create a preparation with name "myFirstPreparation", based on "simpleCSV" dataset
#    When I add a step "uppercase" to the column "lastname" of the preparation "myFirstPreparation"
#    And I full run the preparation "myFirstPreparation" on the dataset "simpleCSV" and export the result in "myResult.csv" file.
#    Then I check that exported preparation equals "simpleCSV_processed.csv"

