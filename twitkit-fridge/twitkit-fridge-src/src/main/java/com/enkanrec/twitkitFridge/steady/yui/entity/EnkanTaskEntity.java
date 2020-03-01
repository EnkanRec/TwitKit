/*
 * Author : Rinka
 * Date   : 2020/2/4
 */
package com.enkanrec.twitkitFridge.steady.yui.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.ToString;

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
@Table(name = "enkan_task", schema = "yui")
public class EnkanTaskEntity {
    private int tid;
    private String statusId;
    private String url;
    private String content;
    private String media;
    private boolean published;
    private boolean hided;
    private String comment;
    private String twitterUid;
    private Integer refTid;
    private Timestamp pubDate;
    private String extra;
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
    @Column(name = "url", length = 767)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Basic
    @Column(name = "status_id", length = 255)
    public String getStatusId() {
        return statusId;
    }

    public void setStatusId(String statusId) {
        this.statusId = statusId;
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
    @Column(name = "extra")
    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    @Basic
    @Column(name = "pub_date")
    public Timestamp getPubDate() {
        return pubDate;
    }

    public void setPubDate(Timestamp pubDate) {
        this.pubDate = pubDate;
    }

    @Basic
    @Column(name = "twitter_uid")
    public String getTwitterUid() {
        return twitterUid;
    }

    public void setTwitterUid(String twitterUid) {
        this.twitterUid = twitterUid;
    }

    @Basic
    @Column(name = "ref_tid")
    public Integer getRefTid() {
        return refTid;
    }

    public void setRefTid(Integer refTid) {
        this.refTid = refTid;
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
                Objects.equals(statusId, that.statusId) &&
                Objects.equals(url, that.url) &&
                Objects.equals(content, that.content) &&
                Objects.equals(media, that.media) &&
                Objects.equals(comment, that.comment) &&
                Objects.equals(hided, that.hided) &&
                Objects.equals(extra, that.extra) &&
                Objects.equals(twitterUid, that.twitterUid) &&
                Objects.equals(refTid, that.refTid) &&
                Objects.equals(pubDate, that.pubDate) &&
                Objects.equals(newdate, that.newdate) &&
                Objects.equals(updatetime, that.updatetime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tid, statusId, url, content, media, published, comment, newdate, updatetime, hided, pubDate, extra, refTid, twitterUid);
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
