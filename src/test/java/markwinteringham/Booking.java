package markwinteringham;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;

public class Booking extends Base {

    @Test
    public void getAuthToken(){

        Response r =getTokenResponse().then()
        .statusCode(200)
        .body("token",is(notNullValue())).extract().response();
        System.out.println(r.jsonPath().getString("token"));
    }


    @Test
    public void getBookingIds()
    {
        Response response = get("/booking");
        System.out.println(response.prettyPrint());
        System.out.println("The status code of the request is: "+response.statusCode());
        Assert.assertEquals(response.statusCode(), 200);
        ArrayList<String> bookingIds = response.jsonPath().get("bookingid");
        System.out.println(bookingIds);
        System.out.println("Se encontraron : "+bookingIds.size()+" booking ids");
        Assert.assertTrue(bookingIds.size()>0,"No se encontró ningún resultado");
    }

    @Test
    public void getBookingIdsByName()
    {
        String firstName = "Jim";
        String lastName = "Wilson";
        given(RestAssured.requestSpecification)
        .queryParam("firstname",firstName)
        .queryParam("lastname", lastName)
        .when()
        .get("/booking")
        .then()
        .assertThat()
        .statusCode(200)
        .and()
        .body("", instanceOf(List.class)); // "" or "$" to indicate the json root
                       //Validating if its a JSON Array
                       // Validate a JSON object use Map.class  
    }

    @Test
    public void getBookingIdsByCheckInCheckOut()
    {
        String checkInDate = "2020-01-01";
        //String checkOutDate = "2020-10-01";
        Response res = given(RestAssured.requestSpecification)
        .queryParam("checkin",checkInDate)
        //.queryParam("checkout", checkOutDate)
        .get("/booking");
        Assert.assertTrue(res.jsonPath().getList("$").size()>0);
    }

    @Test
    public void getBookingSchemaValidation()
    {
            String bookingId = "2";
            given(RestAssured.requestSpecification)
            .pathParam("id",bookingId)
            .get("/booking/{id}")
            .then()
            .assertThat()
            .body(matchesJsonSchema(new File(System.getProperty("user.dir")+"/src/resources/schema.json")));
    }

    @Test
    public void getBookingById()
    {
            String bookingId = "5";
            given(RestAssured.requestSpecification)
            //There are two ways to pass path parameters
            //.pathParam("id",bookingId) 
            .get("/booking/{id}",bookingId)
            .then()
            .assertThat()
            .statusCode(200)
            .body("firstname",equalTo("Mary"))
            .and()
            .body("totalprice", greaterThan(0),"bookingdates.checkin", startsWith("20"),"depositpaid", is(notNullValue()), "depositpaid",equalTo(true));
    }

    @Test
    public void createBooking(){
        BookingPojo bp = new BookingPojo();
        BookingDates bd = new BookingDates();
        String firstname = "Salma";
        String lastname = "Lastra";
        int totalprice = 145;
        boolean depositpaid = false;
        bd.setCheckin("2018-01-01");
        bd.setCheckout("2019-01-01");
        String additional ="Dinner";


        bp.setFirstname(firstname);
        bp.setLastname(lastname);
        bp.setTotalprice(totalprice);
        bp.setDepositpaid(depositpaid);
        bp.setBookingdates(bd);
        bp.setAdditionalneeds(additional);

        given().spec(RestAssured.requestSpecification)
        .and()
        .body(bp)
        .when()
        .post("/booking")
        .then()
        .assertThat()
        .statusCode(200)
        .and()
        .statusLine("HTTP/1.1 200 OK")
        .and()
        .body("booking.firstname", equalToIgnoringCase(firstname),
         "booking.lastname",equalToIgnoringCase(lastname),
          "booking.totalprice",equalTo(145),
          "booking.bookingdates.checkin",equalTo(bd.getCheckin()),
           "booking.bookingdates.checkout",equalTo(bd.getCheckout()),
           "$",hasKey("bookingid"));
    }

