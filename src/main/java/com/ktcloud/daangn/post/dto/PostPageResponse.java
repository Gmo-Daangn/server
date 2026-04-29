package com.ktcloud.daangn.post.dto;

import java.util.List;

public record PostPageResponse(
        List<PostListResponse> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
) {}