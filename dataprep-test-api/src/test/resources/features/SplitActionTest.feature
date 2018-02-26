Feature: Perform scenarios with ExtractDateToken related action

  Scenario: TDP-2673 (TDP-4926/TDP-5057) related bug fix
    #
    Given I upload the dataset "/data/Albums_Musique.txt" with name "Albums_Musique_dataset"
    Given I create a preparation with name "Albums_Musique_prep", based on "Albums_Musique_dataset" dataset
    Given I add a step identified by "splitColumn" with parameters :
      | preparationName | Albums_Musique_prep |
      | columnId        | 0002                |
      | actionName      | split               |
      | separator       | ;                   |
      | limit           | 2                   |
    Then I check that a step like "splitColumn" exists in the preparation "Albums_Musique_prep"
    Given I add a step identified by "changeDateFrench" with parameters :
      | preparationName   | Albums_Musique_prep |
      | columnId          | 0004                |
      | actionName        | change_date_pattern |
      | fromPatternMode   | unknown_separators  |
      | newPattern        | custom              |
      | customDatePattern | dd/MM/yy            |
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