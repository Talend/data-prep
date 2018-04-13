@Filter
Feature: Filter features

  @CleanAfter
  Scenario Outline: Apply a filter to a dataset
    When I upload the dataset "/data/8L3C.csv" with name "8L3C_dataset"
    Then I check the records of the dataset "8L3C_dataset" after applying a filter using the following TQL "<tql>" equals "/data/<filename>" file

    Examples:
      | tql                                                | filename                           |
      | ((0001 = 'Adele'))                                 | 10L3C_equals_filter_records.json   |
      | ((0001 contains 'le'))                             | 10L3C_contains_filter_records.json |
      | ((0001 complies 'Aaaaaaaaa'))                      | 10L3C_complies_filter_records.json |
      | ((0000 between [0, 3[))                            | 10L3C_between_filter_records.json  |
      | ((0001 = 'Diana') or (0001 = 'Carole'))            | 10L3C_or_filter_records.json       |
      | ((0000 between [0, 3[)) and ((0001 contains 'le')) | 10L3C_and_filter_records.json      |

  @CleanAfter
  Scenario Outline: Apply a filter to a preparation
    When I upload the dataset "/data/A-customers_100_with_pb.csv" with name "customers_100_with_pb_dataset"
    And I create a preparation with name "customers_100_with_pb_preparation", based on "customers_100_with_pb_dataset" dataset
    And I add a "uppercase" step on the preparation "customers_100_with_pb_preparation" with parameters :
      | column_name | firstname |
      | column_id   | 0000      |
    Then I check the records of the preparation "customers_100_with_pb_preparation" after applying a filter using the following TQL "<tql_prep>" equals "/data/<filename_prep>" file

    Examples:
      | tql_prep                                        | filename_prep                                       |
      | ((0000 = 'BILL'))                               | customers_100_equals_filter_records.json            |
      | ((0000 contains 'WAR'))                         | customers_100_contains_filter_records.json          |
      | ((0000 complies 'AAAAAAAAAA'))                  | customers_100_complies_filter_records.json          |
      | ((0003 between [1167606000000, 1169852400000[)) | customers_100_between_filter_records.json           |
      | ((0001 = 'Johnson')) and ((0000 = 'BILL'))      | customers_100_and_filter_records.json               |
      | ((0001 = 'Taft') or (0001 = 'Arthur'))          | customers_100_or_filter_records.json                |
      | (0002 is invalid)                               | customers_100_invalid_filter_records.json           |
      | (0002 is empty)                                 | customers_100_empty_filter_records.json             |
      | (* is invalid)                                  | customers_100_all_invalid_filter_records.json       |
      | (* is empty)                                    | customers_100_all_empty_filter_records.json         |
      | ((* is empty) or (* is invalid))                | customers_100_all_invalid_empty_filter_records.json |


  @CleanAfter
  Scenario Outline: Apply filter to a preparation step
    When I upload the dataset "/data/A-customers_100_with_pb.csv" with name "customers_100_with_pb_dataset"
    And I create a preparation with name "customers_100_with_pb_preparation", based on "customers_100_with_pb_dataset" dataset
    And I add a "<step_name>" step on the preparation "customers_100_with_pb_preparation" with parameters :
      | column_name | firstname       |
      | column_id   | 0000            |
      | filter      | <tql_prep_step> |
    When I export the preparation with parameters :
      | exportType           | CSV                               |
      | preparationName      | customers_100_with_pb_preparation |
      | csv_escape_character | "                                 |
      | csv_enclosure_char   | "                                 |
      | dataSetName          | customers_100_with_pb_dataset     |
      | fileName             | customers_100_with_pb.csv         |
      | filter               | <export_filter_export>            |
    Then I check that "customers_100_with_pb.csv" temporary file equals "/data/<export_filename>" file

    Examples:
      | step_name | tql_prep_step                              | export_filter_export                       | export_filename                                    |
      | uppercase | ((0000 = 'Bill'))                          | ((0000 = 'BILL'))                          | customers_100_equals_filter_records.csv            |
      | uppercase | ((0000 contains 'war'))                    | ((0000 contains 'WAR'))                    | customers_100_contains_filter_records.csv          |
      | uppercase | ((0000 complies 'Aaaaaaaaaa'))             | ((0000 complies 'AAAAAAAAAA'))             | customers_100_complies_filter_records.csv          |
      | uppercase | ((0001 = 'Johnson')) and ((0000 = 'Bill')) | ((0001 = 'Johnson')) and ((0000 = 'BILL')) | customers_100_and_filter_records.csv               |
      | keep_only | ((0001 = 'Taft') or (0001 = 'Arthur'))     | ((0001 = 'Arthur'))                        | customers_100_or_filter_records.csv                |
      | keep_only | (0002 is invalid)                          | (0002 is invalid)                          | customers_100_invalid_filter_records.csv           |
      | keep_only | (0002 is empty)                            | (0002 is empty)                            | customers_100_empty_filter_records.csv             |
      | keep_only | (* is invalid)                             | (* is invalid)                             | customers_100_all_invalid_filter_records.csv       |
      | keep_only | (* is empty)                               | (* is empty)                               | customers_100_all_empty_filter_records.csv         |
      | keep_only | ((* is empty) or (* is invalid))           | ((* is empty) or (* is invalid))           | customers_100_all_invalid_empty_filter_records.csv |
