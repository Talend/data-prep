@EnvOS @EnvOnPremise @EnvCloud @Action
Feature: Check some features of the Duplicate Action and the Delete Invalid Action

  # @see <a href="https://jira.talendforge.org/browse/TDP-6272">TDP-6272</a>
  # duplicate action should display invalids cells on new column
  # delete invalid rows should update the quality bar
  @CleanAfter
  Scenario: I delete invalid rows on a dataset and check the quality bar
    Given I upload the dataset "/data/phoneNumber.csv" with name "phoneNumber_delete_invalid_dataset"
    Then I wait for the dataset "phoneNumber_delete_invalid_dataset" metadata to be computed
    Given I create a preparation with name "phoneNumber_delete_invalid_prep", based on "phoneNumber_delete_invalid_dataset" dataset
    Then The characteristics of the preparation "phoneNumber_delete_invalid_prep" match:
      | sample_records_count | 6 |
    Then The preparation "phoneNumber_delete_invalid_prep" should have the following quality bar characteristics on the column number "2":
      | valid   | 5 |
      | invalid | 1 |
      | empty   | 0 |
    And I add a "copy" step identified by "duplicate" on the preparation "phoneNumber_delete_invalid_prep" with parameters :
      | column_id | 0002 |
    Then I check that a step like "duplicate" exists in the preparation "phoneNumber_delete_invalid_prep"
    Then The preparation "phoneNumber_delete_invalid_prep" should contain the following columns:
      | id | lastname | phoneNumber | phoneNumber_copy |
    # duplicate action should display invalids cells on new column
    Then The preparation "phoneNumber_delete_invalid_prep" should have the following invalid characteristics on the row number "2":
      | invalidCells   | 0002,0003 |
    And I add a "delete_invalid" step identified by "delete_invalid" on the preparation "phoneNumber_delete_invalid_prep" with parameters :
      | column_id | 0002 |
    Then The characteristics of the preparation "phoneNumber_delete_invalid_prep" match:
      | sample_records_count | 5 |
    # delete invalid rows should update the quality bar
    Then The preparation "phoneNumber_delete_invalid_prep" should have the following quality bar characteristics on the column number "2":
      | valid   | 5 |
      | invalid | 0 |
      | empty   | 0 |
    Then The preparation "phoneNumber_delete_invalid_prep" should have the following quality bar characteristics on the column number "3":
      | valid   | 5 |
      | invalid | 0 |
      | empty   | 0 |
