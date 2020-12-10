Feature: Fraud Detection

  Scenario Outline: Single transaction scenario
    Given Customer has a credit card with account number "<cc>"
    When Customer transacts $<amount> at "<event>"
    Then Fraud flag is "<flag>"
    Examples:
      |cc|amount|event|flag|
      |4567-8901-2345-6789|500.50|2020-12-10T13:50:40Z|N|
      |4567-8901-2345-6789|1000|2020-12-10T13:50:40Z|N|
      |4567-8901-2345-6789|1000.50|2020-12-10T13:50:40Z|Y|
      |4567-8901-2345-6789|1500|2020-12-10T13:50:40Z|Y|

  Scenario Outline: Accumulative transaction during 5 minutes interval
    Given Customer has a credit card with account number "<cc>"
    When Customer transacts $<amount1> at "<event1>"
    And Customer transacts $<amount2> at "<event2>"
    And Customer transacts $<amount3> at "<event3>"
    Then Fraud flag is "<flag>"
    Examples:
      |cc|amount1|amount2|amount3|event1|event2|event3|flag|
      |4567-8901-2345-6789|100.50|500.50|500.0|2020-12-10T13:00:00Z|2020-12-10T13:01:00Z|2020-12-10T13:05:00Z|N|
      |4567-8901-2345-6789|500.50|500.50|500.0|2020-12-10T13:00:00Z|2020-12-10T13:01:00Z|2020-12-10T13:05:00Z|Y|
      |4567-8901-2345-6789|500.50|500.50|500.0|2020-12-10T13:00:00Z|2020-12-10T13:01:00Z|2020-12-10T13:06:00Z|N|
