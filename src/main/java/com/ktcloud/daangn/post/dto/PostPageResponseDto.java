package com.ktcloud.daangn.post.dto;

import com.ktcloud.daangn.post.entity.Post;
import org.springframework.data.domain.Page;

import java.util.List;

public record PostPageResponseDto(
        List<PostListResponseDto> contents,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
) {
    public static PostPageResponseDto from(Page<Post> page) {
        return new PostPageResponseDto(
                page.getContent().stream()
                        .map(PostListResponseDto::from)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
