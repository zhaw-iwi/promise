package ch.zhaw.statefulconversation.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.zhaw.statefulconversation.spi.LMOpenAI;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderColumn;

@Entity
public class State extends Prompt {
    private static final Logger LOGGER = LoggerFactory.getLogger(State.class);
    protected static final String SUMMARISE_PROMPT = "Please summarise the following conversation. Be concise, but ensure that the key points and issues are included. ";

    protected State() {

    }

    private String name;
    @Column(length = 3000)
    private String starterPrompt;
    @Column(length = 3000)
    private String summarisePrompt;
    private boolean isStarting;
    private boolean isOblivious;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderColumn(name = "transition_index")
    private List<Transition> transitions;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    protected Utterances utterances;

    public State(String prompt, String name, String starterPrompt, List<Transition> transitions) {
        super(prompt);
        this.name = name;
        this.starterPrompt = starterPrompt;
        this.transitions = new ArrayList<Transition>(transitions);
        this.summarisePrompt = State.SUMMARISE_PROMPT;
        this.isStarting = true;
        this.isOblivious = false;
        this.utterances = new Utterances();
    }

    public State(String prompt, String name, String starterPrompt, List<Transition> transitions, String summarisePrompt,
            boolean isStarting,
            boolean isOblivious) {
        this(prompt, name, starterPrompt, transitions);
        this.summarisePrompt = summarisePrompt;
        this.isStarting = isStarting;
        this.isOblivious = isOblivious;
    }

    public State(String prompt, String name, String starterPrompt, List<Transition> transitions, Storage storage,
            List<String> storageKeysFrom) {
        super(prompt, storage, storageKeysFrom);
        this.name = name;
        this.starterPrompt = starterPrompt;
        this.transitions = new ArrayList<Transition>(transitions);
        this.summarisePrompt = SUMMARISE_PROMPT;
        this.isStarting = true;
        this.isOblivious = false;
        this.utterances = new Utterances();
    }

    public State(String prompt, String name, String starterPrompt, List<Transition> transitions, String summarisePrompt,
            boolean isStarting,
            boolean isOblivious,
            Storage storage, List<String> storageKeysFrom) {
        this(prompt, name, starterPrompt, transitions, storage, storageKeysFrom);
        this.summarisePrompt = summarisePrompt;
        this.isStarting = isStarting;
        this.isOblivious = isOblivious;
    }

    public String getName() {
        return this.name;
    }

    public boolean isStarting() {
        return this.isStarting;
    }

    public Utterances getUtterances() {
        return this.utterances;
    }

    public boolean isActive() {
        return true;
    }

    public void addTransition(Transition transition) {
        this.transitions.add(transition);
    }

    protected List<Transition> getTransitions() {
        return List.copyOf(this.transitions);
    }

    protected void collectStates(Set<State> visited, List<State> result) {
        if (visited.contains(this)) {
            return;
        }
        visited.add(this);
        result.add(this);
        for (Transition current : this.transitions) {
            current.getSubsequentState().collectStates(visited, result);
        }
    }

    protected void raiseIfTransit() throws TransitionException {
        State subsequentState = this.transit();
        if (subsequentState != null) {
            throw new TransitionException(subsequentState);
        }
    }

    private State transit() {
        for (Transition current : this.transitions) {
            if (this.transitThisOne(current)) {
                return current.getSubsequentState();
            }
        }
        return null;
    }

    private boolean transitThisOne(Transition transition) {
        if (transition.decide(this.utterances)) {
            State.LOGGER.info(this.getName() + ": Transition to "
                    + transition.getSubsequentState().getName() + ": YES");
            transition.action(this.utterances);
            return true;
        }
        State.LOGGER.info(this.getName() + ": Transition to "
                + transition.getSubsequentState().getName() + ": NO");
        return false;
    }

    public Response start() {
        return this.start(null);
    }

    public Response start(String outerPrompt) {
        this.enter();
        String totalPromptPrepend = this.composeTotalPrompt(outerPrompt);
        // @todo: is it ok to avoid completion if there's no prompt?
        if (totalPromptPrepend.isEmpty()) {
            return null;
        }
        String assistantSays = LMOpenAI.complete(this.utterances, totalPromptPrepend, this.starterPrompt, this.name);
        this.utterances.appendAssistantSays(assistantSays, this);
        return new Response(this, assistantSays);
    }

    public Response respond(String userSays) throws TransitionException {
        return this.respond(userSays, null);
    }

    public Response respond(String userSays, String outerPrompt) throws TransitionException {
        this.acknowledge(userSays, outerPrompt);
        // no transition, compose prompt
        String totalPrompt = this.composeTotalPrompt(outerPrompt);
        // @todo: is it ok to avoid completion if there's no prompt?
        if (totalPrompt.isEmpty()) {
            return null;
        }
        String assistantSays = LMOpenAI.complete(this.utterances, totalPrompt, this.name);
        this.utterances.appendAssistantSays(assistantSays, this);
        return new Response(this, assistantSays);
    }

    public void enter() {
        State.LOGGER
                .info(this.getName() + " Starting");
        if (this.isOblivious) {
            State.LOGGER
                    .info(this.getName() + " Oblivious");
            this.utterances.reset();
        }
    }

    public void acknowledge(String userSays) throws TransitionException {
        this.acknowledge(userSays, null);
    }

    public void acknowledge(String userSays, String outerPrompt) throws TransitionException {
        State.LOGGER
                .info(this.getName() + " ACK User: \"" + userSays + "\"");
        this.utterances.appendUserSays(userSays, this);
        this.raiseIfTransit();
    }

    public void appendAssistantSays(String assistantSays) {
        State.LOGGER
                .info(this.getName() + " ACK Assistant: \"" + assistantSays + "\"");
        this.utterances.appendAssistantSays(assistantSays, this);
    }

    public String getTotalPrompt() {
        return this.getTotalPrompt(null);
    }

    public String getTotalPrompt(String outerPrompt) {
        String totalPrompt = this.composeTotalPrompt(outerPrompt);
        if (this.isStarting && this.starterPrompt != null && !this.starterPrompt.isEmpty()) {
            if (!totalPrompt.isEmpty()) {
                totalPrompt = totalPrompt + " ";
            }
            totalPrompt = totalPrompt + this.starterPrompt;
        }
        return totalPrompt;
    }

    public PromptResult getPromptBundle() {
        return this.getPromptBundle(null);
    }

    public PromptResult getPromptBundle(String outerPrompt) {
        String totalPrompt = this.getTotalPrompt(outerPrompt);
        List<Utterance> conversation = this.utterances.toList();
        return new PromptResult(this, totalPrompt, conversation);
    }

    protected String composeTotalPrompt(String outerPrompt) {
        String totalPrompt = (this.getPrompt() != null ? this.getPrompt() : "");
        if (outerPrompt != null) {
            totalPrompt = outerPrompt + " " + totalPrompt;
        }
        return totalPrompt.trim();
    }

    public String summarise() {
        String result = LMOpenAI.summariseOffline(this.utterances, this.summarisePrompt);
        return result;
    }

    public void reset() {
        this.reset(new HashSet<State>());
    }

    protected void reset(Set<State> statesAlreadyReseted) {
        if (statesAlreadyReseted.contains(this)) {
            return;
        }
        this.utterances.reset();
        statesAlreadyReseted.add(this);
        for (Transition current : this.transitions) {
            current.getSubsequentState().reset(statesAlreadyReseted);
        }
    }

    @Override
    public String toString() {
        return "State IS-A " + super.toString() + " with name " + this.getName();
    }

}
