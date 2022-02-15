package notesservice;

import api.notes.AddNoteInput;
import api.notes.AddTagsToNoteInput;
import api.notes.NoteDto;
import api.notes.NoteServiceApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/notes/")
@RequiredArgsConstructor
class NoteController implements NoteServiceApi {

    private final NoteService noteService;

    @Deprecated
    @Override
    public Collection<NoteDto> getAll() {
        return noteService.findAll();
    }

    @Override
    public List<NoteDto> findFirst(Integer limit) {
        return noteService.findFirst(limit);
    }

    @Override
    public NoteDto getById(Long id) {
        return noteService.findById(id);
    }

    @Override
    public NoteDto add(AddNoteInput input) {
        return noteService.add(input);
    }

    @Override
    public void deleteById(Long id) {
        noteService.deleteById(id);
    }

    @Override
    public NoteDto addTags(AddTagsToNoteInput input) {
        return noteService.addTags(input);
    }

    @Override
    public Integer countAll() {
        return noteService.countAll();
    }

}
