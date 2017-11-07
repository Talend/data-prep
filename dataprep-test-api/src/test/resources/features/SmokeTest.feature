Feature: Smoke Test

  Scenario: Perform a smoke test
    Given I upload the dataset "/data/10L3C.csv" with name "10L3C_dataset"
    Then A dataset with the following parameters exists :
      | name  | 10L3C_dataset |
      | nbRow | 10            |
    And I create a preparation with name "10L3C_preparation", based on "10L3C_dataset" dataset
    And I add a step identified by "stepUp" with parameters :
      | actionName      | uppercase         |
      | columnName      | firstname         |
      | columnId        | 0001              |
      | preparationName | 10L3C_preparation |
    Then I check that a step like "stepUp" exists in the preparation "10L3C_preparation"
    And I add a step identified by "changeDate" with parameters :
      | actionName      | change_date_pattern  |
      | fromPatternMode | "unknown_separators" |
      | pattern         | M/d/yy               |
      | columnName      | date                 |
      | columnId        | 0002                 |
      | preparationName | 10L3C_preparation    |
    Then I check that a step like "changeDate" exists in the preparation "10L3C_preparation"
    And I add a step identified by "dateSplit" with parameters :
      | actionName      | split             |
      | limit           | 2                 |
      | separator       | ;                 |
      | columnName      | date              |
      | columnId        | 0002              |
      | preparationName | 10L3C_preparation |
    Then I check that a step like "dateSplit" exists in the preparation "10L3C_preparation"
    And I update the first step like "dateSplit" on the preparation "10L3C_preparation" with the following parameters :
      | separator             | other (string) |
      | manualSeparatorString | /              |
    Then I check that a step like "dateSplit" exists in the preparation "10L3C_preparation"
    And I move the first step like "dateSplit" after the first step like "stepUp" on the preparation "10L3C_preparation"
    And I add a step identified by "deleteDate" with parameters :
      | actionName      | delete_column     |
      | scope           | column            |
      | columnName      | date              |
      | columnId        | 0002              |
      | preparationName | 10L3C_preparation |
    Then I fail to move the first step like "deleteDate" after the first step like "stepUp" on the preparation "10L3C_preparation"
    And I export the preparation "10L3C_preparation" on the dataset "10L3C_dataset" and export the result in "acote.csv" temporary file.
    Then I check that "acote.csv" temporary file equals "/data/10L3C_processed.csv" file
    Then A preparation with the following parameters exists :
      | preparationName | 10L3C_preparation |
      | datasetName     | 10L3C_dataset     |
      | nbSteps         | 4                 |
    Then I create a folder with the following parameters :
      | origin     | /          |
      | folderName | smoke/test |
    Then I move the preparation "10L3C_preparation" with the following parameters :
      | origin             | /                 |
      | destination        | /smoke/test       |
      | newPreparationName | 10L3C_preparation |
    And I check that the preparation "10L3C_preparation" exists under the folder "/smoke/test"
    And I export the preparation "10L3C_preparation" on the dataset "10L3C_dataset" and export the result in "10L3C_result.csv" temporary file.
    Then I check that "10L3C_result.csv" temporary file equals "/data/10L3C_processed.csv" file