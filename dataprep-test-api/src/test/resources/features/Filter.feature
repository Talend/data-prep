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
      | tql_prep                                        | filename_prep                              |
      | ((0000 = 'BILL'))                               | customers_100_equals_filter_records.json   |
      | ((0000 contains 'WAR'))                         | customers_100_contains_filter_records.json |
      | ((0000 complies 'AAAAAAAAAA'))                  | customers_100_complies_filter_records.json |
      | ((0003 between [1167606000000, 1169852400000[)) | customers_100_between_filter_records.json  |
      | ((0001 = 'Johnson')) and ((0000 = 'BILL'))      | customers_100_and_filter_records.json      |
      | ((0001 = 'Taft') or (0001 = 'Arthur'))       | customers_100_or_filter_records.json       |

