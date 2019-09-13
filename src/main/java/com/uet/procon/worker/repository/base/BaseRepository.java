package com.uet.procon.worker.repository.base;

import com.uet.procon.common.entity.base.BaseEntity;

import java.io.Serializable;

public interface BaseRepository<T extends BaseEntity<Tid>, Tid extends Serializable> {

}
