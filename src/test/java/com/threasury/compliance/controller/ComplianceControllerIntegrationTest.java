package com.threasury.compliance.controller;

import com.threasury.compliance.support.MockChatModelSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ComplianceControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatModel chatModel;

    private MockMultipartFile imagePart;
    private MockMultipartFile formDataPart;

    @BeforeEach
    void setUp() {
        imagePart = new MockMultipartFile(
                "image",
                "label.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                minimalJpeg()
        );
        formDataPart = new MockMultipartFile(
                "formData",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                        {
                          "brandName": "Stone's Throw",
                          "alcoholPercentage": "12.5",
                          "productType": "Wine"
                        }
                        """.getBytes()
        );
    }

    @Test
    void verifySingle_returnsCompliantResult_whenRulesPass() throws Exception {
        MockChatModelSupport.stubExtraction(chatModel, MockChatModelSupport.compliantExtractionJson());

        mockMvc.perform(multipart("/api/v1/compliance/verify")
                        .file(imagePart)
                        .file(formDataPart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName", is("label.jpg")))
                .andExpect(jsonPath("$.compliant", is(true)))
                .andExpect(jsonPath("$.extractedData.brandName", is("STONE'S THROW")))
                .andExpect(jsonPath("$.ruleResults", hasSize(4)))
                .andExpect(jsonPath("$.ruleResults[0].ruleName", is("Brand Name")))
                .andExpect(jsonPath("$.ruleResults[0].passed", is(true)))
                .andExpect(jsonPath("$.ruleResults[3].ruleName", is("Government Health Warning")))
                .andExpect(jsonPath("$.ruleResults[3].passed", is(true)));
    }

    @Test
    void verifySingle_returnsNonCompliantResult_whenRulesFail() throws Exception {
        MockChatModelSupport.stubExtraction(chatModel, MockChatModelSupport.nonCompliantExtractionJson());

        mockMvc.perform(multipart("/api/v1/compliance/verify")
                        .file(imagePart)
                        .file(formDataPart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compliant", is(false)))
                .andExpect(jsonPath("$.ruleResults[0].passed", is(false)))
                .andExpect(jsonPath("$.ruleResults[3].passed", is(false)));
    }

    @Test
    void verifyBatch_processesMultipleFilesConcurrently() throws Exception {
        MockChatModelSupport.stubExtraction(chatModel, MockChatModelSupport.compliantExtractionJson());

        MockMultipartFile imageTwo = new MockMultipartFile(
                "images",
                "label-two.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47}
        );
        MockMultipartFile imageOne = new MockMultipartFile(
                "images",
                "label-one.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                minimalJpeg()
        );

        mockMvc.perform(multipart("/api/v1/compliance/verify/batch")
                        .file(imageOne)
                        .file(imageTwo)
                        .file(formDataPart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalFiles", is(2)))
                .andExpect(jsonPath("$.compliantCount", is(2)))
                .andExpect(jsonPath("$.results", hasSize(2)));
    }

    @Test
    void verifySingle_acceptsFormDataWithoutJsonContentType() throws Exception {
        MockChatModelSupport.stubExtraction(chatModel, MockChatModelSupport.compliantExtractionJson());

        MockMultipartFile plainFormData = new MockMultipartFile(
                "formData",
                "form-data.json",
                MediaType.TEXT_PLAIN_VALUE,
                """
                        {
                          "brandName": "Stone's Throw",
                          "alcoholPercentage": "12.5",
                          "productType": "Wine"
                        }
                        """.getBytes()
        );

        mockMvc.perform(multipart("/api/v1/compliance/verify")
                        .file(imagePart)
                        .file(plainFormData))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compliant", is(true)));
    }

    @Test
    void verifySingle_acceptsIndividualFormFields() throws Exception {
        MockChatModelSupport.stubExtraction(chatModel, MockChatModelSupport.compliantExtractionJson());

        mockMvc.perform(multipart("/api/v1/compliance/verify")
                        .file(imagePart)
                        .param("brandName", "Stone's Throw")
                        .param("alcoholPercentage", "12.5")
                        .param("productType", "Wine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.compliant", is(true)));
    }

    @Test
    void verifySingle_returnsBadRequest_whenImageMissing() throws Exception {
        mockMvc.perform(multipart("/api/v1/compliance/verify")
                        .file(formDataPart)
                        .param("brandName", "Stone's Throw")
                        .param("alcoholPercentage", "12.5")
                        .param("productType", "Wine"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void verifySingle_returnsBadRequest_whenFormDataInvalid() throws Exception {
        MockMultipartFile invalidForm = new MockMultipartFile(
                "formData",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"brandName\":\"\",\"alcoholPercentage\":\"12.5\",\"productType\":\"Wine\"}".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/compliance/verify")
                        .file(imagePart)
                        .file(invalidForm))
                .andExpect(status().isBadRequest());
    }

    private static byte[] minimalJpeg() {
        return new byte[]{
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xD9
        };
    }
}
