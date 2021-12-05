package booster.dao;

import booster.model.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TagDao {

    private static final RowMapper<Tag> RS_2_TAG = (rs, i) -> new Tag(rs.getString("name"));

    private final JdbcTemplate jdbcTemplate;

    public List<Tag> findAll() {
        return jdbcTemplate.query("""
                        select *
                        from tag""",
                RS_2_TAG);
    }

    public String add(String name) {
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> con.prepareStatement("""
                        insert into tag
                        (name)
                        values ('%s')""".formatted(name),
                new String[]{"name"}), keyHolder);
        return keyHolder.getKeyAs(String.class);
    }

    public Tag findByName(String name) {
        return jdbcTemplate.queryForObject("""
                        select *
                        from tag
                        where name = '%s'""".formatted(name),
                RS_2_TAG);
    }

    public Integer countWithName(String tag) {
        return jdbcTemplate.queryForObject("""
                        select count(*)
                        from tag
                        where name = '%s'""".formatted(tag),
                Integer.class);
    }

    public void addAll(List<String> tagsToCreate) {
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    insert into tag (name)
                    select * from (select lower(new_tag) from unnest(?) as new_tag) tags_to_create
                    except
                    select lower(name) from tag""");
            ps.setArray(1, con.createArrayOf("varchar", tagsToCreate.toArray()));
            return ps;
        });
    }

}