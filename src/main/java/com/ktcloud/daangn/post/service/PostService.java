package com.ktcloud.daangn.post.service;

import com.ktcloud.daangn.post.dto.*;
import org.springframework.data.domain.Pageable;

public interface PostService {

    PostCreateResponse createPost(PostRequest request);

    PostPageResponse getPostList(Pageable pageable);

    PostDetailResponse getPostDetail(Long postId);
}