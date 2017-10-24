Feature: Smoke Test

  Scenario: Perform a smoke test
    Given I upload the dataset "/data/10L3C.csv" with name "10L3C_dataset"
    Then A dataset with the following parameters exists :
      | name  | 10L3C_dataset |
      | nbRow | 10            |
    And I create a preparation with name "10L3C_preparation", based on "10L3C_dataset" dataset
    And I add a step with parameters :
      | actionName      | uppercase         |
      | columnName      | firstname         |
      | columnId        | 0002              |
      | preparationName | 10L3C_preparation |
    Then A step with the following parameters exists on the preparation "10L3C_preparation" :
      | actionName | uppercase |
      | columnName | firstname |
      | columnId   | 0002      |
    And I add a step with parameters :
      | actionName      | change_date_pattern  |
      | fromPatternMode | "unknown_separators" |
      | pattern         | M/d/yy               |
      | columnName      | date                 |
      | columnId        | 0003                 |
      | preparationName | 10L3C_preparation    |
    Then A step with the following parameters exists on the preparation "10L3C_preparation" :
      | actionName | uppercase |
      | columnName | firstname |
      | columnId   | 0003      |
