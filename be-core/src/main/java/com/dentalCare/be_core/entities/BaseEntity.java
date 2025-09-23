package com.dentalCare.be_core.entities;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {

	/**
	 * Unique identifier of the entity.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Date and time when the record was created.
	 */
	@Column(name = "created_datetime", nullable = false, updatable = false)
	private LocalDateTime createdDatetime;

	/**
	 * ID of the user who created the record.
	 */
	@Column(name = "created_user", nullable = false, updatable = false)
	private Long createdUser;

	/**
	 * Date and time of the last record update.
	 */
	@Column(name = "last_updated_datetime", nullable = false)
	private LocalDateTime lastUpdatedDatetime;

	/**
	 * ID of the user who performed the last update.
	 */
	@Column(name = "last_updated_user", nullable = false)
	private Long lastUpdatedUser;

	/**
	 * Indicates whether the record is active (1) or inactive (0).
	 * Used to implement logical deletions.
	 */
	@Column(name = "is_active", nullable = false)
	private Boolean isActive = true;

	/**
	 * Method executed before persisting the entity.
	 * Sets creation and update timestamps, and marks as active.
	 */
	@PrePersist
	protected void onCreate() {
		LocalDateTime now = LocalDateTime.now();
		this.createdDatetime = now;
		this.lastUpdatedDatetime = now;

		if (this.isActive == null) {
			this.isActive = true;
		}
	}

	/**
	 * Method executed before updating the entity.
	 * Updates the last modification timestamp.
	 */
	@PreUpdate
	protected void onUpdate() {
		this.lastUpdatedDatetime = LocalDateTime.now();
	}

	/**
	 * Convenience method to perform a logical deletion.
	 * Marks the record as inactive without physically deleting it.
	 *
	 * @param userId ID of the user performing the deletion
	 */
	public void softDelete(Long userId) {
		this.isActive = false;
		this.lastUpdatedUser = userId;
		this.lastUpdatedDatetime = LocalDateTime.now();
	}

	/**
	 * Convenience method to reactivate a record.
	 * Marks the record as active again.
	 *
	 * @param userId ID of the user performing the reactivation
	 */
	public void activate(Long userId) {
		this.isActive = true;
		this.lastUpdatedUser = userId;
		this.lastUpdatedDatetime = LocalDateTime.now();
	}

	/**
	 * Checks if the record is active.
	 *
	 * @return true if the record is active, false otherwise
	 */
	public boolean isActive() {
		return Boolean.TRUE.equals(this.isActive);
	}

	/**
	 * Checks if the record is inactive (logical deletion).
	 *
	 * @return true if the record is inactive, false otherwise
	 */
	public boolean isInactive() {
		return !isActive();
	}
}

