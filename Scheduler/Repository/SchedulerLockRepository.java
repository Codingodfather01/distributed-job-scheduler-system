package com.CodinGodfather.Scheduler.Repository;

import com.CodinGodfather.Scheduler.Entity.SchedulerLockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchedulerLockRepository extends JpaRepository<SchedulerLockEntity, String> {
}