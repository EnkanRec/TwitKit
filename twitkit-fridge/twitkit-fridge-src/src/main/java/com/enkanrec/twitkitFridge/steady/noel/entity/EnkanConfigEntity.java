/*
 * Author : Rinka
 * Date   : 2020/1/30
 */
package com.enkanrec.twitkitFridge.steady.noel.entity;

import lombok.ToString;

import javax.persistence.*;
import java.util.Objects;

/**
 * Class : EnkanConfigEntity
 * Usage :
 */
@Entity
@ToString
@Table(name = "enkanConfig", schema = "Noel")
public class EnkanConfigEntity {
    private long id;
    private String key;
    private String value;

    @Id
    @Column(name = "id", nullable = false)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "key", nullable = false, length = 255)
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Basic
    @Column(name = "value", nullable = true, length = -1)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnkanConfigEntity that = (EnkanConfigEntity) o;
        return id == that.id &&
                Objects.equals(key, that.key) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, key, value);
    }
}
