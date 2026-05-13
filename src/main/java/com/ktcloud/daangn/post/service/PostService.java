package com.ktcloud.daangn.post.service;

import com.ktcloud.daangn.post.dto.PostCreateResponseDto;
import com.ktcloud.daangn.post.dto.PostDetailResponseDto;
import com.ktcloud.daangn.post.dto.PostPageResponseDto;
import com.ktcloud.daangn.post.dto.PostRequestDto;
import com.ktcloud.daangn.post.dto.PostUpdateRequestDto;
import org.springframework.data.domain.Pageable;

public interface PostService {

    PostCreateResponseDto createPost(PostRequestDto request);

    PostPageResponseDto getPostList(Pageable pageable);

    PostDetailResponseDto getPostDetail(Long postId);

    PostDetailResponseDto updatePost(Long postId, PostUpdateRequestDto request);

    String deletePost(Long postId, Long memberId);
}
