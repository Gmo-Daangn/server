package com.ktcloud.daangn.post.controller;

import com.ktcloud.daangn.common.dto.BaseResponse;
import com.ktcloud.daangn.post.dto.PostCreateResponseDto;
import com.ktcloud.daangn.post.dto.PostDetailResponseDto;
import com.ktcloud.daangn.post.dto.PostPageResponseDto;
import com.ktcloud.daangn.post.dto.PostRequestDto;
import com.ktcloud.daangn.post.dto.PostUpdateRequestDto;
import com.ktcloud.daangn.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public BaseResponse<PostCreateResponseDto> createPost(
            @Valid @RequestBody PostRequestDto request) {
        return BaseResponse.success(postService.createPost(request));
    }

    @GetMapping
    public BaseResponse<PostPageResponseDto> getPostList(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return BaseResponse.success(postService.getPostList(pageable));
    }

    @GetMapping("/{postId}")
    public BaseResponse<PostDetailResponseDto> getPostDetail(
            @PathVariable Long postId) {
        return BaseResponse.success(postService.getPostDetail(postId));
    }

    @PutMapping("/{postId}")
    public BaseResponse<PostDetailResponseDto> updatePost(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequestDto request) {
        return BaseResponse.success(postService.updatePost(postId, request));
    }

    @DeleteMapping("/{postId}")
    public BaseResponse<String> deletePost(
            @PathVariable Long postId,
            @RequestParam Long memberId) {
        return BaseResponse.success(postService.deletePost(postId, memberId));
    }
}
