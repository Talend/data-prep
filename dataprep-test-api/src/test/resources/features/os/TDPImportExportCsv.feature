Feature: Test
    Scenario: base sample
        When I upload the dataset simpleCSV.csv with name simpleCSV
        Then The uploaded dataset is present in datasets list
