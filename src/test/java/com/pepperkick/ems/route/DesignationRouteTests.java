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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = { Application.class, H2Configuration.class })
public class DesignationRouteTests extends AbstractTransactionalTestNGSpringContextTests {
    @Autowired
    private DesignationRoute designationRoute;

    @Autowired
    private MockMvc mockMvc;

    private String path = "/api/v1/designations";

    @Test
    public void isRouteRunning() throws Exception {
        assertThat(designationRoute).isNotNull();
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
    public void shouldAddDesignation() throws Exception {
        JSONObject body = new JSONObject();
        body.put("name", "DBMS");
        body.put("higher", 6);
        body.put("equals", true);

        mockMvc.
            perform(post(path).content(String.valueOf(body)).accept(MediaType.APPLICATION_JSON).contentType(MediaType.APPLICATION_JSON)).
            andDo(print()).
            andExpect(status().isOk());
    }
}
