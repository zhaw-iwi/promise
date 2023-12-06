package ch.zhaw.statefulconversation.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ch.zhaw.statefulconversation.model.Utterances;

public interface UtterancesRepository extends JpaRepository<Utterances, UUID> {

}
