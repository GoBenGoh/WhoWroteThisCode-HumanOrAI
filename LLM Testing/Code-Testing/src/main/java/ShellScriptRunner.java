import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ShellScriptRunner {
    public static void main(String[] args) {
        // Hard Coded Student Repo
        String repositoryDirectory = "./repos_output/assignment-1-repository-12";
        TestResultAnalyzer testingResults = new TestResultAnalyzer(false, -1, -1, -1, -1, new ArrayList<>());

        String compileResponse = runCommand(repositoryDirectory, "COMPILE");
        boolean isBuildSucceeded = compileResponse.contains("BUILD SUCCESS");
        if (isBuildSucceeded) {
            // Running Provided Tests
            runCommand(repositoryDirectory, "CLEAR_TESTS");
            runCommand(repositoryDirectory, "ADD_PROVIDED_TESTS");
            String testResponse = runCommand(repositoryDirectory, "TEST");

            int totalProvided = TestResultAnalyzer.getValue(testResponse, "Tests run: ");
            int failures = TestResultAnalyzer.getValue(testResponse, "Failures: ");
            int errors = TestResultAnalyzer.getValue(testResponse, "Errors: ");
            int skipped = TestResultAnalyzer.getValue(testResponse, "Skipped: ");
            int numPassedProvidedTests = totalProvided - failures - errors - skipped;

            if (totalProvided == -1 || failures == -1 || errors == -1 || skipped == -1){
                System.out.println("Failed to get testing information from provided tests");
                testingResults = new TestResultAnalyzer(true, 0, 0, 0, 0, new ArrayList<>());
                return;
            }

            // Running Hidden Tests
            runCommand(repositoryDirectory, "CLEAR_TESTS");
            runCommand(repositoryDirectory, "ADD_HIDDEN_TESTS");
            testResponse = runCommand(repositoryDirectory, "TEST");
            List<String> failureMessages =  TestResultAnalyzer.getFailureMessages(testResponse);
            for (String testName : TestResultAnalyzer.extractTestNames(failureMessages)){
                System.out.println(testName);
            }


            int totalHidden = TestResultAnalyzer.getValue(testResponse, "Tests run: ");
            failures = TestResultAnalyzer.getValue(testResponse, "Failures: ");
            errors = TestResultAnalyzer.getValue(testResponse, "Errors: ");
            skipped = TestResultAnalyzer.getValue(testResponse, "Skipped: ");
            int numPassedHiddenTests = totalHidden - failures - errors - skipped;

            if (totalHidden == -1 || failures == -1 || errors == -1 || skipped == -1){
                System.out.println("Failed to get testing information from hidden tests");
                testingResults = new TestResultAnalyzer(true, numPassedProvidedTests, 0, totalProvided, 0, new ArrayList<>());
                return;
            }

//            System.out.println("Provided Tests: " + numPassedProvidedTests + "/" + totalProvided);
//            System.out.println("Hidden Tests: " + numPassedHiddenTests + "/" + totalHidden);
            testingResults = new TestResultAnalyzer(true, numPassedProvidedTests, numPassedHiddenTests, totalProvided, totalHidden, new ArrayList<>());

        } else {
            System.out.println("Build Failed");
            ArrayList<String> errorMessages = TestResultAnalyzer.getCompilationErrors(compileResponse);
            testingResults = new TestResultAnalyzer(false, -1, -1, -1, -1, errorMessages);
            ErrorFileWriter.writeErrorsToFile(errorMessages);
        }
    }

    private static String runCommand(String repositoryDirectory, String command) {
        try {
            String[] scriptCommand = {"C:\\Program Files\\Git\\bin\\bash.exe", "-c", "./Code-Testing/src/main/java/script.sh " + repositoryDirectory + " " + command};
            ProcessBuilder processBuilder = new ProcessBuilder(scriptCommand);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            if (command.equals("CLEAR_TESTS")) {
                return "";
            } else {
                return readOutput(process);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String readOutput(Process process) throws IOException {
        InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append(System.lineSeparator());
        }
        return output.toString();
    }
}

