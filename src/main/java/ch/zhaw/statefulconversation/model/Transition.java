package ch.zhaw.statefulconversation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.zhaw.statefulconversation.spi.LMOpenAI;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;

@Entity
public class Transition {
    private static final Logger LOGGER = LoggerFactory.getLogger(Transition.class);

    @Id
    @GeneratedValue
    private UUID id;

    protected Transition() {

    }

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderColumn(name = "decision_index")
    private List<Decision> decisions;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderColumn(name = "action_index")
    private List<Action> actions;
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private State subsequentState;

    public Transition(List<Decision> decisions, List<Action> actions, State subsequentState) {
        this.decisions = new ArrayList<Decision>(decisions);
        this.actions = new ArrayList<Action>(actions);
        this.subsequentState = subsequentState;
    }

    public Transition(Decision decision, Action action, State subsequentState) {
        this(List.of(decision), List.of(action), subsequentState);
    }

    public Transition(Decision decision, State subsequentState) {
        this(List.of(decision), List.of(), subsequentState);
    }

    public Transition(Action action, State subsequentState) {
        this(List.of(), List.of(action), subsequentState);
    }

    public Transition(State subsequentState) {
        this(List.of(), List.of(), subsequentState);
    }

    public State getSubsequentState() {
        return this.subsequentState;
    }

    public void addDecision(Decision decision) {
        this.decisions.add(decision);
    }

    public void addAction(Action action) {
        this.actions.add(action);
    }

    public void setSubsequenState(State subsequentState) {
        this.subsequentState = subsequentState;
    }

    public boolean decide(Utterances utterances) {
        Transition.LOGGER.warn("decisions if transition to " + this.subsequentState.getName());
        if (this.decisions.isEmpty()) {
            Transition.LOGGER.warn("no decisions present");
            return true;
        }
        String currentDecisionPrompt;
        boolean currentDecision;
        for (Decision current : this.decisions) {
            currentDecisionPrompt = current.getPrompt();
            currentDecision = LMOpenAI.decide(utterances, currentDecisionPrompt);
            if (!currentDecision) {
                return false;
            }
        }
        return true;
    }

    public void action(Utterances utterances) {
        Transition.LOGGER.warn("actions while transitioning to " + this.subsequentState.getName());
        if (this.actions.isEmpty()) {
            Transition.LOGGER.warn("no action present");
            return;
        }
        for (Action current : this.actions) {
            current.execute(utterances);
        }
    }

    @Override
    public String toString() {
        return "Transition to " + this.subsequentState + " decided by " + this.decisions + " and affected by "
                + this.actions;
    }
}
