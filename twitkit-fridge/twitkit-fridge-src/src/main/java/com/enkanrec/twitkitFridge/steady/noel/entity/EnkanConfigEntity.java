/*
 * Author : Rinka
 * Date   : 2020/2/3
 */
package com.enkanrec.twitkitFridge.steady.noel.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Class : EnkanConfigEntity
 * Usage :
 */
@Entity
@Table(name = "enkan_config", schema = "Noel")
public class EnkanConfigEntity {
    private long id;
    private String namespace;
    private String configKey;
    private String configValue;
    private Timestamp newdate;
    private Timestamp updatetime;

    @Id
    @Column(name = "id", nullable = false)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "namespace", nullable = false, length = 255)
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Basic
    @Column(name = "config_key", nullable = false, length = 255)
    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

    @Basic
    @Column(name = "config_value", nullable = true, length = -1)
    public String getConfigValue() {
        return configValue;
    }

    public void setConfigValue(String configValue) {
        this.configValue = configValue;
    }

    @Basic
    @Column(name = "newdate", nullable = false, updatable = false, insertable = false)
    public Timestamp getNewdate() {
        return newdate;
    }

    public void setNewdate(Timestamp newdate) {
        this.newdate = newdate;
    }

    @Basic
    @Column(name = "updatetime", nullable = false, updatable = false, insertable = false)
    public Timestamp getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Timestamp updatetime) {
        this.updatetime = updatetime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnkanConfigEntity that = (EnkanConfigEntity) o;
        return id == that.id &&
                Objects.equals(namespace, that.namespace) &&
                Objects.equals(configKey, that.configKey) &&
                Objects.equals(configValue, that.configValue) &&
                Objects.equals(newdate, that.newdate) &&
                Objects.equals(updatetime, that.updatetime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, namespace, configKey, configValue, newdate, updatetime);
    }
}
