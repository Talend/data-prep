@EnvOS @EnvOnPremise @EnvCloud
Feature: Row count of current sample

  Scenario: Apply an action which does not trigger statistics analysis
    Given I upload the dataset "/data/12L5C.csv" with name "12L5C_dataset"
    And I create a preparation with name "12L5C_preparation", based on "12L5C_dataset" dataset
    When I add a "delete_column" step on the preparation "12L5C_preparation" with parameters :
      | column_name | firstname |
      | column_id   | 0001      |
    Then The characteristics of the preparation "12L5C_preparation" match:
      | sample_records_count | 12 |

  Scenario: Apply an action which triggers statistics analysis
    Given I create a preparation with name "12L5C_preparation 2", based on "12L5C_dataset" dataset
    When I add a "uppercase" step on the preparation "12L5C_preparation 2" with parameters :
      | column_name | firstname |
      | column_id   | 0001      |
    Then The characteristics of the preparation "12L5C_preparation 2" match:
      | sample_records_count | 12 |

  @CleanAfter
  Scenario: Apply an action which modifies the total row count of the sample
    Given I create a preparation with name "12L5C_preparation 3", based on "12L5C_dataset" dataset
    When I add a "delete_empty" step on the preparation "12L5C_preparation 3" with parameters :
      | column_name | firstname |
      | column_id   | 0001      |
    Then The characteristics of the preparation "12L5C_preparation 3" match:
      | sample_records_count | 10 |

