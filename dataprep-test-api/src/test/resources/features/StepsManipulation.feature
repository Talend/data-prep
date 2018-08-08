@EnvOS @EnvOnPremise @EnvCloud
Feature: Disable, enable, reorder, delete steps

  @CleanAfter
  Scenario: Disable all steps of a preparation
    Given I upload the dataset "/data/8L3C.csv" with name "8L3C_dataset"
    And I wait for the dataset "8L3C_dataset" metadata to be computed
    And I create a preparation with name "8L3C_preparation", based on "8L3C_dataset" dataset
    And I add a "uppercase" step identified by "uppercase_on_firstname" on the preparation "8L3C_preparation" with parameters :
      | column_name | firstname |
      | column_id   | 0001      |
    # Need a call to /content to compute the step row metadata
    And I check that I can load "1" times the preparation with name "8L3C_preparation"
    And I add a "create_new_column" step identified by "new_column_on_firstname" on the preparation "8L3C_preparation" with parameters :
      | column_name            | firstname         |
      | column_id              | 0001              |
      | create_new_column_name | new_firstname     |
      | mode_new_column        | other_column_mode |
      | selected_column        | 0001              |
    # Need a call to /content to compute the step row metadata
    And I check that I can load "1" times the preparation with name "8L3C_preparation"
    And I add a "numeric_ops" step identified by "add_2_to_id" on the preparation "8L3C_preparation" with parameters :
      | column_id | 0000          |
      | operator  | +             |
      | operand   | 2             |
      | mode      | constant_mode |
    # Need a call to /content to compute the step row metadata
    And I check that I can load "1" times the preparation with name "8L3C_preparation"
    When I disable the last "3" steps of the preparation "8L3C_preparation"
    Then The characteristics of the preparation "8L3C_preparation" match:
      | records | /data/8L3C_records.json |
