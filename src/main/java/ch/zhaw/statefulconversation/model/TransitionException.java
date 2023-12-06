package ch.zhaw.statefulconversation.model;

public class TransitionException extends Exception {

    private State subsequentState;

    public TransitionException(State subsequentState) {
        this.subsequentState = subsequentState;
    }

    public State getSubsequentState() {
        return this.subsequentState;
    }
}
