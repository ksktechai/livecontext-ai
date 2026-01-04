package com.ai.livecontext.repository;

import com.ai.livecontext.domain.AlertEvent;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertEventRepository extends R2dbcRepository<AlertEvent, Long> {}
