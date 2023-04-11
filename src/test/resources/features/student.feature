@student
Feature: Create Student

  Scenario: Create student as a teacher and verify status code is 201
    Given I logged Bookit api as a "teacher"
    When I send POST request "/api/students/student" endpoint with following information
      | first-name      | Arzu              |
      | last-name       | Husin               |
      | email           | aciinch@cydeo.com    |
      | password        | abc123              |
      | role            | student-team-member |
      | campus-location | VA                  |
      | batch-number    | 20                   |
      | team-name       | TheyBite               |
    Then status code should be 201
    And I delete previously added student

    #Email information needs to be unique.Change email info from there to do it for you unique