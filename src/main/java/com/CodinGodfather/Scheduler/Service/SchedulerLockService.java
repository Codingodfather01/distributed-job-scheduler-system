package com.CodinGodfather.Scheduler.Service;

import com.CodinGodfather.Scheduler.Entity.SchedulerLockEntity;
import com.CodinGodfather.Scheduler.Repository.SchedulerLockRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SchedulerLockService {

    private final SchedulerLockRepository schedulerLockRepository;

    private final String instanceId = "scheduler-instance-1";

    public SchedulerLockService(SchedulerLockRepository schedulerLockRepository) {
        this.schedulerLockRepository = schedulerLockRepository;
    }

    public boolean acquireLock(String lockName, int lockSeconds) {
        LocalDateTime now = LocalDateTime.now();

        Optional<SchedulerLockEntity> optionalLock =
                schedulerLockRepository.findById(lockName);

        if (optionalLock.isEmpty()) {
            SchedulerLockEntity lock = new SchedulerLockEntity();
            lock.setLockName(lockName);
            lock.setLockedBy(instanceId);
            lock.setLockedAt(now);
            lock.setLockUntil(now.plusSeconds(lockSeconds));

            schedulerLockRepository.save(lock);
            return true;
        }

        SchedulerLockEntity lock = optionalLock.get();

        if (lock.getLockUntil().isBefore(now)) {
            lock.setLockedBy(instanceId);
            lock.setLockedAt(now);
            lock.setLockUntil(now.plusSeconds(lockSeconds));

            schedulerLockRepository.save(lock);
            return true;
        }

        return false;
    }
}