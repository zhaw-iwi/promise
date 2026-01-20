package ch.zhaw.statefulconversation.model;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import ch.zhaw.statefulconversation.spi.GsonExclude;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Utterance {

    @Id
    @GeneratedValue
    @GsonExclude
    private UUID id;

    protected Utterance() {

    }

    private String role;
    @Column(length = 4096)
    private String content;

    @CreationTimestamp
    @Column(name = "createdDate", nullable = false, updatable = false)
    private Instant createdDate;
    @GsonExclude
    private String stateName;
    @ManyToOne
    @JoinColumn(name = "utterances_id")
    @GsonExclude
    private Utterances utterances;

    public Utterance(String role, String content, String stateName) {
        this.role = role;
        this.content = content;
        this.stateName = stateName;
    }

    public String getRole() {
        return this.role;
    }

    public String getContent() {
        return this.content;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public String getStateName() {
        return this.stateName;
    }

    public void setUtterances(Utterances utterances) {
        this.utterances = utterances;
    }

    @Override
    public String toString() {
        return "{role=\"" + role + "\", content=\"" + content + "\", stateName=\"" + stateName + "\"}";
    }
}
