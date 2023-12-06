package ch.zhaw.statefulconversation.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ch.zhaw.statefulconversation.model.Utterance;

public interface UtteranceRepository extends JpaRepository<Utterance, UUID> {

}
