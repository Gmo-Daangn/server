package com.ktcloud.daangn.post.service;

import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.service.MemberService;
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
    private final MemberService memberService;

    @Override
    @Transactional
    public PostCreateResponseDto createPost(PostRequestDto request) {
        Member member = memberService.getByIdOrThrow(request.memberId());

        Post savedPost = postRepository.save(
                Post.create(
                        member,
                        request.title(),
                        request.content(),
                        request.price(),
                        member.getAddress().city())
        );

        return new PostCreateResponseDto(
                savedPost.getId(),
                "게시글 등록 성공 (ID: " + savedPost.getId() + ")"
        );
    }

    @Override
    public PostPageResponseDto getPostList(Pageable pageable) {
        Page<Post> page = postRepository.findAll(pageable);

        return PostPageResponseDto.from(page);
    }

    @Override
    @Transactional
    public PostDetailResponseDto getPostDetail(Long postId) {
        Post post = getPostOrThrow(postId);
        post.increaseViewCount();

        return PostDetailResponseDto.from(post);
    }

    @Override
    @Transactional
    public PostDetailResponseDto updatePost(Long postId, PostUpdateRequestDto request) {
        Post post = getPostOrThrow(postId);
        validateOwner(post, request.memberId());

        post.update(
                request.title(),
                request.content(),
                request.price(),
                request.status()
        );

        return PostDetailResponseDto.from(post);
    }

    @Override
    @Transactional
    public String deletePost(Long postId, Long memberId) {
        Post post = getPostOrThrow(postId);
        validateOwner(post, memberId);

        postRepository.delete(post);
        return "게시글 삭제 성공";
    }

    public Post getPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
    }

    private void validateOwner(Post post, Long memberId) {
        if (!post.isOwner(memberId)) {
            throw new IllegalArgumentException("게시글 작성자만 처리할 수 있습니다.");
        }
    }
}
