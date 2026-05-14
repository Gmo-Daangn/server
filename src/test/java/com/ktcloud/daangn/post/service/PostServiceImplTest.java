package com.ktcloud.daangn.post.service;

import com.ktcloud.daangn.common.exception.InvalidInputException;
import com.ktcloud.daangn.common.valueObject.Address;
import com.ktcloud.daangn.member.entity.Member;
import com.ktcloud.daangn.member.service.MemberService;
import com.ktcloud.daangn.post.dto.PostCreateResponseDto;
import com.ktcloud.daangn.post.dto.PostDetailResponseDto;
import com.ktcloud.daangn.post.dto.PostPageResponseDto;
import com.ktcloud.daangn.post.dto.PostRequestDto;
import com.ktcloud.daangn.post.dto.PostUpdateRequestDto;
import com.ktcloud.daangn.post.entity.Post;
import com.ktcloud.daangn.post.entity.PostStatus;
import com.ktcloud.daangn.post.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    private static final Long MEMBER_ID = 1L;
    private static final Long POST_ID = 10L;

    @Mock
    PostRepository postRepository;

    @Mock
    MemberService memberService;

    @InjectMocks
    PostServiceImpl postService;

    Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(MEMBER_ID)
                .nickName("판매자")
                .address(new Address("서울시", "강남구", "역삼동"))
                .build();
    }

    @Nested
    @DisplayName("Create (생성)")
    class Create {

        @Test
        @DisplayName("[HAPPY] 게시글을 생성한다")
        void createPost_validRequest_savesPost() {
            given(memberService.getByIdOrThrow(MEMBER_ID)).willReturn(member);
            given(postRepository.save(any(Post.class))).willAnswer(invocation -> {
                Post savedPost = invocation.getArgument(0);
                ReflectionTestUtils.setField(savedPost, "id", POST_ID);
                return savedPost;
            });

            PostRequestDto request = new PostRequestDto("자전거", "상태 좋아요", 100000, MEMBER_ID);
            PostCreateResponseDto result = postService.createPost(request);

            assertThat(result.postId()).isEqualTo(POST_ID);
            assertThat(result.message()).contains(String.valueOf(POST_ID));

            ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
            verify(postRepository).save(captor.capture());
            Post captured = captor.getValue();
            assertThat(captured.getMemberId()).isEqualTo(MEMBER_ID);
            assertThat(captured.getLocation()).isEqualTo("서울시");
            assertThat(captured.getStatus()).isEqualTo(PostStatus.FOR_SALE);
        }

        @Test
        @DisplayName("[Exception] 존재하지 않는 회원이면 게시글을 생성하지 않는다")
        void createPost_missingMember_throwsException() {
            given(memberService.getByIdOrThrow(MEMBER_ID))
                    .willThrow(new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "존재하지 않는 ID입니다."));

            PostRequestDto request = new PostRequestDto("자전거", "상태 좋아요", 100000, MEMBER_ID);

            assertThatThrownBy(() -> postService.createPost(request))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("존재하지 않는 ID입니다.");
            verify(postRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Read (조회)")
    class Read {

        @Test
        @DisplayName("[HAPPY] 게시글 목록을 페이지로 조회한다")
        void getPostList_existingPosts_returnsPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Post post = savedPost(POST_ID);
            given(postRepository.findAll(pageable)).willReturn(new PageImpl<>(List.of(post), pageable, 1));

            PostPageResponseDto result = postService.getPostList(pageable);

            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.contents()).hasSize(1);
            assertThat(result.contents().getFirst().postId()).isEqualTo(POST_ID);
            assertThat(result.contents().getFirst().title()).isEqualTo("자전거");
        }

        @Test
        @DisplayName("[HAPPY] 게시글 상세를 조회하고 조회수를 증가시킨다")
        void getPostDetail_existingPost_increasesViewCount() {
            Post post = savedPost(POST_ID);
            given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));

            PostDetailResponseDto result = postService.getPostDetail(POST_ID);

            assertThat(result.postId()).isEqualTo(POST_ID);
            assertThat(result.sellerNickname()).isEqualTo("판매자");
            assertThat(result.viewCount()).isEqualTo(1);
            assertThat(post.getViewCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("[Exception] 게시글이 없으면 예외가 발생한다")
        void getPostDetail_missingPost_throwsException() {
            given(postRepository.findById(POST_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> postService.getPostDetail(POST_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("해당 게시글이 존재하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("Update (수정)")
    class Update {

        @Test
        @DisplayName("[HAPPY] 작성자가 게시글을 수정한다")
        void updatePost_owner_updatesPost() {
            Post post = savedPost(POST_ID);
            given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));

            PostUpdateRequestDto request = new PostUpdateRequestDto(
                    "수정된 제목",
                    "수정된 내용",
                    90000,
                    PostStatus.RESERVED,
                    MEMBER_ID
            );
            PostDetailResponseDto result = postService.updatePost(POST_ID, request);

            assertThat(result.title()).isEqualTo("수정된 제목");
            assertThat(result.content()).isEqualTo("수정된 내용");
            assertThat(result.price()).isEqualTo(90000);
            assertThat(result.status()).isEqualTo(PostStatus.RESERVED);
            assertThat(post.getTitle()).isEqualTo("수정된 제목");
        }

        @Test
        @DisplayName("[HAPPY] 수정 요청에 상태가 없으면 기존 상태를 유지한다")
        void updatePost_statusMissing_keepsCurrentStatus() {
            Post post = savedPost(POST_ID);
            given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));

            PostUpdateRequestDto request = new PostUpdateRequestDto(
                    "수정된 제목",
                    "수정된 내용",
                    90000,
                    null,
                    MEMBER_ID
            );
            PostDetailResponseDto result = postService.updatePost(POST_ID, request);

            assertThat(result.status()).isEqualTo(PostStatus.FOR_SALE);
            assertThat(post.getStatus()).isEqualTo(PostStatus.FOR_SALE);
        }

        @Test
        @DisplayName("[Exception] 작성자가 아니면 게시글을 수정할 수 없다")
        void updatePost_notOwner_throwsException() {
            Post post = savedPost(POST_ID);
            given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));

            PostUpdateRequestDto request = new PostUpdateRequestDto(
                    "수정된 제목",
                    "수정된 내용",
                    90000,
                    PostStatus.SOLD,
                    2L
            );

            assertThatThrownBy(() -> postService.updatePost(POST_ID, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("게시글 작성자만 처리할 수 있습니다.");
        }
    }

    @Nested
    @DisplayName("Delete (삭제)")
    class Delete {

        @Test
        @DisplayName("[HAPPY] 작성자가 게시글을 삭제한다")
        void deletePost_owner_deletesPost() {
            Post post = savedPost(POST_ID);
            given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));

            String result = postService.deletePost(POST_ID, MEMBER_ID);

            assertThat(result).isEqualTo("게시글 삭제 성공");
            verify(postRepository).delete(post);
        }

        @Test
        @DisplayName("[Exception] 작성자가 아니면 게시글을 삭제할 수 없다")
        void deletePost_notOwner_throwsException() {
            Post post = savedPost(POST_ID);
            given(postRepository.findById(POST_ID)).willReturn(Optional.of(post));

            assertThatThrownBy(() -> postService.deletePost(POST_ID, 2L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("게시글 작성자만 처리할 수 있습니다.");
            verify(postRepository, never()).delete(any());
        }
    }

    private Post savedPost(Long postId) {
        Post post = new Post(member, "자전거", "상태 좋아요", 100000, "서울시");
        ReflectionTestUtils.setField(post, "id", postId);
        return post;
    }
}
