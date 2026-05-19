package com.ktcloud.daangn.notification.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;

class EmitterRepositoryImplTest {

    EmitterRepositoryImpl emitterRepository;

    @BeforeEach
    void setUp() {
        emitterRepository = new EmitterRepositoryImpl();
    }

    @Test
    @DisplayName("save() 호출 후 저장된 Emitter를 get()으로 반환한다")
    void save_andGet_returnsSameEmitter() {
        SseEmitter emitter = new SseEmitter();

        emitterRepository.save(1L, emitter);
        SseEmitter found = emitterRepository.get(1L);

        assertThat(found).isSameAs(emitter);
    }

    @Test
    @DisplayName("save()는 저장된 Emitter 인스턴스를 반환한다")
    void save_returnsStoredEmitter() {
        SseEmitter emitter = new SseEmitter();

        SseEmitter returned = emitterRepository.save(2L, emitter);

        assertThat(returned).isSameAs(emitter);
    }

    @Test
    @DisplayName("존재하지 않는 receiverId로 get()하면 null을 반환한다")
    void get_nonExistentId_returnsNull() {
        SseEmitter result = emitterRepository.get(999L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("deleteById() 후 get()하면 null을 반환한다")
    void deleteById_thenGet_returnsNull() {
        emitterRepository.save(3L, new SseEmitter());

        emitterRepository.deleteById(3L);
        SseEmitter result = emitterRepository.get(3L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 ID를 deleteById()해도 예외가 발생하지 않는다")
    void deleteById_nonExistentId_noException() {
        // should not throw
        emitterRepository.deleteById(9999L);
    }

    @Test
    @DisplayName("같은 receiverId로 두 번 save()하면 마지막 Emitter가 반환된다")
    void save_sameId_twice_returnsLatest() {
        SseEmitter first = new SseEmitter();
        SseEmitter second = new SseEmitter();

        emitterRepository.save(5L, first);
        emitterRepository.save(5L, second);

        assertThat(emitterRepository.get(5L)).isSameAs(second);
    }

    @Test
    @DisplayName("여러 receiverId를 저장하면 각각 독립적으로 관리된다")
    void save_multipleIds_independentStorage() {
        SseEmitter e1 = new SseEmitter();
        SseEmitter e2 = new SseEmitter();

        emitterRepository.save(10L, e1);
        emitterRepository.save(20L, e2);

        assertThat(emitterRepository.get(10L)).isSameAs(e1);
        assertThat(emitterRepository.get(20L)).isSameAs(e2);
    }

    @Test
    @DisplayName("한 ID를 deleteById() 해도 다른 ID의 Emitter는 영향받지 않는다")
    void deleteById_doesNotAffectOtherIds() {
        SseEmitter e1 = new SseEmitter();
        SseEmitter e2 = new SseEmitter();
        emitterRepository.save(7L, e1);
        emitterRepository.save(8L, e2);

        emitterRepository.deleteById(7L);

        assertThat(emitterRepository.get(7L)).isNull();
        assertThat(emitterRepository.get(8L)).isSameAs(e2);
    }
}
