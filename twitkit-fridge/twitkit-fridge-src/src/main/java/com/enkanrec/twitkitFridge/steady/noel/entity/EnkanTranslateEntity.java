/*
 * Author : Rinka
 * Date   : 2020/2/4
 */
package com.enkanrec.twitkitFridge.steady.noel.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Class : EnkanTranslateEntity
 * Usage :
 */
@Entity
@Table(name = "enkan_translate", schema = "Noel")
public class EnkanTranslateEntity {
    private int zzid;
    private int tid;
    private int version;
    private String translation;
    private String img;
    private Timestamp newdate;
    private Timestamp updatetime;

    @Id
    @Column(name = "zzid", nullable = false)
    public int getZzid() {
        return zzid;
    }

    public void setZzid(int zzid) {
        this.zzid = zzid;
    }

    @Basic
    @Column(name = "tid", nullable = false)
    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    @Basic
    @Column(name = "version", nullable = false)
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Basic
    @Column(name = "translation", nullable = false, length = -1)
    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }

    @Basic
    @Column(name = "img", nullable = false, length = 2047)
    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    @Basic
    @Column(name = "newdate", nullable = false, insertable = false, updatable = false)
    public Timestamp getNewdate() {
        return newdate;
    }

    public void setNewdate(Timestamp newdate) {
        this.newdate = newdate;
    }

    @Basic
    @Column(name = "updatetime", nullable = false, insertable = false, updatable = false)
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
        EnkanTranslateEntity that = (EnkanTranslateEntity) o;
        return zzid == that.zzid &&
                tid == that.tid &&
                version == that.version &&
                Objects.equals(translation, that.translation) &&
                Objects.equals(img, that.img) &&
                Objects.equals(newdate, that.newdate) &&
                Objects.equals(updatetime, that.updatetime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zzid, tid, version, translation, img, newdate, updatetime);
    }
}
