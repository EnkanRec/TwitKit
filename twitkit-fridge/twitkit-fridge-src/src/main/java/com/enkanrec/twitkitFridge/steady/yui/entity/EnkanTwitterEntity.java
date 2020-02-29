/*
 * Author : Rinka
 * Date   : 2020/2/29
 */
package com.enkanrec.twitkitFridge.steady.yui.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * Class : EnkanTwitterEntity
 * Usage :
 */
@Entity
@Table(name = "enkan_twitter", schema = "yui")
public class EnkanTwitterEntity {
    private int uid;
    private String twitterUid;
    private String name;
    private String display;
    private String avatar;
    private Timestamp updatetime;
    private Timestamp newdate;

    @Id
    @Column(name = "uid", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    @Basic
    @Column(name = "twitter_uid", nullable = false, length = 255)
    public String getTwitterUid() {
        return twitterUid;
    }

    public void setTwitterUid(String twitterUid) {
        this.twitterUid = twitterUid;
    }

    @Basic
    @Column(name = "name", nullable = false, length = 511)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "display", nullable = false, length = 511)
    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    @Basic
    @Column(name = "avatar", nullable = false, length = 2047)
    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnkanTwitterEntity that = (EnkanTwitterEntity) o;
        return uid == that.uid &&
                Objects.equals(twitterUid, that.twitterUid) &&
                Objects.equals(name, that.name) &&
                Objects.equals(display, that.display) &&
                Objects.equals(avatar, that.avatar) &&
                Objects.equals(updatetime, that.updatetime) &&
                Objects.equals(newdate, that.newdate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid, twitterUid, name, display, avatar, updatetime, newdate);
    }

    @Basic
    @Column(name = "updatetime", nullable = false, insertable = false, updatable = false)
    public Timestamp getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Timestamp updatetime) {
        this.updatetime = updatetime;
    }

    @Basic
    @Column(name = "newdate", nullable = false, insertable = false, updatable = false)
    public Timestamp getNewdate() {
        return newdate;
    }

    public void setNewdate(Timestamp newdate) {
        this.newdate = newdate;
    }
}
