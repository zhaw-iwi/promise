package ch.zhaw.statefulconversation.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import ch.zhaw.statefulconversation.model.Agent;

public interface AgentRepository extends JpaRepository<Agent, UUID> {

}
