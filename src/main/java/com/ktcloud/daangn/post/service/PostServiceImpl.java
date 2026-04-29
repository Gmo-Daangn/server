package com.ktcloud.daangn.post.service;

import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.repository.MemberRepository;
import com.ktcloud.daangn.post.dto.*;
import com.ktcloud.daangn.post.entity.Post;
import com.ktcloud.daangn.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public PostCreateResponse createPost(PostRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        Post post = new Post(
                member.getId(),
                request.title(),
                request.content(),
                request.price(),
                member.getLocation()
        );

        Post savedPost = postRepository.save(post);

        return new PostCreateResponse(
                savedPost.getId(),
                "게시글 등록 성공 (ID: " + savedPost.getId() + ")"
        );
    }

    @Override
    public PostPageResponse getPostList(Pageable pageable) {
        Page<Post> page = postRepository.findAll(pageable);

        return new PostPageResponse(
                page.getContent().stream()
                        .map(post -> new PostListResponse(
                                post.getId(),
                                post.getTitle(),
                                post.getPrice(),
                                post.getLocation(),
                                post.getViewCount(),
                                post.getCreatedAt()
                        ))
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    @Override
    public PostDetailResponse getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        Member member = memberRepository.findById(post.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("작성자 정보를 찾을 수 없습니다."));

        post.increaseViewCount();

        return new PostDetailResponse(
                post.getId(),
                member.getNickName(),
                post.getTitle(),
                post.getContent(),
                post.getPrice(),
                post.getLocation(),
                post.getStatus(),
                post.getViewCount(),
                post.getCreatedAt()
        );
    }
}