package cliclient.dao;

import cliclient.model.Language;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LanguageDao {

    private static final RowMapper<Language> RS_2_LANGUAGE = (rs, i) -> Language.builder()
            .id(rs.getLong("id"))
            .name(rs.getString("name"))
            .build();

    private final JdbcTemplate jdbcTemplate;

    public List<Language> findAll() {
        return jdbcTemplate.query("""
                        select *
                        from language""",
                RS_2_LANGUAGE);
    }

    public Integer countWithId(long id) {
        return jdbcTemplate.queryForObject("""
                        select count(*)
                        from language
                        where id = %s""".formatted(id),
                Integer.class);
    }

    public Language findByName(String name) {
        return jdbcTemplate.queryForObject("""
                        select *
                        from language
                        where name = '%s'""".formatted(name),
                RS_2_LANGUAGE);
    }

    public Integer countWithName(String name) {
        return jdbcTemplate.queryForObject("""
                        select count(*)
                        from language
                        where name = '%s'""".formatted(name),
                Integer.class);
    }

    public long add(String name) {
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> con.prepareStatement("""
                        insert into language
                        (name)
                        values ('%s')""".formatted(name),
                new String[]{"id"}), keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void delete(Long id) {
        jdbcTemplate.update("""
                delete from language
                where id = %s""".formatted(id));
    }

    public Language findById(long id) {
        return jdbcTemplate.queryForObject("""
                        select *
                        from language
                        where id = %s""".formatted(id),
                RS_2_LANGUAGE);
    }

}