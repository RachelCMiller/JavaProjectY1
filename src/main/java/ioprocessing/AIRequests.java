package ioprocessing;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONObject;
import org.json.JSONArray;
import java.time.Duration;

public class AIRequests {
    private static final String apiKey = System.getenv("OPENROUTERAPIKEY");

    private static final String ai = "Gemini";
    private static final String aiModel = "google/gemini-2.0-flash-exp:free";

    //top-level function for extracting applicant details. Build the request before sending to AI
    public static String getAIResponse(String cvText, int applicantID) {
        String requestContent = "The following text is from a person's CV. Can you extract the following details and separate them with a comma " +
                "(remove any commas from the original text). If you can't find a detail for a category, mark it as n/a. Only answer with the extracted details following the format:\n" +
                "name,phone number,email address,linkedin profile,house address,rest of the CV information\nDo not include any other text at all.\nThe CV text is as follows:\n" + cvText;
        JSONArray messages = buildMessages(requestContent);
        return sendToAI(messages, "applicant " + applicantID + "'s details", 1000);
    }

    //top-level function for extracting job details. Build the request before sending to AI
    public static String getAIResponse(String jobText) {
        String requestContent = "The following text is from a job description. You need to extract the job title, educational requirements, experience requirements, and skills requirements " +
                "from the description as well as applying a number between 1 and 5 depending on how important the requirement is (e.g. essential being 4 or 5, while nice to have would be 1 or 2). " +
                "The requirements strings shouldn't have any commas and should be kept to under 20 words (requirements can be split into 2 parts that are under 20 words each). " +
                "Simply answer (i.e. no introduction or explanation of reasoning) following this example format for a Junior Software Engineer with 3 EDUCATION, 2 EXPERIENCE and 3 SKILLS requirements:\n" +
                "Junior Software Engineer^EDUCATION:first educational requirement,1,second educational requirement,4,third educational requirement,2,^EXPERIENCE:first experience requirement,2,second experience requirement,3," +
                "^SKILLS:first skill requirement,5,second skill requirement,3,third skill requirement,1,\nDo not include any other text at all.\n" +
                "The job text is:\n" + jobText;
        JSONArray messages = buildMessages(requestContent);
        return sendToAI(messages, "the job details", 500);
    }

    //top-level function for scoring applicant against job requirements. Build the request before sending to AI
    public static String getAIResponse(String jobInfo, String applicantInfo) {
        //"If fulfillment isn't clear and may only be implied, mark false rather than true" - it was initially overzealous with marking true for less experienced applicants
        String requestContent = "You will be given the EDUCATION, EXPERIENCE and SKILLS requirements for a job (in a comma separated list) and details of an applicant. " +
                "For each requirement, decide if the person fulfills it based on their details. " +
                "If they do mark \"true\" and if they don't mark \"false\". If fulfillment isn't clear and may only be implied, mark false rather than true. " +
                "If two requirements are similar only mark true for the more specific version (e.g. if 2 requirements are BSc Computer Science and any undergrad degree, " +
                "if a candidate has a computer science degree, only mark true for that and not both. " +
                "Simply answer (i.e. no introduction or explanation of reasoning) following this example format for 3 EDUCATION, 2 EXPERIENCE and 3 SKILLS requirements:\n" +
                "EDUCATION:true,true,false,^EXPERIENCE:true,false,^SKILLS:false,true,false,\nDo not include any other text at all.\n" +
                "The job requirements are:\n" + jobInfo + "\nThe applicant's details are:\n" + applicantInfo;
        JSONArray messages = buildMessages(requestContent);
        return sendToAI(messages, "the applicant job score", 100);
    }

    //function to build JSON messages
    public static JSONArray buildMessages(String requestContent) {
        //ref sameera sy (2020) 'Getting JSON data' [Stack Overflow] 8 June. Available at: https://stackoverflow.com/questions/62268311/java-get-json-data-from-url (Accessed: 6 April 2025)
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", requestContent);
        JSONArray messages = new JSONArray();
        messages.put(message);
        return messages;
    }

    //function to attempt to send the request to Gemini API through OpenRouter
    public static String sendToAI(JSONArray messages, String errorMessage, int maxTokens) {
        String answer;
        try {
            //ref OpenRouter (no date) 'Using the OpenRouter API directly'. Available at: https://openrouter.ai/docs/quickstart (Accessed: 6 April 2025)
            //ref Baeldung (2024) 'Exploring the New HTTP Client in Java'. Available at: https://www.baeldung.com/java-9-http-client (Accessed: 6 April 2025)
            HttpClient client = HttpClient.newHttpClient();
            JSONObject data = new JSONObject();
            data.put("model", aiModel);
            data.put("messages", messages);
            data.put("temperature", 0.0);
            data.put("max_tokens", maxTokens);
            if (apiKey == null) {
                System.out.println("No API key set.");
                return "";
            }
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(data.toString()))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject myObject = new JSONObject(response.body());
            if (myObject.has("error")) {
                int code = myObject.getJSONObject("error")
                        .getInt("code");
                String aiMessage;
                if (code == 429) {
                    aiMessage = "too many requests sent.";
                } else {
                    aiMessage = myObject.getJSONObject("error")
                            .getString("message");
                }
                System.out.println("Error " + code + " " + aiMessage);
                return "";
            }
            answer = myObject.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
            if (myObject.getJSONArray("choices")
                    .getJSONObject(0)
                    //ref BrantApps (2012) 'Use .has(String) to test if a JSONObject is null' [Stack Overflow] 25 September. Available at: https://stackoverflow.com/questions/12585492/how-to-test-if-a-jsonobject-is-null-or-doesnt-exist (Accessed: 21 April 2025)
                    .has("error")) {
                String aiMessage = myObject.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("error")
                        .getString("message");
                int code = myObject.getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("error")
                        .getInt("code");
                System.out.println("Error from " + AIRequests.getAI() + ": " + code + " " + aiMessage);
            }
            return answer;
        } catch (HttpConnectTimeoutException hct) {
            System.out.println("Request timed out.");
            return "";
        } catch (IOException | InterruptedException io) {
            System.out.println("Error processing " + errorMessage + ". Please make sure you have a stable internet connection for processing.");
            return "";
        } catch (Exception e) {
            System.out.println("Error processing request: " + e.getMessage());
            return "";
        }
    }

    public static String getAI() {
        return ai;
    }
}
