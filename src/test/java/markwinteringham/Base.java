package markwinteringham;

import org.testng.annotations.BeforeClass;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class Base 
{
    @BeforeClass(alwaysRun = true)
    public static void setUpRestAssured()
    {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
        //RestAssured.basePath = ""; Se asigna el basePath en cada peticion, debido a que va cambiando
        RequestSpecification requestSpecification = new RequestSpecBuilder().
        addHeader("Content-Type",ContentType.JSON.toString()).
        addHeader("Accept", ContentType.JSON.toString())
        .build();
        RestAssured.requestSpecification = requestSpecification;
    }    
}
