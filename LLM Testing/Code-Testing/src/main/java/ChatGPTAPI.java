import com.google.gson.*;
import org.checkerframework.checker.units.qual.C;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
public class ChatGPTAPI {
    public String content = "package nz.ac.auckland.se281;\n" +
            "\n" +
            "import nz.ac.auckland.se281.Main.PolicyType;\n" +
            "\n" +
            "public class InsuranceSystem {\n" +
            "    public InsuranceSystem() {\n" +
            "        // Only this constructor can be used (if you need to initialise fields).\n" +
            "    }\n" +
            "\n" +
            "    public void printDatabase() {\n" +
            "        // TODO: Complete this method.\n" +
            "    }\n" +
            "\n" +
            "    public void createNewProfile(String userName, String age) {\n" +
            "        // TODO: Complete this method.\n" +
            "    }\n" +
            "\n" +
            "    public void loadProfile(String userName) {\n" +
            "        // TODO: Complete this method.\n" +
            "    }\n" +
            "\n" +
            "    public void unloadProfile() {\n" +
            "        // TODO: Complete this method.\n" +
            "    }\n" +
            "\n" +
            "    public void deleteProfile(String userName) {\n" +
            "        // TODO: Complete this method.\n" +
            "    }\n" +
            "\n" +
            "    public void createPolicy(PolicyType type, String[] options) {\n" +
            "        // TODO: Complete this method.\n" +
            "    }\n" +
            "}\n";

