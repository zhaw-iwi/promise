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

    public String start() {
        return this.start(null);
    }

    public String start(String outerPrompt) {
        String totalPrompt = (this.composeTotalPrompt(outerPrompt).isEmpty() ? null
                : this.composeTotalPrompt(outerPrompt));

        String assistantSays = this.innerCurrent.start(totalPrompt);
        this.utterances.appendAssistantSays(assistantSays);
        return assistantSays;
    }

    public String respond(String userSays) throws TransitionException {
        return this.respond(userSays, null);
    }

    public String respond(String userSays, String outerPrompt) throws TransitionException {
        this.utterances.appendUserSays(userSays);
        this.raiseIfTransit();
        String totalPrompt = this.composeTotalPrompt(outerPrompt);
        String assistantSays = null;
        try {
            assistantSays = this.innerCurrent.respond(userSays, totalPrompt);
            this.utterances.appendAssistantSays(assistantSays);
            return assistantSays;
        } catch (TransitionException e) {
            this.innerCurrent = e.getSubsequentState();
            if (this.innerCurrent.isStarting()) {
                assistantSays = this.innerCurrent.start(totalPrompt);
            } else {
                assistantSays = this.innerCurrent.respond(userSays, totalPrompt);
            }
            this.utterances.appendAssistantSays(assistantSays);
            return assistantSays;
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
