@EnvOs @EnvOnPremise @EnvCloud
Feature: Perform scenarios action and check that the type is changed

  Scenario: Perform scenarios action and check that the type is changed
    Given I upload the dataset "/data/6L3C.csv" with name "6L3C_concat"
    Then I wait for the dataset "6L3C_concat" metadata to be computed
    Given I create a preparation with name "6L3C_concat_prep", based on "6L3C_concat" dataset
    Given I add a "concat" step identified by "concat" on the preparation "6L3C_concat_prep" with parameters :
      | column_id | 0000 |
    Then I check that a step like "concat" exists in the preparation "6L3C_concat_prep"
    Then The preparation "6L3C_concat_prep" should contain the following columns:
      | id | firstname | date |

  @CleanAfter
  Scenario: Export 6L3C_concat_prep and check the exported file 6L3C_concat_prep_result.csv
    When I export the preparation with parameters :
      | preparationName | 6L3C_concat_prep                |
      | exportType      | CSV                             |
      | fileName        | 6L3C_concat_prep_result.csv |
    Then I check that "6L3C_concat_prep_result.csv" temporary file equals "/data/6L3C_concat_prep_exported.csv" file
