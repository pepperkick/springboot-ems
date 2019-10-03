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

    @Test
    public void shouldPingApi() throws Exception {
        mockMvc.
            perform(get(path).accept(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isOk()).
            andExpect(content().contentTypeCompatibleWith("application/json")).
            andExpect(jsonPath("$").isArray());
    }

    @Test
    public void shouldGetDirector() throws Exception {
        mockMvc.
            perform(get(path + "/1").accept(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(content().contentTypeCompatibleWith("application/json")).
            andExpect(jsonPath("$.id").value(1)).
            andExpect(jsonPath("$.name").value("Thor"));
    }

    @Test
    public void shouldFailToGetEmployeeDueToInvalidID() throws Exception {
        mockMvc.
            perform(get(path + "/-1").accept(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isBadRequest());
    }

    @Test
    public void shouldFailToGetEmployeeDueToNotFound() throws Exception {
        mockMvc.
            perform(get(path + "/100").accept(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isNotFound());
    }

    @Test
    public void shouldAddEmployee() throws Exception {
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

    @Test
    public void shouldFailToAddEmployeeDueToMissingParam() throws Exception {
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

    @Test
    public void shouldFailToAddEmployeeDueToHigherDesignation() throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", "BlackPanther");
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

    @Test
    public void shouldFailToAddEmployeeDueToMultipleDirectors() throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", "BlackPanther");
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

    @Test
    public void shouldFailToAddEmployeeDueToInvalidDesignation() throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", "BlackPanther");
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

    @Test
    public void shouldFailToAddEmployeeDueToInvalidManager() throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", "BlackPanther");
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

    @Test
    public void shouldDeleteEmployee() throws Exception {
        mockMvc.
            perform(delete(path + "/10")).
            andDo(print()).
            andExpect(status().isOk());
    }

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

    @Test
    public void shouldFailToDeleteEmployeeDueToIdNotFound() throws Exception {
        mockMvc.
            perform(delete(path + "/100")).
            andDo(print()).
            andExpect(status().isNotFound());
    }

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

    @Test
    public void shouldDeleteDirector() throws Exception {
        for (int i = 2; i < 20; i++)
            mockMvc.perform(delete(path + "/" + i));

        mockMvc.
            perform(delete(path +"/1")).
            andDo(print()).
            andExpect(status().isOk());
    }

    @Test
    public void shouldUpdateName() throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", "Nick Fury");

        mockMvc.
            perform(put(path + "/1").content(String.valueOf(body)).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isOk()).
            andExpect(jsonPath("$.name").value(body.get("name")));
    }

    @Test
    public void shouldFailToUpdateManagerOfDirector() throws Exception {
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

    @Test
    public void shouldFailToUpdateDueToInvalidID() throws Exception {
        mockMvc.
            perform(put(path + "/-1").contentType(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReplaceDirector() throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", "Nick Fury");
        body.put("jobTitle", "Director");

        mockMvc.
            perform(put(path + "/1").content(String.valueOf(body)).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isOk()).
            andExpect(jsonPath("$.name").value(body.get("name")));
    }

    @Test
    public void shouldFailToPutDueToInvalidDataType() throws Exception {
        mockMvc.
            perform(put(path + "/1")).
            andDo(print()).
            andExpect(status().isUnsupportedMediaType());
    }

    @Test
    public void shouldFailToPatch() throws Exception {
        mockMvc.
            perform(patch(path)).
            andDo(print()).
            andExpect(status().isMethodNotAllowed());
    }

    @Test
    public void shouldFailToGetEmployeeList() throws Exception {
        for (int i = 0; i < 20; i++)
            mockMvc.perform(delete(path + "/" + i));
        mockMvc.perform(delete(path + "/1"));

        mockMvc.
            perform(get(path)).
            andDo(print()).
            andExpect(status().isNotFound());
    }
}
