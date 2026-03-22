package tn.comping.spring.backendcomping.services.serviceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.comping.spring.backendcomping.dto.FeedResponseDTO;
import tn.comping.spring.backendcomping.repositories.PostRepository;
import tn.comping.spring.backendcomping.services.FeedService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedServiceImpl implements FeedService {

    private final PostRepository postRepository;

    @Override
    public List<FeedResponseDTO> getFeed(int page, int size) {
        // TODO: Pagination + posts abonnements + publics + tri algo
        // Implémentation future avec abonnements
        return List.of(); 
    }
}

