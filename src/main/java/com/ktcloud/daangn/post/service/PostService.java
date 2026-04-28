package com.ktcloud.daangn.post.service;

import com.ktcloud.daangn.post.dto.PostCreateResponse;
import com.ktcloud.daangn.post.dto.PostRequest;
import com.ktcloud.daangn.post.dto.PostResponse;
import java.util.List;

public interface PostService {
    PostCreateResponse createPost(PostRequest request);
    List<PostResponse> getAllPosts();
    PostResponse getPostById(Long id);
}