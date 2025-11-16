package ch.zhaw.statefulconversation.model.commons.actions;

import ch.zhaw.statefulconversation.model.Action;
import ch.zhaw.statefulconversation.model.State;
import ch.zhaw.statefulconversation.model.Utterances;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;

@Entity
public class TransferUtterancesAction extends Action {

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private State to;

    protected TransferUtterancesAction() {

    }

    public TransferUtterancesAction(State to) {
        super(null);
        this.to = to;
    }

    @Override
    public void execute(Utterances utterances) {
        this.to.getUtterances().append(utterances, this.to);
    }

    @Override
    public String toString() {
        return "TransferUtterancesAction IS-A " + super.toString();
    }
}
