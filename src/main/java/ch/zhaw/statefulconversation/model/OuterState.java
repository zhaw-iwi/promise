package ch.zhaw.statefulconversation.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;

@Entity
public class OuterState extends State {

    protected OuterState() {

    }

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private State innerInitial;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private State innerCurrent;

    public OuterState(String prompt, String name, List<Transition> transitions, State innerInitial) {
        super(prompt, name, null, transitions);
        this.innerInitial = innerInitial;
        this.innerCurrent = this.innerInitial;
    }

    public OuterState(String prompt, String name, List<Transition> transitions, State innerInitial,
            String summarisePrompt) {
        super(prompt, name, null, transitions, summarisePrompt, true, false);
        this.innerInitial = innerInitial;
        this.innerCurrent = this.innerInitial;
    }

    @Override
    public boolean isActive() {
        return this.innerCurrent.isActive();
    }

    public Response start() {
        return this.start(null);
    }

    public Response start(String outerPrompt) {
        String totalPrompt = (this.composeTotalPrompt(outerPrompt).isEmpty() ? null
                : this.composeTotalPrompt(outerPrompt));

        Response assistantResponse = this.innerCurrent.start(totalPrompt);
        this.utterances.appendAssistantSays(assistantResponse.getText(), this);
        return assistantResponse;
    }

    public Response respond(String userSays) throws TransitionException {
        return this.respond(userSays, null);
    }

    public Response respond(String userSays, String outerPrompt) throws TransitionException {
        this.utterances.appendUserSays(userSays, this);
        this.raiseIfTransit();
        String totalPrompt = this.composeTotalPrompt(outerPrompt);
        Response assistantResponse = null;
        try {
            assistantResponse = this.innerCurrent.respond(userSays, totalPrompt);
            this.utterances.appendAssistantSays(assistantResponse.getText(), this);
            return assistantResponse;
        } catch (TransitionException e) {
            this.innerCurrent = e.getSubsequentState();
            if (this.innerCurrent.isStarting()) {
                assistantResponse = this.innerCurrent.start(totalPrompt);
            } else {
                assistantResponse = this.innerCurrent.respond(userSays, totalPrompt);
            }
            this.utterances.appendAssistantSays(assistantResponse.getText(), this);
            return assistantResponse;
        }
    }

    @Override
    public void reset() {
        this.reset(new HashSet<State>());
    }

    @Override
    protected void reset(Set<State> statesAlreadyReseted) {
        if (statesAlreadyReseted.contains(this)) {
            return;
        }
        super.reset(statesAlreadyReseted);
        this.innerCurrent = this.innerInitial;
        this.innerCurrent.reset(statesAlreadyReseted);
    }

    @Override
    public String toString() {
        return "OuterState IS-A " + super.toString() + " with inner initial " + this.innerInitial
                + " and inner current " + this.innerCurrent;
    }

}
