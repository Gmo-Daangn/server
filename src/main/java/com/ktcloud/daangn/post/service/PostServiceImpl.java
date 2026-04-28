package com.ktcloud.daangn.post.service;

import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.repository.MemberRepository;
import com.ktcloud.daangn.post.dto.PostCreateResponse;
import com.ktcloud.daangn.post.dto.PostRequest;
import com.ktcloud.daangn.post.dto.PostResponse;
import com.ktcloud.daangn.post.entity.Post;
import com.ktcloud.daangn.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

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
                member.getId(),
                "게시글이 성공적으로 등록되었습니다."
        );
    }

    @Override
    public List<PostResponse> getAllPosts() {
        return postRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PostResponse getPostById(Long id) {
        return postRepository.findById(id)
                .map(this::convertToResponse)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }

    private PostResponse convertToResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getPrice(),
                post.getLocation(),
                post.getViewCount(),
                post.getCreatedAt()
        );
    }
}