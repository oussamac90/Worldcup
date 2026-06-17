package com.goalkeeperdash.user.service;

import com.goalkeeperdash.common.error.ApiException;
import com.goalkeeperdash.user.api.NationAdminService;
import com.goalkeeperdash.user.api.NationView;
import com.goalkeeperdash.user.domain.Nation;
import com.goalkeeperdash.user.repo.NationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NationAdminServiceImpl implements NationAdminService {

    private final NationRepository nations;

    public NationAdminServiceImpl(NationRepository nations) {
        this.nations = nations;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NationView> listAll() {
        return nations.findAll(org.springframework.data.domain.Sort.by("name")).stream()
                .map(NationServiceImpl::toView).toList();
    }

    @Override
    @Transactional
    public void setActive(String code, boolean active) {
        require(code).setActive(active);
    }

    @Override
    @Transactional
    public void update(String code, String name, String flagColors) {
        Nation nation = require(code);
        if (name != null && !name.isBlank()) nation.setName(name);
        if (flagColors != null) nation.setFlagColors(flagColors);
    }

    private Nation require(String code) {
        return nations.findByCode(code).orElseThrow(() -> ApiException.notFound("Nation not found: " + code));
    }
}
