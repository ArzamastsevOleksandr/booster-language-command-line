package cliclient.dao;

import cliclient.dao.params.AddVocabularyEntryDaoParams;
import cliclient.dao.params.UpdateVocabularyEntryDaoParams;
import cliclient.model.VocabularyEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@Component
@RequiredArgsConstructor
public class VocabularyEntryDao {

    // todo: DRY ve query has to be changed in many places
    private static final RowMapper<VocabularyEntry> RS_2_VOCABULARY_ENTRY = (rs, i) -> VocabularyEntry.builder()
            .id(rs.getLong("ve_id"))
            .createdAt(rs.getTimestamp("created_at"))
            .lastSeenAt(rs.getTimestamp("last_seen_at"))
            .correctAnswersCount(rs.getInt("cac"))
            .name(rs.getString("w_name"))
            .wordId(rs.getLong("w_id"))
            .definition(rs.getString("definition"))
            .languageId(rs.getLong("l_id"))
            .languageName(rs.getString("l_name"))
            .build();

    private final JdbcTemplate jdbcTemplate;

    public List<VocabularyEntry> findAll() {
        return jdbcTemplate.query("""
                        select ve.id as ve_id, ve.created_at, ve.last_seen_at, ve.correct_answers_count as cac, ve.definition as definition,
                        w.name as w_name, w.id as w_id,
                        l.name as l_name, l.id as l_id
                        from vocabulary_entry ve
                        join word w
                        on ve.word_id = w.id
                        join language l
                        on ve.language_id = l.id
                        order by cac, ve.last_seen_at""",
                RS_2_VOCABULARY_ENTRY);
    }

    public Map<Long, Set<String>> getId2SynonymsMap() {
        // todo: simplify, ve_id is already in the join table
        return jdbcTemplate.query("""
                        select ve.id as ve_id, w.name as synonym
                        from vocabulary_entry__synonym__jt ves
                        join word w
                        on ves.word_id = w.id
                        join vocabulary_entry ve
                        on ves.vocabulary_entry_id = ve.id""",
                equivalentsResultSetExtractor("synonym"));
    }

    public Map<Long, Set<String>> getId2AntonymsMap() {
        return jdbcTemplate.query("""
                        select ve.id as ve_id, w.name as antonym
                        from vocabulary_entry__antonym__jt vea
                        join word w
                        on vea.word_id = w.id
                        join vocabulary_entry ve
                        on vea.vocabulary_entry_id = ve.id""",
                equivalentsResultSetExtractor("antonym"));
    }

    // todo: heavy lifting on db side - return an aggregated result? (id, tags)
    public Map<Long, Set<String>> getId2TagsMap() {
        return jdbcTemplate.query("""
                        select *
                        from vocabulary_entry__tag__jt""",
                id2ValueResultSetExtractor("tag"));
    }

    public Map<Long, Set<String>> getId2ContextsMap() {
        return jdbcTemplate.query("""
                        select *
                        from vocabulary_entry__context__jt""",
                id2ValueResultSetExtractor("context"));
    }

    // todo: return 1 entry instead of a Map to avoid type confusion
    public Map<Long, Set<String>> getTagsByIdMap(Long id) {
        return jdbcTemplate.query("""
                        select *
                        from vocabulary_entry__tag__jt
                        where vocabulary_entry_id = %s""".formatted(id),
                id2ValueResultSetExtractor("tag")
        );
    }

    public Map<Long, Set<String>> getContextsByIdMap(long id) {
        return jdbcTemplate.query("""
                        select *
                        from vocabulary_entry__context__jt
                        where vocabulary_entry_id = %s""".formatted(id),
                id2ValueResultSetExtractor("context")
        );
    }

    private ResultSetExtractor<Map<Long, Set<String>>> id2ValueResultSetExtractor(String column) {
        return rs -> {
            Map<Long, Set<String>> id2Tags = new HashMap<>();
            while (rs.next()) {
                id2Tags.computeIfAbsent(rs.getLong("vocabulary_entry_id"), k -> new HashSet<>())
                        .add(rs.getString(column));
            }
            return id2Tags;
        };
    }

