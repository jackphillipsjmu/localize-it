package com.alert.microservice.config.api;

import com.alert.microservice.Application;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class, SwaggerAPIConfiguration.class})
@AutoConfigureRestDocs
@AutoConfigureMockMvc
public class Swagger2MarkupTest {
    private static final String SPRINGFOX_OUTPUT_PROP = "io.springfox.staticdocs.outputDir";
    private static final String SWAGGER_FILENAME = "swagger.json";
    private static final String SWAGGER_DOC_ENDPOINT = "/v2/api-docs";

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    @Test
    public void createSpringfoxSwaggerJson() throws Exception {
        // Retrieve output location through system properties
        String outputDir = System.getProperty(SPRINGFOX_OUTPUT_PROP);
        // Call Swagger UI's api-docs endpoint to get JSON describing service
        MvcResult mvcResult = this.mockMvc.perform(get(SWAGGER_DOC_ENDPOINT)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        // Get Response and transform to a String
        MockHttpServletResponse response = mvcResult.getResponse();
        String swaggerJson = response.getContentAsString();
        // Write out JSON file to be picked up by AsciiDoctor to be converted
        Files.createDirectories(Paths.get(outputDir));
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputDir, SWAGGER_FILENAME), StandardCharsets.UTF_8)) {
            writer.write(swaggerJson);
        }
    }
}