package com.webservicestudy.webservicestudy.modules.tag;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    public Tag findOrCreateNew(String tagTitle) {
        Tag tag = tagRepository.findByTitle(tagTitle);
        if(tag == null){
            tag = tagRepository.save(Tag.builder().title(tagTitle).build());
        }
        return tag;
    }
}
