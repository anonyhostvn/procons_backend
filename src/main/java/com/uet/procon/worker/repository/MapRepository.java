package com.uet.procon.worker.repository;

import com.uet.procon.common.entity.MapImpl;
import com.uet.procon.worker.repository.base.BaseMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MapRepository extends BaseMongoRepository<MapImpl, String> {

}
