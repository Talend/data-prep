@EnvOS @EnvOnPremise @EnvCloud
Feature: Filter features

  @CleanAfter
  Scenario: Apply a filter to a dataset
    Given I upload the dataset "/data/12L5C.csv" with name "12L5C_dataset"
    Then I wait for the dataset "12L5C_dataset" metadata to be computed
    When I apply the filter "((0000 between [0, 3[))" on dataset "12L5C_dataset"
    Then The characteristics of the dataset "12L5C_dataset" match:
      | records              | /data/filter/12L5C_0000_between_0_and_3_records.json |
      | quality              | /data/filter/12L5C_initial_quality.json              |
      | sample_records_count | 12                                                   |

    When I remove all filters on dataset "12L5C_dataset"
    Then The characteristics of the dataset "12L5C_dataset" match:
      | records              | /data/filter/12L5C_initial_records.json |
      | quality              | /data/filter/12L5C_initial_quality.json |
      | sample_records_count | 12                                      |

    When I apply the filter "((0002 is empty) or (0002 is invalid)) and ((0001 contains 'l'))" on dataset "12L5C_dataset"
    Then The characteristics of the dataset "12L5C_dataset" match:
      | records              | /data/filter/12L5C_0002_empty_or_invalid_and_0001_contains_l_records.json |
      | quality              | /data/filter/12L5C_initial_quality.json                                   |
      | sample_records_count | 12                                                                        |


  @CleanAfter
  Scenario: Apply a filter to a dataset which matches no row
    Given I upload the dataset "/data/12L5C.csv" with name "12L5C_dataset"
    Then I wait for the dataset "12L5C_dataset" metadata to be computed
    When I apply the filter "((0002 between [1510786800000, 1512082800000]))" on dataset "12L5C_dataset"
    Then The characteristics of the dataset "12L5C_dataset" match:
      | records              | /data/filter/content_no_records.json    |
      | quality              | /data/filter/12L5C_initial_quality.json |
      | sample_records_count | 12                                      |


  @CleanAfter
  Scenario Outline: Apply a filter to a preparation
    When I upload the dataset "/data/12L5C.csv" with name "12L5C_dataset"
    Then I wait for the dataset "12L5C_dataset" metadata to be computed
    And I create a preparation with name "12L5C_preparation", based on "12L5C_dataset" dataset
    And I add a "uppercase" step on the preparation "12L5C_preparation" with parameters :
      | column_name | firstname |
      | column_id   | 0001      |

    And I apply the filter "<tql_1>" on the preparation "12L5C_preparation"
    Then The characteristics of the preparation "12L5C_preparation" match:
      | records              | /data/filter/<filtered_records_1>       |
      | quality              | /data/filter/12L5C_initial_quality.json |
      | sample_records_count | 12                                      |

    When I remove all filters on preparation "12L5C_preparation"
    Then The characteristics of the preparation "12L5C_preparation" match:
      | records              | /data/filter/12L5C_prep_no_filter_records.json |
      | quality              | /data/filter/12L5C_initial_quality.json        |
      | sample_records_count | 12                                             |

    And I apply the filter "<tql_2>" on the preparation "12L5C_preparation"
    Then The characteristics of the preparation "12L5C_preparation" match:
      | records              | /data/filter/<filtered_records_2>       |
      | quality              | /data/filter/12L5C_initial_quality.json |
      | sample_records_count | 12                                      |

    Examples:
      | tql_1                      | filtered_records_1                           | tql_2                                                               | filtered_records_2                                                |
      | ((0004 contains 'domain')) | 12L5C_prep_0004_contains_domain_records.json | ((0002 is empty) or (0002 is invalid)) and ((0001 contains 'a'))    | 12L5C_prep_0002_empty_or_invalid_and_0001_contains_a_records.json |
      | ((0003 contains '88'))     | 12L5C_prep_0003_contains_88_records.json     | (0002 is valid) and ((0002 between [1498428000000, 1512082800000])) | 12L5C_prep_0002_valid_and_between_records.json                    |


  @CleanAfter
  Scenario: Apply a filter to a preparation step
    When I upload the dataset "/data/12L5C.csv" with name "12L5C_dataset"
    Then I wait for the dataset "12L5C_dataset" metadata to be computed
    And I create a preparation with name "12L5C_preparation", based on "12L5C_dataset" dataset
    And I add a "uppercase" step identified by "step_with_filter" on the preparation "12L5C_preparation" with parameters :
      | column_name | firstname                  |
      | column_id   | 0001                       |
      | filter      | ((0004 contains 'domain')) |
    Then The step "step_with_filter" is applied with the filter "((0004 contains 'domain'))"

    When I apply the filter "((0004 contains 'domain'))" on the preparation "12L5C_preparation"
    Then The characteristics of the preparation "12L5C_preparation" match:
      | records              | /data/filter/12L5C_prep_step_0004_contains_domain_filtered_records.json |
      | quality              | /data/filter/12L5C_initial_quality.json                                 |
      | sample_records_count | 12                                                                      |

    When I export the preparation with parameters :
      | exportType           | CSV                          |
      | preparationName      | 12L5C_preparation            |
      | csv_escape_character | "                            |
      | csv_enclosure_char   | "                            |
      | dataSetName          | 12L5C_dataset                |
      | fileName             | 12L5C_export_with_filter.csv |
      | filter               | ((0004 contains 'domain'))   |
    Then I check that "12L5C_export_with_filter.csv" temporary file equals "/data/filter/12L5C_prep_filtered_processed.csv" file

    When I export the preparation with parameters :
      | exportType           | CSV               |
      | preparationName      | 12L5C_preparation |
      | csv_escape_character | "                 |
      | csv_enclosure_char   | "                 |
      | dataSetName          | 12L5C_dataset     |
      | fileName             | 12L5C_export.csv  |
    Then I check that "12L5C_export.csv" temporary file equals "/data/filter/12L5C_processed.csv" file

    When I remove all filters on preparation "12L5C_preparation"
    Then The characteristics of the preparation "12L5C_preparation" match:
      | records              | /data/filter/12L5C_prep_filtered_step_no_filter_records.json |
      | quality              | /data/filter/12L5C_initial_quality.json                      |
      | sample_records_count | 12                                                           |


  @CleanAfter
  Scenario Outline: Apply a filter and keep the filtered rows to change the current sample
    When I upload the dataset "/data/12L5C.csv" with name "12L5C_dataset"
    Then I wait for the dataset "12L5C_dataset" metadata to be computed
    And I create a preparation with name "12L5C_preparation", based on "12L5C_dataset" dataset
    And I apply the filter "<tql>" on the preparation "12L5C_preparation"
    Then The characteristics of the preparation "12L5C_preparation" match:
      | records              | /data/filter/<filtered_records>         |
      | quality              | /data/filter/12L5C_initial_quality.json |
      | sample_records_count | 12                                      |

    And I add a "keep_only" step identified by "keep_filtered_rows" on the preparation "12L5C_preparation" with parameters :
      | column_name | webdomain |
      | column_id   | 0004      |
      | filter      | <tql>     |
    Then The step "keep_filtered_rows" is applied with the filter "<tql>"
    And I apply the filter "(* is empty)" on the preparation "12L5C_preparation"
    Then The characteristics of the preparation "12L5C_preparation" match:
      | records              | /data/filter/<filtered_records>   |
      | quality              | /data/filter/<new_sample_quality> |
      | sample_records_count | <new_sample_records_count>        |
    When I remove all filters on preparation "12L5C_preparation"
    Then The characteristics of the preparation "12L5C_preparation" match:
      | records              | /data/filter/<filtered_records>   |
      | quality              | /data/filter/<new_sample_quality> |
      | sample_records_count | <new_sample_records_count>        |

    Examples:
      | tql                                             | filtered_records                              | new_sample_quality                           | new_sample_records_count |
      | (* is empty)                                    | 12L5C_prep_rows_with_empty_value_records.json | 12L5C_prep_filtered_rows_sample_quality.json | 6                        |
      | ((0002 between [1512082800000, 1512082800000])) | content_no_records.json                       | 12L5C_initial_quality.json                   | 0                       |
