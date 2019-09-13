package com.uet.procon.common.entity.base;

public interface BaseEntity<Tid extends Object> {

    Tid getId();

    void setId(Tid id);
}
