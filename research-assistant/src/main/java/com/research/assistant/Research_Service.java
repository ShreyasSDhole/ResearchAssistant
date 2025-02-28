package com.research.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
public class Research_Service 
{
    @Value("${gemini.api.url}")
    private String geminiApiUrl;
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public Research_Service(WebClient.Builder webClientBuilder, ObjectMapper objectMapper)
    {
        this.webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    public String processContent(Research_Request request)
    {
        // Build the prompt
        String prompt =  buildPrompt(request);

        // Query the AI Model API
        Map<String, Object> requestBody = Map.of("contents", new Object[]{
                    Map.of("parts", new Object[]{
                            Map.of("text", prompt)
                    })
              }
        );

        String response = webClient.post().uri(geminiApiUrl + geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        // Parse the response
        // Return response
        return extractTextFromResponse(response);
    }

    private String extractTextFromResponse(String response)
    {
        try
        {
            Gemini_Response geminiResponse = objectMapper.readValue(response, Gemini_Response.class);
            if(geminiResponse.getCandidates()!=null
                    && !geminiResponse.getCandidates().isEmpty())
            {
                Gemini_Response.Candidate firstCandidate = geminiResponse.getCandidates().get(0);
                if(firstCandidate.getContent()!=null
                    && firstCandidate.getContent().getParts()!=null
                    && !firstCandidate.getContent().getParts().isEmpty())
                {
                    return firstCandidate.getContent().getParts().get(0).getText();
                }
            }
            return "No Content found in Response!";
        }
        catch (Exception e)
        {
            return "Error Parsing: "+ e.getMessage();
        }
    }

    private String buildPrompt(Research_Request request)
    {
        StringBuilder prompt = new StringBuilder();
        switch(request.getOperation())
        {
            case "summarize":
                prompt.append("Provide a clear and concise summary of the following text in a few sentences:\n\n");
                break;
            case "suggest":
                prompt.append("Based on the following content, suggest related topics and further reading. format the response with clear headings and bullet points:\n\n ");
                break;
            default:
                throw new IllegalArgumentException("Unknown Operation: " + request.getOperation());
        }
        prompt.append(request.getContent());
        return prompt.toString();
    }

}
