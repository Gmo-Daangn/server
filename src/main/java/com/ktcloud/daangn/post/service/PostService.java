package com.ktcloud.daangn.post.service;

import com.ktcloud.daangn.post.ntt.Post;
import com.ktcloud.daangn.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public Long save(Post post) {
        Post savedPost = postRepository.save(post);
        return savedPost.getPostId();
    }

    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Post findById(Long postId) {
        return postRepository.findById(postId).orElse(null);
    }
}