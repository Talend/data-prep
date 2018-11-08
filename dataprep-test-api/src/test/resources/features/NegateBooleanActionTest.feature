@EnvOS @EnvOnPremise @EnvCloud @Delete
Feature: Check some features of Negate Boolean Action

  @CleanAfter
  Scenario: I negate a column anc check that type change is applied
    Given I upload the dataset "/data/NegateBooleanAction.txt" with name "NegateBooleanAction_dataset"
    Then I wait for the dataset "NegateBooleanAction_dataset" metadata to be computed
    Given I create a preparation with name "NegateBooleanAction_prep", based on "NegateBooleanAction_dataset" dataset
    And I add a "negate" step identified by "negate_boolean" on the preparation "NegateBooleanAction_prep" with parameters :
      | column_id         | 0000 |
      | create_new_column | true |
    Then I check that a step like "negate_boolean" exists in the preparation "NegateBooleanAction_prep"
    Then The preparation "NegateBooleanAction_prep" should contain the following columns:
      | boolean | boolean_negate |
    # check the types TODO
    And I add a "concat" step identified by "concat_string" on the preparation "NegateBooleanAction_prep" with parameters :
      | column_id | 00001         |
      | mode      | constant_mode |
      | prefix    | aaaa          |
      | sufix     | bbbb          |
    # check the types
