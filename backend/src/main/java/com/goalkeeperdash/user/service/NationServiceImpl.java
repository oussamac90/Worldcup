package com.goalkeeperdash.user.service;

import com.goalkeeperdash.user.api.NationService;
import com.goalkeeperdash.user.api.NationView;
import com.goalkeeperdash.user.domain.Nation;
import com.goalkeeperdash.user.repo.NationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class NationServiceImpl implements NationService {

    private final NationRepository nations;

    public NationServiceImpl(NationRepository nations) {
        this.nations = nations;
    }

    @Override
    public List<NationView> listActive() {
        return nations.findAllByActiveTrueOrderByNameAsc().stream().map(NationServiceImpl::toView).toList();
    }

    @Override
    public Optional<NationView> findByCode(String code) {
        return nations.findByCode(code).map(NationServiceImpl::toView);
    }

    @Override
    public Optional<NationView> findById(UUID id) {
        return nations.findById(id).map(NationServiceImpl::toView);
    }

    static NationView toView(Nation n) {
        return new NationView(n.getId(), n.getCode(), n.getName(), n.getFlagColors(), n.isActive());
    }
}
