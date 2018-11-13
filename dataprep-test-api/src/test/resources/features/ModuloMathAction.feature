@EnvOS @EnvOnPremise @EnvCloud @Action @Test
Feature: Check some features of the Duplicate Action and the Delete Invalid Action

  # @see <a href="https://jira.talendforge.org/browse/TDP-4889">TDP-4889</a>
  #
  #@CleanAfter
  Scenario: I apply math modulo function on a dataset and check the quality bar, empty cells and invalid cells
    Given I upload the dataset "/data/TestModuloMathFunction.txt" with name "modulo_math_dataset"
    Then I wait for the dataset "modulo_math_dataset" metadata to be computed
    # original data
    Given I create a preparation with name "modulo_math_prep", based on "modulo_math_dataset" dataset
    Then The characteristics of the preparation "modulo_math_prep" match:
      | sample_records_count | 6 |
    Then The preparation "modulo_math_prep" should have the following invalid characteristics on the row number "0":
      | invalidCells | 0000 |
    Then The preparation "modulo_math_prep" should have the following quality bar characteristics on the column number "0":
      | valid   | 5 |
      | invalid | 1 |
      | empty   | 0 |
    # modulo with create a new column option
    And I add a "modulo" step identified by "modulo_math" on the preparation "modulo_math_prep" with parameters :
      | column_id         | 0000          |
      | create_new_column | true          |
      | mode              | constant_mode |
      | constant_value    | 5             |
    Then I check that a step like "modulo_math" exists in the preparation "modulo_math_prep"
    Then The preparation "modulo_math_prep" should contain the following columns:
      | int | int_mod |
    Then The preparation "modulo_math_prep" should have the following invalid characteristics on the row number "0":
      | invalidCells | 0000 |
    Then The preparation "modulo_math_prep" should have the following quality bar characteristics on the column number "0":
      | valid   | 5 |
      | invalid | 1 |
      | empty   | 0 |
    Then The preparation "modulo_math_prep" should have the following quality bar characteristics on the column number "1":
      | valid   | 5 |
      | invalid | 0 |
      | empty   | 1 |
    # modulo on the original column
    And I add a "modulo" step identified by "modulo_math" on the preparation "modulo_math_prep" with parameters :
      | column_id      | 0000          |
      | mode           | constant_mode |
      | constant_value | 5             |
    Then The characteristics of the preparation "modulo_math_prep" match:
      | sample_records_count | 6 |
    Then The preparation "modulo_math_prep" should have the following invalid characteristics on the row number "0":
      | invalidCells |  |
    Then The preparation "modulo_math_prep" should have the following quality bar characteristics on the column number "0":
      | valid   | 5 |
      | invalid | 0 |
      | empty   | 1 |
    Then The preparation "modulo_math_prep" should have the following quality bar characteristics on the column number "1":
      | valid   | 5 |
      | invalid | 0 |
      | empty   | 1 |
