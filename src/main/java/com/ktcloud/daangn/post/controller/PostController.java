package com.ktcloud.daangn.post.controller;

import com.ktcloud.daangn.config.dto.BaseResponse;
import com.ktcloud.daangn.post.ntt.Post;
import com.ktcloud.daangn.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public BaseResponse<Long> create(@RequestBody Post post) {
        Long id = postService.save(post);
        return BaseResponse.success(id);
    }

    @GetMapping
    public BaseResponse<List<Post>> list() {
        List<Post> posts = postService.findAll();
        return BaseResponse.success(posts);
    }

    @GetMapping("/{postId}")
    public BaseResponse<Post> detail(@PathVariable Long postId) {
        Post post = postService.findById(postId);
        return BaseResponse.success(post);
    }
}