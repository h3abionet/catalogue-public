package bio.tech.ystr.persistence.dao;

import bio.tech.ystr.persistence.model.NeoProject;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProjectRepository extends PagingAndSortingRepository<NeoProject, Long> {

    NeoProject findNeoProjectByProjectId(@Param("projectId") String projectId);

    List<NeoProject> findAll();

    Long deleteByProjectId(@Param("projectId") String projectId);

    @Query( "MATCH (u:NeoUser {username: {username}}) " +
            "-[r:HAS_PROJECT]-> (p:NeoProject) " +
            "RETURN p")
    List<NeoProject> userProjects(@Param("username") String username);

    @Query( "MATCH (u:NeoUser {username: {username}}) " +
            "-[r:HAS_PROJECT]->(p:NeoProject {projectId: {projectId}}) " +
            "RETURN p")
    NeoProject projectByUsernameAndProjectId(@Param("username") String username,
                                             @Param("projectId") String projectId);

    @Query( "MATCH (p:NeoProject)-[]->(c:NeoCart) " +
            "WHERE ID(c) = {id} " +
            "RETURN p")
    NeoProject projectByCartLongId(@Param("id") Long id);
}
