package ch.zhaw.statefulconversation.model;

import java.util.List;

import jakarta.persistence.Entity;

@Entity
public class Final extends State {

    private static final String FINAL_PROMPT = "Provide a brief reply, no more than 12 tokens, acknowledging the user and leading to a goodbye.";
    private static final String FINAL_STARTER_PROMPT = "Give a very brief, courteous goodbye to end on a positive and respectful note.";

    public Final() {
        super(Final.FINAL_PROMPT, "FINAL", Final.FINAL_STARTER_PROMPT, List.of());
    }

    public Final(boolean isStarting, String summarisePrompt) {
        super(Final.FINAL_PROMPT, "FINAL", Final.FINAL_STARTER_PROMPT, List.of(), summarisePrompt, isStarting, false);
    }

    public Final(String prompt) {
        super(prompt, "FINAL", Final.FINAL_STARTER_PROMPT, List.of());
    }

    public Final(String prompt, boolean isStarting, String summarisePrompt) {
        super(prompt, "FINAL", Final.FINAL_STARTER_PROMPT, List.of(), summarisePrompt, isStarting, false);
    }

    public Final(String prompt, String starterPrompt) {
        super(prompt, "FINAL", starterPrompt, List.of());
    }

    public Final(String prompt, String starterPrompt, boolean isStarting, String summarisePrompt) {
        super(prompt, "FINAL", starterPrompt, List.of(), summarisePrompt, isStarting, false);
    }

    @Override
    public boolean isActive() {
        return false;
    }

    // @TODO for diagnostic purpose only, remove
    public String summarise() {
        throw new RuntimeException("we want to avoid summarise being invoked on final states");
    }

    @Override
    public String toString() {
        return "Final IS-A " + super.toString();
    }
}
