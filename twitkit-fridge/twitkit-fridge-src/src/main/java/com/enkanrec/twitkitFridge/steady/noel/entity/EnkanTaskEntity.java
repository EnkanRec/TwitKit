/*
 * Author : Rinka
 * Date   : 2020/2/4
 */
package com.enkanrec.twitkitFridge.steady.noel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.ToString;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLInsert;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

/**
 * Class : EnkanTaskEntity
 * Usage :
 */
@Entity
@ToString(exclude = {"translations"})
@Table(name = "enkan_task", schema = "Noel")
public class EnkanTaskEntity {
    private int tid;
    private String url;
    private String content;
    private String media;
    private boolean published;
    private boolean hided;
    private String comment;
    private Timestamp newdate;
    private Timestamp updatetime;

    private List<EnkanTranslateEntity> translations;

    @Id
    @Column(name = "tid", nullable = false, insertable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public int getTid() {
        return tid;
    }

    public void setTid(int tid) {
        this.tid = tid;
    }

    @Basic
    @Column(name = "url", nullable = false, length = 767)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Basic
    @Column(name = "content", nullable = false, length = -1)
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Basic
    @Column(name = "media", nullable = false, length = -1)
    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    @Basic
    @Column(name = "published", nullable = false)
    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    @Basic
    @Column(name = "hided", nullable = false)
    public boolean isHided() {
        return hided;
    }

    public void setHided(boolean hided) {
        this.hided = hided;
    }

    @Basic
    @Column(name = "comment", nullable = false, length = 1023)
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
        EnkanTaskEntity that = (EnkanTaskEntity) o;
        return tid == that.tid &&
                published == that.published &&
                Objects.equals(url, that.url) &&
                Objects.equals(content, that.content) &&
                Objects.equals(media, that.media) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(newdate, that.newdate) &&
                Objects.equals(updatetime, that.updatetime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tid, url, content, media, published, comment, newdate, updatetime);
    }

    @JsonIgnore
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public List<EnkanTranslateEntity> getTranslations() {
        return translations;
    }

    public void setTranslations(List<EnkanTranslateEntity> translations) {
        this.translations = translations;
    }
}