    public void sendGPTRequest(String request, String key, boolean isNaturalLanguageRequest) {
        try {
            URL url = new URL("https://api.openai.com/v1/chat/completions");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            //Make sure you put the right Organization key saved earlier.
            con.setDoOutput(true);
            //Make sure you put the right API Key saved earlier.
            con.setRequestProperty("Authorization", "Bearer "+ key);
            //Make sure to REPLACE the path of the json file!
            String jsonInputString = readLinesAsString(new File(request));
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            int responseCode = con.getResponseCode();
            System.out.println("Response Code : " + responseCode);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            String content = getResponseContent(response);
            System.out.println(content);
            if (isNaturalLanguageRequest){
                createResponseTxtFiles("src/main/resources/NaturalLanguageResponse.txt", response, "src/main/resources/NaturalLanguageContent.txt", content);
                this.content=content;
            }
            else{
                createResponseTxtFiles("src/main/resources/response.txt", response, "src/main/resources/content.txt", content);
                this.content=content;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void createResponseTxtFiles(String fileName, StringBuffer response, String fileName1, String content) throws FileNotFoundException {
        try (PrintWriter out = new PrintWriter(fileName)) {
            out.println(response);
        }
        try (PrintWriter out = new PrintWriter(fileName1)) {
            out.println(content);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static String getResponseContent(StringBuffer response) {
        JsonObject jsonObject = JsonParser.parseString(String.valueOf(response)).getAsJsonObject();
        JsonElement choices =  jsonObject.get("choices");
        JsonElement message = choices.getAsJsonArray().get(0).getAsJsonObject().get("message");
        JsonElement content = message.getAsJsonObject().get("content");
        return content.getAsString();
    }

    public void runTestIterations(String[] args, String repo, String commit, String workflow) throws IOException, InterruptedException {
        ChatGPTAPI app = new ChatGPTAPI();
        boolean task1 = true;
        boolean isStart = true;

        String temperature = String.valueOf(args[1]);
        CSVCreator CSVCreator = new CSVCreator(repo, commit, workflow, temperature);
        CSVCreator.createRepoHeader();
        TestResultAnalyzer resultAnalyzer = new TestResultAnalyzer(false, new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), "");

        for(int i = 0; i < 2; i++){
            int attempt = i + 1;
            System.out.println(attempt);

            if (isStart){
                resultAnalyzer = startTesting(app, "1", args, repo, attempt, CSVCreator, resultAnalyzer);
                isStart = false;
            }
            else {
                String errors = resultAnalyzer.getErrors();
                String task1Failures = resultAnalyzer.getT1Failures();
                String task2Failures = resultAnalyzer.getT2Failures();
                if(errors != ""){
                    System.out.println("Compilation errors: \n");
                    System.out.println(errors);
                    if (task1)
                        resultAnalyzer = startTesting(app, "1c", args, repo, attempt, CSVCreator, resultAnalyzer);
                    else
                        resultAnalyzer = startTesting(app, "2c", args, repo, attempt, CSVCreator, resultAnalyzer);
                }
                else{
                    System.out.println("No compilation errors");
                    if (!task1Failures.equals("")) {
                        System.out.println("Task 1 test failures: \n");
                        System.out.println(task1Failures);
                        resultAnalyzer = startTesting(app, "1f", args, repo, attempt, CSVCreator, resultAnalyzer);
                    }
                    else if (task1Failures.equals("") && !task2Failures.equals("")) {
                        System.out.println("Task 2 test failures: \n");
                        System.out.println(task2Failures);
                        task1 = false;
                        resultAnalyzer = startTesting(app, "2f", args, repo, attempt, CSVCreator, resultAnalyzer);
                    }
                    else{
                        // All tests pass
                        System.out.println("All tests passed");
                        CSVCreator.save();
                        return;
                    }
                }
            }
        }
        System.out.println("10 iterations reached");
        CSVCreator.save();
    }

    private TestResultAnalyzer startTesting(ChatGPTAPI app, String mode, String[] args, String repo, int attempt,
                                                   CSVCreator CSVCreator, TestResultAnalyzer priorResults) throws IOException, InterruptedException {
        String responseContent = this.content;
        System.out.println("Response content: "+"\n"+responseContent);
        String jsonRequest = setJsonRequest(mode);
        boolean isNaturalLanguage = false;
        if (args[2] == "natural"){
            isNaturalLanguage = true;
        }
        PromptWriter promptWriter = setPromptWriter(mode, app, responseContent, isNaturalLanguage, priorResults);

        String newPrompt = promptWriter.output(); // new prompt in string form
        Request newRequest;
        if (args[1] == null){ //default 0.7 temperature
            newRequest = new Request("gpt-3.5-turbo-16k", newPrompt, 0.7); // object for gson to convert
        }
        else{
            newRequest = new Request("gpt-3.5-turbo-16k", newPrompt, Double.valueOf(args[1])); // object for gson to convert
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create(); // format JSON
        JsonElement jsonElement = gson.toJsonTree(newRequest);
        String jsonString = gson.toJson(jsonElement);
        try(PrintWriter out = new PrintWriter(jsonRequest)){
            out.println(jsonString);
        }
        try(PrintWriter out = new PrintWriter("src/main/resources/newPrompt.txt")){
            out.println(newPrompt);
        }
        sendGPTRequest(jsonRequest, args[0], false);
        try {
            TextToJava.convertTextToJavaFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        TestResultAnalyzer testingResults = ShellScriptRunner.runTesting(repo);
        if(args.length == 3){
            if(!testingResults.isCompiled() && args[2].equals("natural")){
                writeNaturalLanguageJson(testingResults.getErrors(), app);
                System.out.println("Sending compilation error request");
                sendGPTRequest(jsonRequest, args[0], true);
            }
            else{
                System.out.println("Not sending natural language request");
            }
        }

        CSVCreator.addAttemptInfo(attempt, testingResults);
        return testingResults;
    }

    public void writeNaturalLanguageJson(String errorMessages, ChatGPTAPI app) throws IOException {
        String promptTemplate = app.getFileFromResource("Prompt Templates/NaturalLanguageError.txt");
        String responseContent = this.content;
        String newPrompt = new PromptWriter(promptTemplate, responseContent, errorMessages, "naturalLanguage")
                .createNaturalLanguageErrorPrompt();

        String jsonRequest = "src/main/java/NaturalLanguageRequest.json";
        Request newRequest = new Request("gpt-3.5-turbo", newPrompt); // object for gson to convert
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        JsonElement jsonElement = gson.toJsonTree(newRequest);
        String jsonString = gson.toJson(jsonElement);
        try(PrintWriter out = new PrintWriter(jsonRequest)){
            out.println(jsonString);
        }
        try(PrintWriter out = new PrintWriter("src/main/resources/newNaturalLanguageErrorPrompt.txt")){
            out.println(newPrompt);
        }
    }

    private static PromptWriter setPromptWriter(String mode, ChatGPTAPI app, String responseContent, boolean isNaturalLanguage, TestResultAnalyzer results) throws IOException {
        String promptTemplate;
        String error;
        String failure;
        if (mode.equals("1")){
            promptTemplate = app.getFileFromResource("Prompt Templates/Task1.txt");
            return new PromptWriter(promptTemplate, responseContent, "t1");
        }
        else if (mode.equals("1c")){
            promptTemplate = app.getFileFromResource("Prompt Templates/Task1_CompilationError.txt");
            if (isNaturalLanguage){
                error = app.getFileFromResource("NaturalLanguageContent.txt");
            }
            else{
                error = results.getErrors();
            }
            return new PromptWriter(promptTemplate, responseContent, error, "c");
        }
        else if (mode.equals("1f")){
            promptTemplate = app.getFileFromResource("Prompt Templates/Task1_FailedTests.txt");
//            failure = app.getFileFromResource("provided_failures.txt");
            failure = results.getT1Failures();
            return new PromptWriter(promptTemplate, responseContent, failure, "f");
        }
        else if (mode.equals("2")){
            promptTemplate = app.getFileFromResource("Prompt Templates/Task2.txt");
            return new PromptWriter(promptTemplate, responseContent, "t2");
        }
        else if (mode.equals("2c")){
            promptTemplate = app.getFileFromResource("Prompt Templates/Task2_CompilationError.txt");
            if (isNaturalLanguage){
                error = app.getFileFromResource("NaturalLanguageContent.txt");
            }
            else{
                error = results.getErrors();
            }
            return new PromptWriter(promptTemplate, responseContent, error, "c");
        }
        else if (mode.equals("2f")){
            promptTemplate = app.getFileFromResource("Prompt Templates/Task2_FailedTests.txt");
//            failure = app.getFileFromResource("provided_failures.txt");
            failure = results.getT2Failures();
            return new PromptWriter(promptTemplate, responseContent, failure, "f");
        }
        else {
            throw new RuntimeException("The prompt argument is invalid.");
        }
    }

    private static String setJsonRequest(String mode){
        if (mode.equals("1")){
            return "src/main/java/InitialTask1Request.json";
        }
        else if (mode.equals("1c")){
            return "src/main/java/Task1CompilationFailureRequest.json";
        }
        else if (mode.equals("1f")){
            return "src/main/java/Task1FailedTestsRequest.json";
        }
        else if (mode.equals("2")){
            return  "src/main/java/InitialTask2Request.json";
        }
        else if (mode.equals("2c")){
            return "src/main/java/Task2CompilationFailureRequest.json";
        }
        else if (mode.equals("2f")){
            return  "src/main/java/Task2FailedTestsRequest.json";
        }
        else {
            throw new RuntimeException("The prompt argument is invalid.");
        }
    }

    public String getFileFromResource(String fileName) throws IOException {

        // The class loader that loaded the class
        ClassLoader classLoader = getClass().getClassLoader();
        String inputStream = new String(classLoader.getResourceAsStream(fileName).readAllBytes(), StandardCharsets.UTF_8);

        // the stream holding the file content
        if (inputStream == null) {
            throw new IllegalArgumentException("file not found! " + fileName);
        } else {
            return inputStream;
        }

    }
    public static String readLinesAsString(File file) {
        List<String> returnLines = new LinkedList<String>();
        String text = "";
        try {
            text = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text;
    }
}