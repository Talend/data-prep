Feature: Perform scenarios with ExtractDateToken related action

  Scenario: TDP-2673 (TDP-4926/TDP-5057) related bug fix
    # Remark : the split action will change the type of one of the new column
    Given I upload the dataset "/data/Albums_Musique.txt" with name "Albums_Musique_dataset"
    Given I create a preparation with name "Albums_Musique_prep", based on "Albums_Musique_dataset" dataset
    Given I add a "split" step identified by "splitColumn" on the preparation "Albums_Musique_prep" with parameters :
      | column_id | 0002 |
      | separator | ;    |
      | limit     | 2    |
    Then I check that a step like "splitColumn" exists in the preparation "Albums_Musique_prep"
    Given I add a "change_date_pattern" step identified by "changeDateFrench" on the preparation "Albums_Musique_prep" with parameters :
      | column_id           | 0004               |
      | from_pattern_mode   | unknown_separators |
      | new_pattern         | custom             |
      | custom_date_pattern | dd/MM/yy           |
    Then I check that a step like "changeDateFrench" exists in the preparation "Albums_Musique_prep"

  Scenario: Export and check the exported file
    When I export the preparation with parameters :
      | preparationName      | Albums_Musique_prep            |
      | dataSetName          | Albums_Musique_dataset         |
      | exportType           | CSV                            |
      | fileName             | Albums_Musique_prep_result.csv |
      | csv_escape_character | "                              |
      | csv_enclosure_char   | "                              |
    Then I check that "Albums_Musique_prep_result.csv" temporary file equals "/data/Albums_Musique_prep_exported.csv" file

  @CleanAfter
  Scenario: Remove original preparation after copying the preparation
    When I remove the preparation "Albums_Musique_prep"
    Then I check that the preparation "Albums_Musique_prep" doesn't exist in the folder "/smoke/test"