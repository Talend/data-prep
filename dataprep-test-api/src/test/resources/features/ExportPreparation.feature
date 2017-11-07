Feature: Export Preparation

#  @CleanAfter
  Scenario: Create a preparation with one step
    Given I upload the dataset "/data/3L3C.csv" with name "3L3C_dataset"
    And I create a preparation with name "3L3C_preparation", based on "3L3C_dataset" dataset
    When I add a step with parameters :
      | actionName      | uppercase        |
      | columnName      | lastname         |
      | columnId        | 0001             |
      | preparationName | 3L3C_preparation |

  @CleanAfter
  Scenario: Verify transformation result
    And I export the preparation "3L3C_preparation" on the dataset "3L3C_dataset" and export the result in "3L3C_result.csv" temporary file.
    Then I check that "3L3C_result.csv" temporary file equals "/data/3L3C_processed.csv" file
