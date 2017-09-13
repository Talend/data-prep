Meta:
@author YAC
@theme dataset

Narrative:
As a user
I want to test calling dataprep rest API
I want to import a csv file as a dataset
I want to add a step
I want to  export the created preparation
So that I can achieve a integration test

Scenario: base sample
When I upload the dataset simpleCSV.csv with name simpleCSV
Then The uploaded dataset is present in datasets list
