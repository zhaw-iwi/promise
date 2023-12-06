package ch.zhaw.statefulconversation.model.commons.actions;

import ch.zhaw.statefulconversation.model.Action;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Utterances;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Entity
public class RemoveLastUtteranceAction extends Action {

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private State removeFrom;

    protected RemoveLastUtteranceAction() {

    }

    public RemoveLastUtteranceAction(State removeFrom) {
        super(null); // @TODO: maybe redesign the inheritance hierarchy to avoid this?
        this.removeFrom = removeFrom;
    }

    @Override
    public void execute(Utterances utterances) {
        this.removeFrom.getUtterances().removeLastUtterance();
    }

    @Override
    public String toString() {
        return "StaticExtractionAction IS-A " + super.toString();
    }

}
