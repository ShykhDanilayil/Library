package com.library.library.controller.dto;

import com.library.library.controller.validation.UniqueNicknameAuthor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Builder
public class AuthorDto {
    @NotBlank
    public String name;
    @UniqueNicknameAuthor
    public String nickname;
}
