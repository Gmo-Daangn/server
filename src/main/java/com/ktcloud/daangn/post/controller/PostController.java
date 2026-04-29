package com.ktcloud.daangn.post.controller;

import com.ktcloud.daangn.config.dto.BaseResponse;
import com.ktcloud.daangn.post.dto.*;
import com.ktcloud.daangn.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public BaseResponse<PostCreateResponse> createPost(
            @Valid @RequestBody PostRequest request) {
        return BaseResponse.success(postService.createPost(request));
    }

    @GetMapping
    public BaseResponse<PostPageResponse> getPostList(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return BaseResponse.success(postService.getPostList(pageable));
    }

    @GetMapping("/{postId}")
    public BaseResponse<PostDetailResponse> getPostDetail(
            @PathVariable Long postId) {
        return BaseResponse.success(postService.getPostDetail(postId));
    }
}