package com.ktcloud.daangn.post.controller;

import com.ktcloud.daangn.config.dto.BaseResponse;
import com.ktcloud.daangn.post.entity.Post;
import com.ktcloud.daangn.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 1. 상품 등록 API
    @PostMapping
    public BaseResponse<Long> create(@RequestBody Post post) {

        Long id = postService.createPost(post);
        return BaseResponse.success(id);
    }

    // 2. 전체 상품 목록 조회 API
    @GetMapping
    public BaseResponse<List<Post>> list() {

        List<Post> posts = postService.getAllPosts();
        return BaseResponse.success(posts);
    }

    // 3. 특정 상품 상세 조회 API
    @GetMapping("/{id}")
    public BaseResponse<Post> detail(@PathVariable Long id) {

        Post post = postService.getPostById(id);
        return BaseResponse.success(post);
    }
}