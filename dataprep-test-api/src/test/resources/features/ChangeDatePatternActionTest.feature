@EnvOs @EnvOnPremise @EnvCloud
Feature: Perform scenarios with ChangeDate related actions

  Scenario: Several ChangeDatePattern with update of a previous step TDP-4926 (without new column)
    # 0. dd/MM/yyyy --> (1.) ISO 8601 --> (2.) French Standard --> (3.) update action 1. to German Standard  with time
    # [On the same column]
    Given I upload the dataset "/data/A-customers_100_with_pb.csv" with name "A-customers_100_with_pb_dataset"
    Given I create a preparation with name "A-customers_100_with_pb_prep", based on "A-customers_100_with_pb_dataset" dataset
    Given I add a "change_date_pattern" step identified by "changeDateIso" on the preparation "A-customers_100_with_pb_prep" with parameters :
    # ISO 8601 : yyyy-MM-dd
      | column_id           | 0003               |
      | from_pattern_mode   | unknown_separators |
      | new_pattern         | custom             |
      | custom_date_pattern | yyyy-MM-dd         |
    Then I check that a step like "changeDateIso" exists in the preparation "A-customers_100_with_pb_prep"
    Given I add a "change_date_pattern" step identified by "changeDateFrench" on the preparation "A-customers_100_with_pb_prep" with parameters :
    # French Standard : dd/MM/yy
      | column_id           | 0003               |
      | from_pattern_mode   | unknown_separators |
      | new_pattern         | custom             |
      | custom_date_pattern | dd/MM/yy           |
    Then I check that a step like "changeDateFrench" exists in the preparation "A-customers_100_with_pb_prep"

  Scenario: Export and check the exported file
    # Before update : ISO 8601 and French Standard should be exported
    When I export the preparation with parameters :
      | preparationName      | A-customers_100_with_pb_prep            |
      | dataSetName          | A-customers_100_with_pb_dataset         |
      | exportType           | CSV                                     |
      | fileName             | A-customers_100_with_pb_prep_result.csv |
      | csv_escape_character | "                                       |
      | csv_enclosure_char   | "                                       |
    Then I check that "A-customers_100_with_pb_prep_result.csv" temporary file equals "/data/A-customers_100_with_pb_exported.csv" file

    #This step update should not change the exported result of the last column
    Given I update the first step like "changeDateIso" on the preparation "A-customers_100_with_pb_prep" with the following parameters :
      | custom_date_pattern | dd.MM.yy HH:mm |
      # After update : the exported file should be the same
    When I export the preparation with parameters :
      | preparationName      | A-customers_100_with_pb_prep                    |
      | dataSetName          | A-customers_100_with_pb_dataset                 |
      | exportType           | CSV                                             |
      | fileName             | A-customers_100_with_pb_prep_result_updated.csv |
      | csv_escape_character | "                                               |
      | csv_enclosure_char   | "                                               |
    Then I check that "A-customers_100_with_pb_prep_result_updated.csv" temporary file equals "/data/A-customers_100_with_pb_exported.csv" file

  @CleanAfter
  Scenario: Remove first ChangeDatePattern Action preparation A-customers_100_with_pb_prep
    When I remove the preparation "A-customers_100_with_pb_prep"
    Then I check that the preparation "A-customers_100_with_pb_prep" doesn't exist in the folder "/"

  # this scenario is the same than the previous one but this time, the option new column is used
  Scenario: Several ChangeDatePattern with update of a previous step TDP-4926 (with new column)
    Given I upload the dataset "/data/A-customers_100_with_pb.csv" with name "A-customers_100_with_pb_dataset"
    # 0. dd/MM/yyyy --> (1.) ISO 8601 --> (2.) French Standard --> (3.) update action 1. to German Standard  with time
    Given I create a preparation with name "A-customers_100_with_pb_prep_newCol", based on "A-customers_100_with_pb_dataset" dataset
    Given I add a "change_date_pattern" step identified by "changeDateIso_newCol" on the preparation "A-customers_100_with_pb_prep_newCol" with parameters :
    # ISO 8601 : yyyy-MM-dd
      | column_id           | 0003               |
      | from_pattern_mode   | unknown_separators |
      | new_pattern         | custom             |
      | custom_date_pattern | yyyy-MM-dd         |
      | create_new_column   | true               |
    Then I check that a step like "changeDateIso_newCol" exists in the preparation "A-customers_100_with_pb_prep_newCol"
    Given I add a "change_date_pattern" step identified by "changeDateFrench_newCol" on the preparation "A-customers_100_with_pb_prep_newCol" with parameters :
    # French Standard : dd/MM/yy
      | column_id           | 0009               |
      | from_pattern_mode   | unknown_separators |
      | new_pattern         | custom             |
      | custom_date_pattern | dd/MM/yy           |
      | create_new_column   | true               |
    Then I check that a step like "changeDateFrench_newCol" exists in the preparation "A-customers_100_with_pb_prep_newCol"

  Scenario: Export and check the exported file
    # Before update : ISO 8601 and French Standard should be exported
    When I export the preparation with parameters :
      | dataSetName          | A-customers_100_with_pb_dataset                |
      | preparationName      | A-customers_100_with_pb_prep_newCol            |
      | exportType           | CSV                                            |
      | fileName             | A-customers_100_with_pb_prep_result_newCol.csv |
      | csv_escape_character | "                                              |
      | csv_enclosure_char   | "                                              |
    Then I check that "A-customers_100_with_pb_prep_result_newCol.csv" temporary file equals "/data/A-customers_100_with_pb_exported_newCol.csv" file

    #This step update should not change the exported result of the last column
    Given I update the first step like "changeDateIso_newCol" on the preparation "A-customers_100_with_pb_prep_newCol" with the following parameters :
      | custom_date_pattern | dd.MM.yy HH:mm |
      # After update : German Standard with time and French Standard should be exported
    When I export the preparation with parameters :
      | dataSetName          | A-customers_100_with_pb_dataset                        |
      | preparationName      | A-customers_100_with_pb_prep_newCol                    |
      | exportType           | CSV                                                    |
      | fileName             | A-customers_100_with_pb_prep_result_updated_newCol.csv |
      | csv_escape_character | "                                                      |
      | csv_enclosure_char   | "                                                      |
    Then I check that "A-customers_100_with_pb_prep_result_updated_newCol.csv" temporary file equals "/data/A-customers_100_with_pb_updated_exported_newCol.csv" file

  @CleanAfter
  Scenario: Remove second ChangeDatePattern Action preparation A-customers_100_with_pb_prep_newCol
    When I remove the preparation "A-customers_100_with_pb_prep_newCol"
    Then I check that the preparation "A-customers_100_with_pb_prep_newCol" doesn't exist in the folder "/"