@EnvOS @EnvOnPremise @EnvCloud
Feature: Perform scenarios with SearchAndReplace related action

  Scenario: See bug TDP-5243
    Given I upload the dataset "/data/best_sad_songs_of_all_time.csv" with name "best_sad_songs_of_all_time_dataset"
    Given I create a preparation with name "best_sad_songs_of_all_time_prep", based on "best_sad_songs_of_all_time_dataset" dataset
    Given I add a "replace_on_value" step identified by "searchReplace" on the preparation "best_sad_songs_of_all_time_prep" with parameters :
      | column_id         | 0007                                |
      | replace_value     | 1                                   |
      | cell_value        | {"token":"0","operator":"contains"} |
      | create_new_column | true                                |
    Then I check that a step like "searchReplace" exists in the preparation "best_sad_songs_of_all_time_prep"

  Scenario: Export and check the exported file
    When I export the preparation with parameters :
      | preparationName      | best_sad_songs_of_all_time_prep    |
      | dataSetName          | best_sad_songs_of_all_time_dataset |
      | exportType           | CSV                                |
      | fileName             | best_sad_songs_search_result.csv   |
      | csv_escape_character | "                                  |
      | csv_enclosure_char   | "                                  |
    Then I check that "best_sad_songs_search_result.csv" temporary file equals "/data/best_sad_songs_search_exported.csv" file

  @CleanAfter
  Scenario: Remove SearchAndReplace preparation best_sad_songs_of_all_time_prep
    When I remove the preparation "best_sad_songs_of_all_time_prep"
    Then I check that the preparation "/best_sad_songs_of_all_time_prep" doesn't exist