Feature: Export Preparation with created column

  Scenario: Create a preparation with two steps from a CSV
    Given I upload the dataset "/data/best_sad_songs_of_all_time.csv" with name "best_sad_songs_of_all_time"
    And I create a preparation with name "best_sad_songs_prep", based on "best_sad_songs_of_all_time" dataset
    And I add a step with parameters :
      | actionName        | trim                       |
      | preparationName   | best_sad_songs_prep        |
      | columnName        | Added At                   |
      | columnId          | 0008                       |
      | paddingCharacter  | whitespace                 |
      | createNewColumn   | false                      |
      | rowId             |                            |
    And I add a step with parameters :
      | actionName        | compute_time_since         |
      | preparationName   | best_sad_songs_prep        |
      | columnName        | Added At                   |
      | columnId          | 0008                       |
      | createNewColumn   | true                       |
      | timeUnit          | HOURS                      |
      | sinceWhen         | now_server_side            |
      | rowId             |                            |
  @CleanAfter
  Scenario: Verify export result
    # created column with the second step should be not empty
    When I export the preparation with parameters :
      | exportType           | CSV                                    |
      | preparationName      | best_sad_songs_prep                    |
      | csv_escape_character | "                                      |
      | csv_enclosure_char   | "                                      |
      | dataSetName          | best_sad_songs_of_all_time             |
      | fileName             | result.csv                             |
    Then I check that "result.csv" temporary file equals "/data/best_sad_songs_of_all_time_export.csv" file
