@EnvOS @EnvOnPremise @EnvCloud @Action
Feature: Perform scenarios with some Trim related action

  # Check date, the column type is modified after the trim action (TDP-4926 & TDP-5057)
  Scenario: Calculate Date Until Now & Change DatePattern after a Trim change Type with column creation
    Given I upload the dataset "/data/best_sad_songs_of_all_time.csv" with name "best_sad_songs_dataset"
    Then I wait for the dataset "best_sad_songs_dataset" metadata to be computed
    Given I create a preparation with name "best_sad_songs_prep", based on "best_sad_songs_dataset" dataset
    And I add a "trim" step identified by "trim_date" on the preparation "best_sad_songs_prep" with parameters :
      | column_id         | 0007 |
      | create_new_column | true |
    Then I check that a step like "trim_date" exists in the preparation "best_sad_songs_prep"
    Given I add a "change_date_pattern" step identified by "changeDate1" on the preparation "best_sad_songs_prep" with parameters :
      | column_id         | 0009               |
      | from_pattern_mode | unknown_separators |
      | new_pattern       | yyyy/MM/dd H:mm    |
      | create_new_column | true               |
    Then I check that a step like "changeDate1" exists in the preparation "best_sad_songs_prep"
    Given I add a "change_date_pattern" step identified by "changeDate2" on the preparation "best_sad_songs_prep" with parameters :
      | column_id         | 0010               |
      | from_pattern_mode | unknown_separators |
      | new_pattern       | dd.MM.yyyy         |
      | create_new_column | true               |
    Then I check that a step like "changeDate2" exists in the preparation "best_sad_songs_prep"
    Given I add a "change_date_pattern" step identified by "changeDate3" on the preparation "best_sad_songs_prep" with parameters :
      | column_id         | 0011               |
      | from_pattern_mode | unknown_separators |
      | new_pattern       | yyyy-MM-dd         |
      | create_new_column | true               |
    Then I check that a step like "changeDate3" exists in the preparation "best_sad_songs_prep"
    Given I add a "compute_time_since" step identified by "calculUntil" on the preparation "best_sad_songs_prep" with parameters :
      | column_id         | 0012             |
      | time_unit         | HOURS            |
      | create_new_column | true             |
      | since_when        | specific_date    |
      | specific_date     | 2016-03-01 00:00 |
    Then I check that a step like "calculUntil" exists in the preparation "best_sad_songs_prep"
    Given I update the first step like "changeDate1" on the preparation "best_sad_songs_prep" with the following parameters :
      | new_pattern | dd.MM.yyyy |

  Scenario: Export best_sad_songs_prep preparation and check the exported file best_sad_songs_result.csv
  # escape and enclosure characters should be given because they can be empty
    When I export the preparation with parameters :
      | preparationName      | best_sad_songs_prep       |
      | exportType           | CSV                       |
      | fileName             | best_sad_songs_result.csv |
      | csv_escape_character | "                         |
      | csv_enclosure_char   | "                         |
    Then I check that "best_sad_songs_result.csv" temporary file equals "/data/best_sad_songs_exported.csv" file

  @CleanAfter
  Scenario: Remove Trim Action preparation best_sad_songs_prep
    When I remove the preparation "best_sad_songs_prep"
    Then I check that the preparation "/best_sad_songs_prep" doesn't exist
