/**
 * This is a class used by GSON. GSON converts this class into a JSON request to be sent to GPT.
 */
public class Request {
    public String model;
    public Message[] messages = new Message[1];
    public double temperature;
    public Request(String model, String prompt, double temperature){
        this.model = model;
        Message message = new Message("user", prompt);
        this.messages[0] = message;
        this.temperature = temperature;
    }
    public Request(String model, String prompt){
        this.model = model;
        Message message = new Message("user", prompt);
        this.messages[0] = message;
        this.temperature = 0.7;
    }
    class Message{
        public String role;
        public String content;
        Message(String role, String content){
            this.role=role;
            this.content=content;
        }
    }
}
