package org.appverse.builder.web.rest;

import org.appverse.builder.Application;
import org.appverse.builder.config.AppverseBuilderProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.inject.Inject;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by panthro on 17/02/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class AppverseBuilderCliInfoResourceTest {


    @Inject
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Inject
    private AppverseBuilderProperties appverseBuilderProperties;

    private MockMvc restMockMvc;


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        AppverseBuilderCliInfoResource appverseBuilderCliInfoResource = new AppverseBuilderCliInfoResource();
        ReflectionTestUtils.setField(appverseBuilderCliInfoResource, "appverseBuilderProperties", appverseBuilderProperties);
        this.restMockMvc = MockMvcBuilders.standaloneSetup(appverseBuilderCliInfoResource)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    @Test
    public void getCLIDetails() throws Exception {
        restMockMvc.perform(get("/api/cli/details"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath(AppverseBuilderCliInfoResource.REGISTRY).value(equalTo(appverseBuilderProperties.getCli().getRegistry())))
            .andExpect(jsonPath(AppverseBuilderCliInfoResource.COMMAND_NAME).value(equalTo(appverseBuilderProperties.getCli().getCommandName())))
            .andExpect(jsonPath(AppverseBuilderCliInfoResource.PACKAGE_NAME).value(equalTo(appverseBuilderProperties.getCli().getPackageName())));

    }
}