    @Test
    public void updateBookingWithCookieToken(){
        BookingPojo bp = new BookingPojo();
        BookingDates bd = new BookingDates();
        String firstname = "Salma";
        String lastname = "Lastra";
        int totalprice = 345;
        boolean depositpaid = false;
        bd.setCheckin("2015-01-01");
        bd.setCheckout("2019-01-01");
        String additional ="Coffee";


        bp.setFirstname(firstname);
        bp.setLastname(lastname);
        bp.setTotalprice(totalprice);
        bp.setDepositpaid(depositpaid);
        bp.setBookingdates(bd);
        bp.setAdditionalneeds(additional);

        Response responseWithToken = getTokenResponse();
        System.out.println(responseWithToken.jsonPath().getString("token"));
        String bookingId = "4";
        Response r =  given().spec(RestAssured.requestSpecification)
        .pathParam("id", bookingId)
        .cookie("token",responseWithToken.jsonPath().getString("token"))
        .body(bp)
        .when()
        .put("/booking/{id}");
        System.out.println(r.prettyPrint());
    }

    @Test
    public void updateBookingWithAuthorizationHeader(){
        BookingPojo bp = new BookingPojo();
        BookingDates bd = new BookingDates();
        String firstname = "Eva";
        String lastname = "Hernandez";
        int totalprice = 345;
        boolean depositpaid = false;
        bd.setCheckin("2015-01-01");
        bd.setCheckout("2019-01-01");
        String additional ="Coffee";


        bp.setFirstname(firstname);
        bp.setLastname(lastname);
        bp.setTotalprice(totalprice);
        bp.setDepositpaid(depositpaid);
        bp.setBookingdates(bd);
        bp.setAdditionalneeds(additional);

        String bookingId = "3";
        Response r =  given().spec(RestAssured.requestSpecification)
        .header("Authorization", "Basic YWRtaW46cGFzc3dvcmQxMjM=")
        .pathParam("id", bookingId)
        .body(bp)
        .when()
        .put("/booking/{id}");
        System.out.println(r.prettyPrint());
    }


    private Response getTokenResponse(){
        UserPojo up = new UserPojo();
        up.setUsername("admin");
        up.setPassword("password123");

        return given(RestAssured.requestSpecification)
        .body(up)
        .when()
        .post("/auth");
    }

    @Test
    public void partialUpdateBookinWithAuthorizationHeader(){
        String bookingId = "4";
        Response r =  given().spec(RestAssured.requestSpecification)
        .header("Authorization", "Basic YWRtaW46cGFzc3dvcmQxMjM=")
        .pathParam("id", bookingId)
        .body(new File("src/resources/patchUpdate.json"))
        .when()
        .patch("/booking/{id}");
        System.out.println(r.prettyPrint());
    }

    @Test
    public void partialUpdateBookingWithCookieToken()
    {
        Response responseWithToken = getTokenResponse();
        String bookingId = "5";
        Response r =  given().spec(RestAssured.requestSpecification)
        .pathParam("id", bookingId)
        .cookie("token",responseWithToken.jsonPath().getString("token"))
        .body(new File("src/resources/patchUpdate.json"))
        .when()
        .patch("/booking/{id}");
        System.out.println(r.prettyPrint());
    }

    @Test
    public void deleteBookingWithCookieToken(){
        Response responseWithToken = getTokenResponse();
        String bookingId = "5";
        Response r =  given().spec(RestAssured.requestSpecification)
        .pathParam("id", bookingId)
        .cookie("token",responseWithToken.jsonPath().getString("token"))
        .when()
        .delete("/booking/{id}")
        .then()
        .assertThat().statusLine("HTTP/1.1 201 Created").extract().response();
        System.out.println(r.prettyPrint());
    }

    @Test
    public void deleteBookingWithAuthorizationHeader(){
        String bookingId = "1";
        Response r =  given().spec(RestAssured.requestSpecification)
        .pathParam("id", bookingId)
        .header("Authorization", "Basic YWRtaW46cGFzc3dvcmQxMjM=")
        .when()
        .delete("/booking/{id}")
        .then()
        .assertThat().statusLine("HTTP/1.1 201 Created").extract().response();
        System.out.println(r.prettyPrint());
    }


    @Test
    public void pingCheck(){
        Response r= given()
        .when()
        .get("/ping")
        .then()
        .assertThat()
        .statusCode(201)
        .statusLine("HTTP/1.1 201 Created").extract().response();
        System.out.println(r.prettyPrint());
    }
}
