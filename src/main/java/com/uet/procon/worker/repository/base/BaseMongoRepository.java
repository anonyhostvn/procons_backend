package com.uet.procon.worker.repository.base;

import com.uet.procon.common.entity.base.BaseEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository
public interface BaseMongoRepository<T extends BaseEntity<Tid>, Tid extends Serializable>
        extends BaseRepository, MongoRepository<T, Tid> {

}
