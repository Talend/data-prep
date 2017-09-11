Feature: Export Preparation
    Scenario: Verify transformation result
        Given I upload the dataset "simpleCSV.csv" with name "simpleCSV"
        And I create a preparation with name "myFirstPreparation"
        When I add a step "uppercase" to the column "lastname" of the preparation "myFirstPreparation"
        And I export the preparation "MyFirstPreparation"
        Then I check that exported preparation equals "simpleCSV_processed.csv"

