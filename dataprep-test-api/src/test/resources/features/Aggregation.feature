@EnvOS @EnvOnPremise @EnvCloud
Feature: Make a aggregation

  Scenario: Make an aggregation on a preparation (Average)
    Given I upload the dataset "/data/scores.csv" with name "scores_dataset"
    And I create a preparation with name "scores_preparation", based on "scores_dataset" dataset
    And I add a "uppercase" step on the preparation "scores_preparation" with parameters :
      | column_name | Name |
      | column_id   | 0002 |

    When I apply an aggregation "scores_aggregation" on the preparation "scores_preparation" with parameters :
      | operator | AVERAGE |
      | columnId | 0001    |
      | groupBy  | 0002    |

    Then The aggregation "scores_aggregation" results with the operator "AVERAGE" is :
      | BACK  | 17.25 |
      | FRONT | 15.75 |

  Scenario: Make an aggregation on a preparation (Min)
    When I apply an aggregation "scores_aggregation" on the preparation "scores_preparation" with parameters :
      | operator | MIN     |
      | columnId | 0001    |
      | groupBy  | 0002    |

    Then The aggregation "scores_aggregation" results with the operator "MIN" is :
      | FRONT | 12.0 |
      | BACK  | 12.0 |

  Scenario: Make an aggregation on a preparation (Max)
    When I apply an aggregation "scores_aggregation" on the preparation "scores_preparation" with parameters :
      | operator | MAX     |
      | columnId | 0001    |
      | groupBy  | 0002    |

    Then The aggregation "scores_aggregation" results with the operator "MAX" is :
      | BACK  | 20.0 |
      | FRONT | 19.0 |

  Scenario: Make an aggregation on a preparation (Sum)
    When I apply an aggregation "scores_aggregation" on the preparation "scores_preparation" with parameters :
      | operator | SUM     |
      | columnId | 0001    |
      | groupBy  | 0002    |

    Then The aggregation "scores_aggregation" results with the operator "SUM" is :
      | BACK  | 69.0 |
      | FRONT | 63.0 |

  Scenario: Invalid Operation in an aggregation a preparation
    When I fail to apply an aggregation preparation "scores_preparation" with parameters :
      | operator | TOTO    |
      | columnId | 0001    |
      | groupBy  | 0002    |

  Scenario: Invalid Operation in an aggregation a preparation
    When I fail to apply an aggregation on non existing preparation "fake_scores_preparation" with parameters :
      | operator | SUM    |
      | columnId | 0001    |
      | groupBy  | 0002    |

  Scenario: Make an aggregation on a filtered preparation
    When I apply an aggregation "scores_aggregation" on the preparation "scores_preparation" with parameters :
      | operator | SUM     |
      | columnId | 0001    |
      | groupBy  | 0002    |
      | filter   | { "eq": { "field": "0002", "value": "BACK"}} |

    Then The aggregation "scores_aggregation" results with the operator "SUM" is :
      | BACK  | 69.0 |

  Scenario: Make an aggregation on a dataSet (Min)
    When I apply an aggregation "scores_aggregation" on the dataSet "scores_dataset" with parameters :
      | operator | MIN     |
      | columnId | 0001    |
      | groupBy  | 0002    |

    Then The aggregation "scores_aggregation" results with the operator "MIN" is :
      | back  | 12.0 |
      | front | 12.0 |

  Scenario: Make an aggregation on a dataSet with filter
    When I apply an aggregation "scores_aggregation" on the dataSet "scores_dataset" with parameters :
      | operator | MIN     |
      | columnId | 0001    |
      | groupBy  | 0002    |
      | filter   | { "eq": { "field": "0002", "value": "back"}} |

    Then The aggregation "scores_aggregation" results with the operator "MIN" is :
      | back  | 12.0 |

  Scenario: Make an aggregation on a dataSet with null filter
    When I apply an aggregation "scores_aggregation" on the dataSet "scores_dataset" with parameters :
      | operator | MIN     |
      | columnId | 0001    |
      | groupBy  | 0002    |
      | filter   | null    |

    Then The aggregation "scores_aggregation" results with the operator "MIN" is :
      | back  | 12.0 |
      | front | 12.0 |

  Scenario: Invalid Operation in an aggregation with preparation and dataset
    When I fail to apply an aggregation on preparation "scores_preparation" and dataSet "scores_dataset" with parameters :
      | operator | SUM    |
      | columnId | 0001    |
      | groupBy  | 0002    |
