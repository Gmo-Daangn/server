package com.ktcloud.daangn.post.service;

import com.ktcloud.daangn.post.entity.Post;
import java.util.List;

public interface PostService {
    Long createPost(Post post);
    List<Post> getAllPosts();
    Post getPostById(Long id);
}