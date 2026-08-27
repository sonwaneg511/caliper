package com.caliper.post.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "post_location_map",
    indexes = {
        @Index(name = "idx_dealer", columnList = "dealer_id"),
        @Index(name = "idx_client", columnList = "client_id"),
        @Index(name = "idx_post_location_map_created_date", columnList = "created_date")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(PostLocationMapId.class)
public class PostLocationMap {

    @Id
    @Column(name = "post_id")
    private Long postId;

    @Id
    @Column(name = "dealer_id")
    private String dealerId;

    @Column(name = "client_id", nullable = false)
    private String clientId;

    @Column(name = "platform", length = 200)
    private String platform;

    @Column(name = "views", nullable = false)
    private Long views;

    @Column(name = "likes", nullable = false)
    private Long likes;

    @Column(name = "shares", nullable = false)
    private Long shares;

    @Column(name = "comments", nullable = false)
    private Long comments;

    @Column(name = "status", nullable = false)
    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(
        name = "created_date",
        nullable = false,
        columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
    )
    private Date createdDate;

    @Column(name = "console_post_id")
    private String consolePostId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "post_id",
        referencedColumnName = "post_id",
        insertable = false,
        updatable = false
    )
    private Post post;
}
