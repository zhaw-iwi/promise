package ch.zhaw.statefulconversation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;

@Entity
public class Utterances {

    private static final String ASSISTANT = "assistant";
    private static final String USER = "user";

    @Id
    @GeneratedValue
    private UUID id;

    public UUID getID() {
        return this.id;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @OrderColumn(name = "utterance_index")
    private List<Utterance> utteranceList;

    public Utterances() {
        this.utteranceList = new ArrayList<Utterance>();
    }

    public void appendAssistantSays(String assistantSays) {
        this.utteranceList.add(new Utterance(Utterances.ASSISTANT, assistantSays));
    }

    public void appendUserSays(String userSays) {
        this.utteranceList.add(new Utterance(Utterances.USER, userSays));
    }

    /*
     * This one is assuming the last utterance is from the user
     * (used in RemoveLastUtteranceAction)
     */
    public void removeLastUtterance() {
        if (!this.utteranceList.getLast().getRole().equals(Utterances.USER)) {
            throw new RuntimeException("assumption that last utterance has role == user failed");
        }
        this.utteranceList.removeLast();
    }

    public String removeLastTwoUtterances() {
        if (!this.utteranceList.getLast().getRole().equals(Utterances.ASSISTANT)) {
            throw new RuntimeException("assumption that last utterance has role == assistant failed");
        }
        this.utteranceList.removeLast();

        // the following loop is to accomodate the possibility that the assistant had
        // multiple responses in a row (cf. HTML reponses)
        while (this.utteranceList.getLast().getRole().equals(Utterances.ASSISTANT)) {
            this.utteranceList.removeLast();
        }

        if (!this.utteranceList.getLast().getRole().equals(Utterances.USER)) {
            throw new RuntimeException(
                    "assumption that when removing all assistant utterances only user utterance remains failed");
        }

        Utterance lastUserUtterance = this.utteranceList.getLast();
        return lastUserUtterance.getContent();
    }

    public boolean isEmpty() {
        return this.utteranceList.isEmpty();
    }

    public void reset() {
        this.utteranceList.clear();
    }

    public List<Utterance> toList() {
        List<Utterance> result = List.copyOf(this.utteranceList);
        return result;
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer("");
        for (Utterance current : this.utteranceList) {
            result.append(current.getRole() + ": " + current.getContent() + "\n");
        }
        return result.toString();
    }
}
