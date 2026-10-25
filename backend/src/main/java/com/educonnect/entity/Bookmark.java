package com.educonnect.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Bookmark entity connecting students to their saved educational resources.
 * Enforces database-level uniqueness per user-resource pair.
 */
@Entity
@Table(
    name = "bookmarks",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_bookmarks_user_resource", columnNames = {"user_id", "resource_id"})
    },
    indexes = {
        @Index(name = "idx_bookmarks_user_created", columnList = "user_id, created_at"),
        @Index(name = "idx_bookmarks_resource", columnList = "resource_id")
    }
)
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bookmarks_users"))
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false, foreignKey = @ForeignKey(name = "fk_bookmarks_resources"))
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private Resource resource;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Bookmark() {
    }

    public Bookmark(User user, Resource resource) {
        this.user = user;
        this.resource = resource;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Resource getResource() {
        return resource;
    }

    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
