Feature: Fraud Detection

  @single
  Scenario Outline: Single transaction scenario: amount above $1000 then notify
    Given Customer has a credit card with account number "<cc>"
    When Customer transacts $<amount> at "<event>"
    Then Fraud flag is "<flag>"
    And the suspicious amount is $<total>
    Examples:
      |cc|amount|event|flag|total|
      |4567-8901-2345-6789|500.50|2020-12-10T13:50:40Z|N|0|
      |4567-8901-2345-6789|1000|2020-12-10T13:50:40Z|N|0 |
      |4567-8901-2345-6789|1000.50|2020-12-10T13:50:40Z|Y|1000.50|
      |4567-8901-2345-6789|1500|2020-12-10T13:50:40Z|Y|1500|

  @hopping
  Scenario Outline: Accumulative transaction during 5 minutes interval: amount above $1500 then notify
    Given Customer has a credit card with account number "<cc>"
    When Customer transacts $<amount1> at "<event1>"
    And Customer transacts $<amount2> at "<event2>"
    And Customer transacts $<amount3> at "<event3>"
    Then Fraud flag is "<flag>"
    And the suspicious amount is $<total>
    Examples:
      |cc|amount1|amount2|amount3|event1|event2|event3|flag|total|
      |4567-8901-2345-6789|100.50|500.50|500.0|2020-12-10T13:00:00Z|2020-12-10T13:01:00Z|2020-12-10T13:05:00Z|N|0|
      |4567-8901-2345-6789|500.50|500.50|500.0|2020-12-10T13:00:00Z|2020-12-10T13:01:00Z|2020-12-10T13:05:00Z|Y|1501.0|
      |4567-8901-2345-6789|500.50|500.50|500.0|2020-12-10T13:00:00Z|2020-12-10T13:01:00Z|2020-12-10T13:06:00Z|N|0|

  @session
  Scenario Outline: Accumulative transaction in a single period of activity with 1 hour inactivity gap: amount above $4000 then notify
    Given Customer has a credit card with account number "<cc>"
    When Customer transacts $<amount1> at "<event1>"
    And Customer transacts $<amount2> at "<event2>"
    And Customer transacts $<amount3> at "<event3>"
    And Customer transacts $<amount4> at "<event4>"
    And Customer transacts $<amount5> at "<event5>"
    Then Fraud flag is "<flag>"
    And the suspicious amount is $<total>
    Examples:
      |cc|amount1|amount2|amount3|amount4|amount5|event1|event2|event3|event4|event5|flag|total|
      |4567-8901-2345-6789|1000|1000|1000|1000|0|2020-12-10T13:00:00Z|2020-12-10T13:30:00Z|2020-12-10T13:30:00Z|2020-12-10T13:40:00Z|2020-12-10T13:50:00Z|N|0|
      |4567-8901-2345-6789|1000|1000|1000|1000|1000|2020-12-10T13:00:00Z|2020-12-10T13:30:00Z|2020-12-10T13:30:00Z|2020-12-10T13:40:00Z|2020-12-10T13:50:00Z|Y|5000|
      |4567-8901-2345-6789|1000|1000|1000|1000|1000|2020-12-10T13:00:00Z|2020-12-10T14:01:00Z|2020-12-10T14:30:00Z|2020-12-10T14:40:00Z|2020-12-10T14:50:00Z|N|0|
      # this will set flag to Y because amount to is $1500. it means breaking the first scenario then we still need to informed the suspicious transaction.
      |4567-8901-2345-6789|1000|1500|1000|1000|0|2020-12-10T13:00:00Z|2020-12-10T14:01:00Z|2020-12-10T14:30:00Z|2020-12-10T14:40:00Z|2020-12-10T14:50:00Z|Y|1500|