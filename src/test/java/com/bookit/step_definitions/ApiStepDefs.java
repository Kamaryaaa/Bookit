package com.bookit.step_definitions;

import com.bookit.pages.SelfPage;
import com.bookit.utilities.BookitUtils;
import com.bookit.utilities.ConfigurationReader;
import com.bookit.utilities.DB_Util;
import com.bookit.utilities.Environment;
import io.cucumber.java.af.En;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.hamcrest.Matchers;
import org.junit.Assert;

import java.util.Map;

import static io.restassured.RestAssured.*;

public class ApiStepDefs {
    String token;
    Response response;
    String emailGlobal;
    int entryId;

    Map<String,String> teamInfo;
    int addedTeamID;

    @Given("I logged Bookit api as a {string}")
    public void i_logged_bookit_api_as_a(String role) {
        token = BookitUtils.generateTokenByRole(role);
        System.out.println("token = " + token);

        Map<String, String> credentialsMap = BookitUtils.returnCredentials(role);

        emailGlobal=credentialsMap.get("email");


    }
    @When("I sent get request to {string} endpoint")
    public void i_sent_get_request_to_endpoint(String endpoint) {
        response = given().accept(ContentType.JSON)
                .header("Authorization", token)
                .when().get(Environment.BASE_URL + endpoint);
    }
    @Then("status code should be {int}")
    public void status_code_should_be(int expectedStatusCode) {
        System.out.println("response.statusCode() = " + response.statusCode());
        //verify status code
        Assert.assertEquals(expectedStatusCode,response.statusCode());

    }
    @Then("content type is {string}")
    public void content_type_is(String expectedContentType) {
        System.out.println("response.contentType() = " + response.contentType());
        Assert.assertEquals(expectedContentType,response.contentType());

    }
    @Then("role is {string}")
    public void role_is(String expectedRole) {
        response.prettyPrint();
        String actualRole = response.path("role");
        Assert.assertEquals(actualRole,expectedRole);

    }

    @Then("the information about current user from api and database should match")
    public void the_information_about_current_user_from_api_and_database_should_match() {
       //get data from api
        JsonPath jsonPath = response.jsonPath();
        /*
        {
            "id": 17381,
            "firstName": "Raymond",
            "lastName": "Reddington",
            "role": "student-team-member"
        }
         */
        String actualFirstName = jsonPath.getString("firstName");
        String actualLastName = jsonPath.getString("lastName");
        String actualRole = jsonPath.getString("role");


        // and get data from db
        String query = "select firstname,lastname,role from users\n" +
                "where email ='"+emailGlobal+"'";
       DB_Util.runQuery(query);
        Map<String, String> dbMap = DB_Util.getRowMap(1);
        System.out.println("dbMap = " + dbMap);
        String expectedFirstName = dbMap.get("firstname");
        String expectedLastName = dbMap.get("lastname");
        String expectedRole = dbMap.get("role");

        // compare them
        Assert.assertEquals(expectedFirstName,actualFirstName);
        Assert.assertEquals(expectedLastName,actualLastName);
        Assert.assertEquals(expectedRole,actualRole);
    }

    @Then("UI,API and Database user information must be match")
    public void ui_api_and_database_user_information_must_be_match() {

            //get data from api
            JsonPath jsonPath = response.jsonPath();
        /*
        {
            "id": 17381,
            "firstName": "Raymond",
            "lastName": "Reddington",
            "role": "student-team-member"
        }
         */
            String actualFirstName = jsonPath.getString("firstName");
            String actualLastName = jsonPath.getString("lastName");
            String actualRole = jsonPath.getString("role");


            // and get data from db
            String query = "select firstname,lastname,role from users\n" +
                    "where email ='" + emailGlobal + "'";
            DB_Util.runQuery(query);
            Map<String, String> dbMap = DB_Util.getRowMap(1);
            System.out.println("dbMap = " + dbMap);
            String expectedFirstName = dbMap.get("firstname");
            String expectedLastName = dbMap.get("lastname");
            String expectedRole = dbMap.get("role");

            // compare them
            Assert.assertEquals(expectedFirstName, actualFirstName);
            Assert.assertEquals(expectedLastName, actualLastName);
            Assert.assertEquals(expectedRole, actualRole);

         //get data from UI
        SelfPage selfPage=new SelfPage();

        String actualFullNameUI=selfPage.name.getText();
        String actualRoleUI = selfPage.role.getText();
            //UI vs DB

            String expectedFullName = expectedFirstName+" "+expectedLastName;
            Assert.assertEquals(expectedFullName,actualFullNameUI);
            Assert.assertEquals(expectedRole,actualRole);

            //UI vs API
        String expectedNameFromAPI=actualFirstName+" "+actualLastName;
        Assert.assertEquals(expectedNameFromAPI,actualFullNameUI);
        Assert.assertEquals(expectedRole,actualRoleUI);

        }

    @When("I send POST request {string} endpoint with following information")
    public void i_send_post_request_endpoint_with_following_information(String endpoint, Map<String, String> studentsInfo) {
         response = given().accept(ContentType.JSON).header("Authorization",token)
                .queryParams(studentsInfo).when().post(Environment.BASE_URL+endpoint).then().extract().response().prettyPeek();

        entryId = response.path("entryiId");

    }
    @Then("I delete previously added student")
    public void i_delete_previously_added_student() {
        given().header("Authorization",token).pathParam("id",entryId)
                .when().delete(Environment.BASE_URL+"/api/students/{id}").then().statusCode(204);

    }



    @When("Users sends POST request to {string} with following info:")
    public void users_sends_post_request_to_with_following_info(String endPoint, Map<String,String> teamInfo) {
        this.teamInfo=teamInfo;
       response= given().accept(ContentType.JSON).header("Authorization",token)
                .queryParams(teamInfo).when().post(Environment.BASE_URL+endPoint)
                .then().extract().response().prettyPeek();
        addedTeamID = response.path("entryiId");

    }
    @Then("Database should persist same team info")
    public void database_should_persist_same_team_info() {

        String query = "select name, batch_number, location from team\n" +
                "join campus c on team.campus_id = c.id\n" +
                "where team.id="+addedTeamID+" and name='"+teamInfo.get("team-name")+"' and batch_number ="+teamInfo.get("batch-number");
        DB_Util.runQuery(query);
        Map<String, String> teamInfoDB = DB_Util.getRowMap(1);
        String actualName = teamInfoDB.get("name");
        String actualLocation = teamInfoDB.get("location");
        String actualBatchNumber = teamInfoDB.get("batch_number");
        System.out.println("teamInfoDB = " + teamInfoDB);

        response = given().accept(ContentType.JSON)
                .header("Authorization", token)
                .when().get(Environment.BASE_URL + "/api/teams/"+addedTeamID);
        JsonPath jp = response.jsonPath();

        String expectedTeamName = jp.getString("name");
        String expectedBatchNumber = teamInfo.get("batch-number");
        String expectedLocation = teamInfo.get("campus-location");

        Assert.assertEquals(expectedTeamName,actualName);
        Assert.assertEquals(expectedBatchNumber,actualBatchNumber);
        Assert.assertEquals(expectedLocation,actualLocation);





    }
    @Then("User deletes previously created team")
    public void user_deletes_previously_created_team() {

        given().header("Authorization",token)
                .pathParam("id",addedTeamID)
                .when()
                .delete(Environment.BASE_URL+"/api/teams/{id}")
                .then().statusCode(200)
                .body("message", Matchers.is("team "+teamInfo.get("team-name")+" has been successfully removed"));

    }









}



