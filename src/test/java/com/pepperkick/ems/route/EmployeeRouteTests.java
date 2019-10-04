package com.pepperkick.ems.route;

import com.pepperkick.ems.Application;
import com.pepperkick.ems.configuration.H2Configuration;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = { Application.class, H2Configuration.class })
public class EmployeeRouteTests extends AbstractTransactionalTestNGSpringContextTests {
    @Autowired
    private EmployeeRoute employeeRoute;

    @Autowired
    private MockMvc mockMvc;

    private String path = "/api/v1/employees";

    @Test
    public void isRouteRunning() throws Exception {
        assertThat(employeeRoute).isNotNull();
    }

    // GET /employees
    // Should successfully get employees list with response code 200
    @Test
    public void shouldGetEmployees() throws Exception {
        mockMvc.
            perform(get(path).accept(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isOk()).
            andExpect(content().contentTypeCompatibleWith("application/json")).
            andExpect(jsonPath("$").isArray());
    }

    // Should fail with response code 404 due to empty employee list
    @Test
    public void shouldFailToGetEmployees() throws Exception {
        for (int i = 0; i < 20; i++)
            mockMvc.perform(delete(path + "/" + i));
        mockMvc.perform(delete(path + "/1"));

        mockMvc.
                perform(get(path)).
                andDo(print()).
                andExpect(status().isNotFound());
    }

    // GET /employee/${id}
    // Should successfully receive employee's information with id 1 and response code 200
    @Test
    public void shouldGetDirector() throws Exception {
        mockMvc.
            perform(get(path + "/1").accept(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(content().contentTypeCompatibleWith("application/json")).
            andExpect(jsonPath("$.id").value(1)).
            andExpect(jsonPath("$.name").value("Thor"));
    }

    // Should fail with response code 400 due to negative id param
    @Test
    public void shouldFailToGetEmployeeDueToInvalidID() throws Exception {
        mockMvc.
            perform(get(path + "/-1").accept(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isBadRequest());
    }

    // Should fail with response code 404 due to employee not found with id
    @Test
    public void shouldFailToGetEmployeeDueToNotFound() throws Exception {
        mockMvc.
            perform(get(path + "/100").accept(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isNotFound());
    }

    // POST /employees
    // Should POST with response code 201 and new employee
    @Test
    public void shouldPostNewEmployee() throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", "DrStrange");
        body.put("jobTitle", "Lead");
        body.put("managerId", 1);

        mockMvc.
            perform(post(path).content(String.valueOf(body)).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isCreated()).
            andExpect(jsonPath("$.name").value(body.get("name")));
    }

    // Should fail to POST with response code 400 due to missing employee name
    @Test
    public void shouldFailToPostNewEmployeeDueToMissingNameParam() throws Exception {
        JSONObject body = new JSONObject();
        body.put("jobTitle", "Lead");
        body.put("managerId", 1);

        mockMvc.
            perform(post(path).content(String.valueOf(body)).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isBadRequest()).
            andExpect(jsonPath("$.message").value(
                "Employee's name cannot be empty"
            ));
    }

    // Should fail to POST with response code 400 due to missing employee jobTitle
    @Test
    public void shouldFailToPostNewEmployeeDueToMissingJobTitleParam() throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", "Black Panther");
        body.put("managerId", 1);

        mockMvc.
            perform(post(path).content(String.valueOf(body)).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isBadRequest()).
            andExpect(jsonPath("$.message").value(
                "Employee's job title cannot be empty"
            ));
    }

    // Should fail to POST with response code 400 due to no designation existing with given job title
    @Test
    public void shouldFailToPostNewEmployeeDueToInvalidDesignation() throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", "Black Panther");
        body.put("jobTitle", "Senior Manager");
        body.put("managerId", 1);

        mockMvc.
            perform(post(path).content(String.valueOf(body)).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isBadRequest()).
            andExpect(jsonPath("$.message").value(
                    "Could not find any designation with the given job title, please make sure the job title matches a designation title"
            ));
    }

    // Should fail to POST with response code 400 due to new employee having higher level designation than it's manager
    @Test
    public void shouldFailToAddEmployeeDueToHigherDesignation() throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", "Black Panther");
        body.put("jobTitle", "Manager");
        body.put("managerId", 3);

        mockMvc.
            perform(post(path).content(String.valueOf(body)).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isBadRequest()).
            andExpect(jsonPath("$.message").value(
                "Employee's designation cannot be higher or equal to it's manager's designation"
            ));
    }

    // Should fail to POST with response code 400 due to restrictions of not having multiple directors
    @Test
    public void shouldFailToAPostNewEmployeeDueToSingleDirectorRestriction() throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", "Black Panther");
        body.put("jobTitle", "Director");
        body.put("managerId", 1);

        mockMvc.
            perform(post(path).content(String.valueOf(body)).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isBadRequest()).
            andExpect(jsonPath("$.message").value(
                "Only one director can be present"
            ));
    }

