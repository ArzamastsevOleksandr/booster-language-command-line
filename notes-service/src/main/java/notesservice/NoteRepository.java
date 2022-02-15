package notesservice;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.stream.Stream;

interface NoteRepository extends JpaRepository<NoteEntity, Long> {

    Integer countAllBy();

    @Query(value = """
            select *
            from notes
            limit ?1
            """, nativeQuery = true)
    Stream<NoteEntity> findFirst(Integer limit);

}