    public long addWithDefaultValues(AddVocabularyEntryDaoParams p) {
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into vocabulary_entry
                    (word_id, language_id, definition)
                    values (%s, %s, ?)""".formatted(p.getWordId(), p.getLanguageId()), new String[]{"id"});
            ps.setString(1, p.getDefinition());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public long addWithAllValues(AddVocabularyEntryDaoParams p) {
        var keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    insert into vocabulary_entry
                    (word_id, language_id, definition, created_at, correct_answers_count, last_seen_at)
                    values (%s, %s, ?, '%s', %s, '%s')""".formatted(p.getWordId(), p.getLanguageId(), p.getCreatedAt(),
                    p.getCorrectAnswersCount(), p.getLastSeenAt()), new String[]{"id"});
            ps.setString(1, p.getDefinition());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().longValue();
    }

    public void addSynonyms(List<Long> synonymIds, long id) {
        jdbcTemplate.batchUpdate("""
                        insert into vocabulary_entry__synonym__jt
                        (vocabulary_entry_id, word_id)
                        values (?, ?)""",
                batchPreparedStatementSetterForWordIds(synonymIds, id));
    }

    public void addAntonyms(List<Long> antonymIds, long id) {
        jdbcTemplate.batchUpdate("""
                        insert into vocabulary_entry__antonym__jt
                        (vocabulary_entry_id, word_id)
                        values (?, ?)""",
                batchPreparedStatementSetterForWordIds(antonymIds, id));
    }

    public void addContexts(List<String> contexts, long id) {
        jdbcTemplate.batchUpdate("""
                        insert into vocabulary_entry__context__jt
                        (vocabulary_entry_id, context)
                        values (?, ?)""",
                batchPreparedStatementSetterForStringValues(contexts, id));
    }

    public void addTags(List<String> tags, long id) {
        jdbcTemplate.batchUpdate("""
                        insert into vocabulary_entry__tag__jt
                        (vocabulary_entry_id, tag)
                        values (?, ?)""",
                batchPreparedStatementSetterForStringValues(tags, id));
    }

    private ResultSetExtractor<Map<Long, Set<String>>> equivalentsResultSetExtractor(String columnName) {
        return rs -> {
            Map<Long, Set<String>> veId2Values = new HashMap<>();
            while (rs.next()) {
                veId2Values.computeIfAbsent(
                        rs.getLong("ve_id"),
                        k -> new HashSet<>()).add(rs.getString(columnName)
                );
            }
            return veId2Values;
        };
    }

    private BatchPreparedStatementSetter batchPreparedStatementSetterForWordIds(List<Long> ids, long vocabularyEntryId) {
        return new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                long wordId = ids.get(i);
                ps.setLong(1, vocabularyEntryId);
                ps.setLong(2, wordId);
            }

            @Override
            public int getBatchSize() {
                return ids.size();
            }
        };
    }

    // todo: separate component
    private BatchPreparedStatementSetter batchPreparedStatementSetterForStringValues(List<String> contexts, long id) {
        return new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                String context = contexts.get(i);
                ps.setLong(1, id);
                ps.setString(2, context);
            }

            @Override
            public int getBatchSize() {
                return contexts.size();
            }
        };
    }

    public void updateCorrectAnswersCount(long id, int cacUpdated) {
        jdbcTemplate.update("""
                update vocabulary_entry ve
                set correct_answers_count = %s
                where ve.id = %s""".formatted(cacUpdated, id));
    }

    public VocabularyEntry findById(long id) {
        return jdbcTemplate.queryForObject("""
                        select ve.id as ve_id, ve.created_at, ve.last_seen_at, ve.correct_answers_count as cac, ve.definition,
                        w.name as w_name, w.id as w_id,
                        l.name as l_name, l.id as l_id
                        from vocabulary_entry ve
                        join word w
                        on ve.word_id = w.id
                        join language l
                        on l.id = ve.language_id
                        where ve.id = %s""".formatted(id),
                RS_2_VOCABULARY_ENTRY);
    }

    public Map<Long, Set<String>> getAntonymsById(long id) {
        return jdbcTemplate.query("""
                        select ve.id as ve_id, w.name as antonym
                        from vocabulary_entry__antonym__jt vea
                        join word w
                        on vea.word_id = w.id
                        join vocabulary_entry ve
                        on vea.vocabulary_entry_id = ve.id
                        where ve.id = %s""".formatted(id),
                equivalentsResultSetExtractor("antonym"));
    }

    public Map<Long, Set<String>> getSynonymsById(long id) {
        return jdbcTemplate.query("""
                        select ve.id as ve_id, w.name as synonym
                        from vocabulary_entry__synonym__jt ves
                        join word w
                        on ves.word_id = w.id
                        join vocabulary_entry ve
                        on ves.vocabulary_entry_id = ve.id
                        where ve.id = %s""".formatted(id),
                equivalentsResultSetExtractor("synonym"));
    }

    public List<VocabularyEntry> findAllWithSynonyms(int limit) {
        return jdbcTemplate.query("""
                        select ve.id as ve_id, ve.created_at,  ve.last_seen_at, ve.correct_answers_count as cac, ve.definition,
                        w.name as w_name, w.id as w_id,
                        l.name as l_name, l.id as l_id
                        from vocabulary_entry ve
                        join word w
                        on ve.word_id = w.id
                        join language l
                        on l.id = ve.language_id
                        where ve.id in (select distinct vocabulary_entry_id from vocabulary_entry__synonym__jt)
                        order by cac, ve.last_seen_at
                        limit %s""".formatted(limit),
                RS_2_VOCABULARY_ENTRY);
    }

    public List<VocabularyEntry> findAllWithAntonyms(int limit) {
        return jdbcTemplate.query("""
                        select ve.id as ve_id, ve.created_at,  ve.last_seen_at, ve.correct_answers_count as cac, ve.definition,
                        w.name as w_name,  w.id as w_id,
                        l.name as l_name, l.id as l_id
                        from vocabulary_entry ve
                        join word w
                        on ve.word_id = w.id
                        join language l
                        on ve.language_id = l.id
                        where ve.id in (select distinct vocabulary_entry_id from vocabulary_entry__antonym__jt)
                        order by cac, ve.last_seen_at
                        limit %s""",
                RS_2_VOCABULARY_ENTRY);
    }

    public List<VocabularyEntry> findAllWithAntonymsAndSynonyms(int limit) {
        return jdbcTemplate.query("""
                        select ve.id as ve_id, ve.created_at,  ve.last_seen_at, ve.correct_answers_count as cac, ve.definition,
                        w.name as w_name,  w.id as w_id,
                        l.name as l_name, l.id as l_id
                        from vocabulary_entry ve
                        join word w
                        on ve.word_id = w.id
                        join language l
                        on ve.language_id = l.id
                        where ve.id in
                        (select distinct vocabulary_entry_id from vocabulary_entry__synonym__jt
                        intersect
                        select distinct vocabulary_entry_id from vocabulary_entry__antonym__jt)
                        order by cac, ve.last_seen_at
                        limit %s""".formatted(limit),
                RS_2_VOCABULARY_ENTRY);
    }

    public List<VocabularyEntry> findAllForLanguageId(long id) {
        return jdbcTemplate.query("""
                        select ve.id as ve_id, ve.created_at,  ve.last_seen_at, ve.correct_answers_count as cac, ve.definition,
                        w.name as w_name, w.id as w_id,
                        l.name as l_name, l.id as l_id
                        from vocabulary_entry ve
                        join word w
                        on ve.word_id = w.id
                        join language l
                        on ve.language_id = l.id
                        where l.id = %s""".formatted(id),
                RS_2_VOCABULARY_ENTRY);
    }

    public Map<Long, Set<String>> getId2TagsMapForLanguageId(long id) {
        return jdbcTemplate.query("""
                        select *
                        from vocabulary_entry__tag__jt vetjt
                        join vocabulary_entry ve
                        on vetjt.vocabulary_entry_id = ve.id
                        join language l
                        on l.id = ve.language_id
                        where l.id = %s""".formatted(id),
                id2ValueResultSetExtractor("tag"));
    }

    public Map<Long, Set<String>> getId2ContextsMapForLanguageId(long id) {
        return jdbcTemplate.query("""
                        select *
                        from vocabulary_entry__context__jt vecjt
                        join vocabulary_entry ve
                        on vecjt.vocabulary_entry_id = ve.id
                        join language l
                        on l.id = ve.language_id
                        where l.id = %s""".formatted(id),
                id2ValueResultSetExtractor("context"));
    }

    public Map<Long, Set<String>> getId2AntonymsMapForLanguageId(long id) {
        return jdbcTemplate.query("""
                        select ve.id as ve_id, w.name as antonym
                        from vocabulary_entry__antonym__jt vea
                        join word w
                        on vea.word_id = w.id
                        join vocabulary_entry ve on
                        vea.vocabulary_entry_id = ve.id
                        join language l
                        on l.id = ve.language_id
                        where l.id = %s""".formatted(id),
                equivalentsResultSetExtractor("antonym"));
    }

    public Map<Long, Set<String>> getId2SynonymsMapForLanguageId(long id) {
        return jdbcTemplate.query("""
                        select ve.id as ve_id, w.name as synonym
                        from vocabulary_entry__synonym__jt ves
                        join word w
                        on ves.word_id = w.id
                        join vocabulary_entry ve
                        on ves.vocabulary_entry_id = ve.id
                        join language l
                        on l.id = ve.language_id
                        where l.id = %s""".formatted(id),
                equivalentsResultSetExtractor("synonym"));
    }

    public Integer countWithId(long id) {
        return jdbcTemplate.queryForObject("""
                        select count(*)
                        from vocabulary_entry
                        where id = %s""".formatted(id),
                Integer.class);
    }

    public Integer countWithWordIdAndLanguageId(long wordId, long languageId) {
        return jdbcTemplate.queryForObject("""
                        select count(*)
                        from vocabulary_entry
                        where word_id = %s
                        and language_id = %s""".formatted(wordId, languageId),
                Integer.class);
    }

    public void markDifficult(long id, boolean difficult) {
        jdbcTemplate.update("""
                update vocabulary_entry
                set is_difficult = %s
                where id = %s""".formatted(difficult, difficult));
    }

    public Integer countWithLanguageId(Long id) {
        return jdbcTemplate.queryForObject("""
                        select count(*)
                        from vocabulary_entry
                        where language_id = %s""".formatted(id),
                Integer.class);
    }

    public void deleteAntonymsById(long id) {
        jdbcTemplate.update("""
                delete from vocabulary_entry__antonym__jt
                where vocabulary_entry_id = %s""".formatted(id));
    }

    public void deleteSynonymsById(long id) {
        jdbcTemplate.update("""
                delete from vocabulary_entry__synonym__jt
                where vocabulary_entry_id = %s""".formatted(id));
    }

    public void updateVocabularyEntry(UpdateVocabularyEntryDaoParams p) {
        jdbcTemplate.update("""
                update vocabulary_entry
                set word_id = %s,
                definition = '%s',
                correct_answers_count = %s
                where id = %s""".formatted(p.getWordId(), p.getDefinition(), p.getCorrectAnswersCount(), p.getId()));
    }

    public List<VocabularyEntry> findAllLimit(Integer limit) {
        return jdbcTemplate.query("""
                        select ve.id as ve_id, ve.created_at, ve.last_seen_at, ve.correct_answers_count as cac, ve.definition as definition,
                        w.name as w_name, w.id as w_id,
                        l.name as l_name, l.id as l_id
                        from vocabulary_entry ve
                        join word w
                        on ve.word_id = w.id
                        join language l
                        on ve.language_id = l.id
                        order by ve.last_seen_at
                        limit %s""".formatted(limit),
                RS_2_VOCABULARY_ENTRY);
    }

    public List<VocabularyEntry> findAllWithSubstring(String substring) {
        return jdbcTemplate.query("""
                        select ve.id as ve_id, ve.created_at, ve.last_seen_at, ve.correct_answers_count as cac, ve.definition as definition,
                        w.name as w_name, w.id as w_id,
                        l.name as l_name, l.id as l_id
                        from vocabulary_entry ve
                        join word w
                        on ve.word_id = w.id and w.name like '%s'
                        join language l
                        on ve.language_id = l.id
                        order by ve.last_seen_at""".formatted(substring),
                RS_2_VOCABULARY_ENTRY);
    }

    public List<VocabularyEntry> findWithSubstringLimit(String substring, Integer limit) {
        String sql = "with substr(s) as (values ('%" + substring + "%')) " +
                """
                        select ve_out.id as ve_id, ve_out.created_at, ve_out.last_seen_at, ve_out.correct_answers_count as cac, ve_out.definition as definition,
                        w_out.name as w_name, w_out.id as w_id,
                        l.name as l_name, l.id as l_id
                        from vocabulary_entry ve_out
                        join word w_out
                        on ve_out.word_id = w_out.id
                        join language l
                        on ve_out.language_id = l.id
                        where exists(select * from word where id = ve_out.word_id and name like (select s from substr))
                        or exists(select * from vocabulary_entry__synonym__jt vesj join word w on vesj.word_id = w.id where vocabulary_entry_id = ve_out.id and w.name like (select s from substr))
                        or exists(select * from vocabulary_entry__antonym__jt veaj join word w on veaj.word_id = w.id where vocabulary_entry_id = ve_out.id and w.name like (select s from substr))
                        or exists(select * from vocabulary_entry__context__jt vecj where vocabulary_entry_id = ve_out.id and context like (select s from substr))
                        order by ve_out.last_seen_at
                        limit %s""".formatted(limit);
        return jdbcTemplate.query(sql, RS_2_VOCABULARY_ENTRY);
    }

    public Integer countTotal() {
        return jdbcTemplate.queryForObject("""
                        select count(*)
                        from vocabulary_entry""",
                Integer.class);
    }

    public Integer countWithSubstring(String substring) {
        String sql = "with substr(s) as (values ('%" + substring + "%')) " +
                """     
                        select count(*)
                        from vocabulary_entry ve_out
                        join word w_out
                        on ve_out.word_id = w_out.id
                        where exists(select * from word where id = ve_out.word_id and name like (select s from substr))
                           or exists(select * from vocabulary_entry__synonym__jt vesj join word w on vesj.word_id = w.id where vocabulary_entry_id = ve_out.id and w.name like (select s from substr))
                           or exists(select * from vocabulary_entry__antonym__jt veaj join word w on veaj.word_id = w.id where vocabulary_entry_id = ve_out.id and w.name like (select s from substr))
                           or exists(select * from vocabulary_entry__context__jt vecj where vocabulary_entry_id = ve_out.id and context like (select s from substr))""";
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }

    public Integer countAny() {
        return jdbcTemplate.queryForObject("""
                        select count(*)
                        from vocabulary_entry""",
                Integer.class);
    }

    public Integer countWithSynonyms() {
        return jdbcTemplate.queryForObject("""
                        select count(distinct vocabulary_entry_id)
                        from vocabulary_entry__synonym__jt""",
                Integer.class);
    }

    public Integer countWithAntonyms() {
        return jdbcTemplate.queryForObject("""
                        select count(distinct vocabulary_entry_id)
                        from vocabulary_entry__antonym__jt""",
                Integer.class);
    }

    @Deprecated
    public Integer countWithAntonymsAndSynonyms() {
        return jdbcTemplate.queryForObject("""
                        select count(*) from
                        (select distinct vocabulary_entry_id
                        from vocabulary_entry__synonym__jt
                        intersect
                        select distinct vocabulary_entry_id
                        from vocabulary_entry__antonym__jt) intersection""",
                Integer.class);
    }

    public void deleteSynonyms(long id) {
        jdbcTemplate.update("""
                delete from vocabulary_entry__synonym__jt sjt
                where sjt.vocabulary_entry_id = %s""".formatted(id));
    }

    public void deleteAntonyms(long id) {
        jdbcTemplate.update("""
                delete from vocabulary_entry__antonym__jt ajt
                where ajt.vocabulary_entry_id = %s""".formatted(id));
    }

    public void deleteContexts(long id) {
        jdbcTemplate.update("""
                delete from vocabulary_entry__context__jt cjt
                where cjt.vocabulary_entry_id = %s""".formatted(id));
    }

    public void removeTagAssociationsById(long id) {
        jdbcTemplate.update("""
                delete from vocabulary_entry__tag__jt
                where vocabulary_entry_id = %s""".formatted(id));
    }

    public void delete(long id) {
        jdbcTemplate.update("""
                delete from vocabulary_entry
                where id = %s""".formatted(id));
    }

    public void addTag(String tag, long id) {
        jdbcTemplate.update("""
                insert into vocabulary_entry__tag__jt
                (vocabulary_entry_id, tag)
                values (%s, '%s')""".formatted(id, tag));
    }

    public void updateLastSeenAtById(long id, Timestamp lastSeenAt) {
        jdbcTemplate.update("""
                update vocabulary_entry
                set last_seen_at = '%s'
                where id = %s""".formatted(lastSeenAt, id));
    }

    public void updateLastSeenAtByIds(List<Long> ids, Timestamp lastSeenAt) {
        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement("""
                    update vocabulary_entry
                    set last_seen_at = ?
                    where id in (select * from unnest(?))""");
            ps.setTimestamp(1, lastSeenAt);
            ps.setArray(2, con.createArrayOf("bigint", ids.toArray(new Long[ids.size()])));
            return ps;
        });
    }

}