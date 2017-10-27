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
      | actionName | change_date_pattern |
      | columnName | date                |
      | columnId   | 0003                |
    And I add a step with parameters :
      | actionName      | split             |
      | limit           | 2                 |
      | separator       | ;                 |
      | columnName      | date              |
      | columnId        | 0003              |
      | preparationName | 10L3C_preparation |
    Then A step with the following parameters exists on the preparation "10L3C_preparation" :
      | actionName | split |
      | limit      | 2     |
      | separator  | ;     |
      | columnName | date  |
      | columnId   | 0003  |
    And I update the step "split" on the preparation "10L3C_preparation" with the following parameters :
      | limit                 | 2              |
      | separator             | other (string) |
      | manualSeparatorString | /              |
    Then A step with the following parameters exists on the preparation "10L3C_preparation" :
      | actionName            | split          |
      | limit                 | 2              |
      | separator             | other (string) |
      | manualSeparatorString | /              |
      | columnName            | date           |
      | columnId              | 0003           |
#    And I move the first "split" step after the first "uppercase" step