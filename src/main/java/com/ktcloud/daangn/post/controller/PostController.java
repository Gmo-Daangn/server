package com.ktcloud.daangn.post.controller;

import com.ktcloud.daangn.config.dto.BaseResponse;
import com.ktcloud.daangn.post.dto.PostCreateResponse;
import com.ktcloud.daangn.post.dto.PostRequest;
import com.ktcloud.daangn.post.dto.PostResponse;
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
    public BaseResponse<PostCreateResponse> create(@RequestBody PostRequest request) {
        return BaseResponse.success(postService.createPost(request));
    }

    @GetMapping
    public BaseResponse<List<PostResponse>> list() {
        return BaseResponse.success(postService.getAllPosts());
    }

    @GetMapping("/{id}")
    public BaseResponse<PostResponse> detail(@PathVariable Long id) {
        return BaseResponse.success(postService.getPostById(id));
    }
}