    // Should fail to POST with response code 400 due to no employee found with manager id
    @Test
    public void shouldFailToAddEmployeeDueToInvalidManager() throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", "Black Panther");
        body.put("jobTitle", "Manager");
        body.put("managerId", 100);

        mockMvc.
            perform(post(path).content(String.valueOf(body)).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isBadRequest()).
            andExpect(jsonPath("$.message").value(
                "No employee found with the supplied manager ID"
            ));
    }

    // Should fail to POST with response code 400 due to large employee's name
    @Test
    public void shouldFailToPostNewEmployeeDueToLargeName() throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", "Black Panther Black Panther Black Panther Black Panther Black Panther Black Panther Black Panther Black Panther Black Panther Black Panther");
        body.put("jobTitle", "Manager");
        body.put("managerId", 1);

        mockMvc.
            perform(post(path).content(String.valueOf(body)).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isBadRequest()).
            andExpect(jsonPath("$.message").value(
                    "Unable to save employee due to constraints error"
            ));
    }


    // Should DELETE with response code 200 and delete the employee
    @Test
    public void shouldDeleteEmployee() throws Exception {
        mockMvc.
            perform(delete(path + "/10")).
            andDo(print()).
            andExpect(status().isOk());
    }

    // Should DELETE with response code 200 and update subordinates manager
    @Test
    public void shouldDeleteEmployeeAndUpdateSubordinates() throws Exception {
        mockMvc.
            perform(delete(path + "/2")).
            andExpect(status().isOk());

        mockMvc.
            perform(get(path + "/5")).
            andDo(print()).
            andExpect(status().isOk()).
            andExpect(jsonPath("$.manager.id").value(1));
    }

    // Should fail to DELETE with response code 404 due to no employee found with given id
    @Test
    public void shouldFailToDeleteEmployeeDueToIdNotFound() throws Exception {
        mockMvc.
            perform(delete(path + "/100")).
            andDo(print()).
            andExpect(status().isNotFound());
    }

    // Should fail DELETE with response 400 due to restriction of unable to delete director with subordinates
    @Test
    public void shouldFailToDeleteDirector() throws Exception {
        mockMvc.
            perform(delete(path + "/1").accept(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isBadRequest()).
            andExpect(jsonPath("$.message").value(
                    "Cannot delete director when subordinates list is not empty"
            ));
    }

    // Should DELETE with response code 200 and delete director
    @Test
    public void shouldDeleteDirector() throws Exception {
        for (int i = 2; i < 20; i++)
            mockMvc.perform(delete(path + "/" + i));

        mockMvc.
            perform(delete(path +"/1")).
            andDo(print()).
            andExpect(status().isOk());
    }

    // Should PUT with response code 200 and update employee name
    @Test
    public void shouldPutAndUpdateEmployeeName() throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", "Nick Fury");

        mockMvc.
            perform(put(path + "/1").content(String.valueOf(body)).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isOk()).
            andExpect(jsonPath("$.name").value(body.get("name")));
    }

    // Should fail to PUT with response code 400 due to restriction of director having no manager
    @Test
    public void shouldFailToPutAndUpdateManagerOfDirector() throws Exception {
        JSONObject body = new JSONObject();
        body.put("managerId", 2);

        mockMvc.
            perform(put(path + "/1").content(String.valueOf(body)).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isBadRequest()).
            andExpect(jsonPath("$.message").value(
                "Employee designation cannot be lower or equal to it's subordinates"
            ));
    }

    // Should fail to PUT with response code 200 due to negative id
    @Test
    public void shouldFailToPutAndUpdateDueToInvalidID() throws Exception {
        mockMvc.
            perform(put(path + "/-1").contentType(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isBadRequest());
    }

    // Should PUT with response code 200 and update director with new details
    @Test
    public void shouldPutAndUpdateDirector() throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", "Nick Fury");

        mockMvc.
            perform(put(path + "/1").content(String.valueOf(body)).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isOk()).
            andExpect(jsonPath("$.name").value(body.get("name")));
    }

    // Should PUT with response code 201 and replace director
    @Test
    public void shouldPutAndReplaceDirector() throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", "Nick Fury");
        body.put("jobTitle", "Director");
        body.put("replace", true);

        mockMvc.
            perform(put(path + "/1").content(body.toString()).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isCreated());
    }


    // Should fail to PUT with response code 415 due to invalid request type
    @Test
    public void shouldFailToPutDueToInvalidDataType() throws Exception {
        mockMvc.
            perform(put(path + "/1")).
            andDo(print()).
            andExpect(status().isUnsupportedMediaType());
    }

    // Should fail to PATCH with response code 405
    @Test
    public void shouldFailToPatch() throws Exception {
        mockMvc.
            perform(patch(path)).
            andDo(print()).
            andExpect(status().isMethodNotAllowed());
    }
}
