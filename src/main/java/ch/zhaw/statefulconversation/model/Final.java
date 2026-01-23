package ch.zhaw.statefulconversation.model;

import java.util.List;

import jakarta.persistence.Entity;

@Entity
public class Final extends State {

    protected Final() {

    }

    private static final String FINAL_PROMPT = """
            This is the final state and the conversation is complete.
            If the user sends further messages, do not restart or continue the interaction, ask no questions, and introduce no new topics.
            Briefly acknowledge the message, state that the conversation has ended, and note that a new session is required to continue.
            Keep responses short and warm.
            """;;
    private static final String FINAL_STARTER_PROMPT = "Give a very brief, courteous goodbye to end on a positive and respectful note.";

    public Final(String name) {
        super(Final.FINAL_PROMPT, name, Final.FINAL_STARTER_PROMPT, List.of());
    }

    public Final(String name, boolean isStarting, String summarisePrompt) {
        super(Final.FINAL_PROMPT, name, Final.FINAL_STARTER_PROMPT, List.of(), summarisePrompt, isStarting, false);
    }

    public Final(String name, String prompt) {
        super(prompt, name, Final.FINAL_STARTER_PROMPT, List.of());
    }

    public Final(String name, String prompt, boolean isStarting, String summarisePrompt) {
        super(prompt, name, Final.FINAL_STARTER_PROMPT, List.of(), summarisePrompt, isStarting, false);
    }

    public Final(String name, String prompt, String starterPrompt) {
        super(prompt, name, starterPrompt, List.of());
    }

    public Final(String name, String prompt, String starterPrompt, boolean isStarting, String summarisePrompt) {
        super(prompt, name, starterPrompt, List.of(), summarisePrompt, isStarting, false);
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
