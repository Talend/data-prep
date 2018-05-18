@EnvOs @EnvOnPremise @EnvCloud
Feature: Make a aggregation

  @CleanAfter
  Scenario: Make an aggregation on numeric values
    Given I upload the dataset "/data/scores.csv" with name "scores_dataset"
    And I create a preparation with name "scores_preparation", based on "scores_dataset" dataset
    And I add a "uppercase" step on the preparation "scores_preparation" with parameters :
      | column_name | Name |
      | column_id   | 0000 |

    When I apply an aggregation preparation "scores_preparation" with parameters :
      | operator | AVERAGE |
      | columnId | 0001    |
      | groupBy  | 0002    |

    Then The aggregate result with the operator "AVERAGE" is :
      | back  | 17.25 |
      | front | 15.75 |
