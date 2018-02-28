Feature: Perform scenarios with some Trim related action

  # Check date, the column type is modified after the trim action
  Scenario: Calculate Date Until Now & ChangeDatePattern after a Trim change Type (TDP-4926 & TDP-5057) with column creation
    Given I upload the dataset "/data/best_sad_songs_of_all_time.csv" with name "best_sad_songs_dataset"
    Given I create a preparation with name "best_sad_songs_prep", based on "best_sad_songs_dataset" dataset
    And I add a step identified by "trim_date" with parameters :
      | actionName      | trim                |
      | columnId        | 0007                |
      | createNewColumn | true                |
      | preparationName | best_sad_songs_prep |
      | createNewColumn | true                |
    Then I check that a step like "trim_date" exists in the preparation "best_sad_songs_prep"
    Given I add a step identified by "changeDate1" with parameters :
      | preparationName   | best_sad_songs_prep |
      | columnId          | 0009                |
      | actionName        | change_date_pattern |
      | fromPatternMode   | unknown_separators  |
      | newPattern        | custom              |
      | customDatePattern | yyyy/MM/dd  H:mm    |
      | createNewColumn   | true                |
    Then I check that a step like "changeDate1" exists in the preparation "best_sad_songs_prep"
    Given I add a step identified by "changeDate2" with parameters :
      | preparationName   | best_sad_songs_prep |
      | columnId          | 0010                |
      | actionName        | change_date_pattern |
      | fromPatternMode   | unknown_separators  |
      | newPattern        | custom              |
      | customDatePattern | dd.MM.yy            |
      | createNewColumn   | true                |
    Then I check that a step like "changeDate2" exists in the preparation "best_sad_songs_prep"
    Given I add a step identified by "changeDate3" with parameters :
      | preparationName   | best_sad_songs_prep |
      | columnId          | 0011                |
      | actionName        | change_date_pattern |
      | fromPatternMode   | unknown_separators  |
      | newPattern        | custom              |
      | customDatePattern | d-M-yyyy            |
      | createNewColumn   | true                |
    Then I check that a step like "changeDate3" exists in the preparation "best_sad_songs_prep"
    Given I add a step identified by "calculUntil" with parameters :
      | preparationName  | best_sad_songs_prep |
      | columnId         | 0012                |
      | actionName       | compute_time_since  |
      | timeUnit         | HOURS               |
      | createNewColumn  | true                |
      | sinceWhen        | specific_date       |
      | specificDateMode | 2016-03-01 00:00    |
    Then I check that a step like "calculUntil" exists in the preparation "best_sad_songs_prep"
    Given I update the first step like "changeDate1" on the preparation "best_sad_songs_prep" with the following parameters :
      | customDatePattern | dd.MM.yyyy. |

  Scenario: Export and check the exported file
  # escape and enclosure characters should be given because they can be empty
    When I export the preparation with parameters :
      | preparationName      | best_sad_songs_prep       |
      | dataSetName          | best_sad_songs_dataset    |
      | exportType           | CSV                       |
      | fileName             | best_sad_songs_result.csv |
      | csv_escape_character | "                         |
      | csv_enclosure_char   | "                         |
    Then I check that "best_sad_songs_result.csv" temporary file equals "/data/best_sad_songs_exported.csv" file

  @CleanAfter
  Scenario: Remove original preparation after copying the preparation
    When I remove the preparation "best_sad_songs_prep"
    Then I check that the preparation "best_sad_songs_prep" doesn't exist in the folder "/smoke/test"