Feature: Test EE
    Scenario: base sample 2
        Given I upload the dataset simpleCSV.csv with name simpleCSV
        Then The uploaded dataset is present in datasets list
