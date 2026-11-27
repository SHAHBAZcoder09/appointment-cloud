package com.appointment.schedule.service.impl;

import com.appointment.common.entity.DoctorSchedule;
import com.appointment.schedule.mapper.ScheduleMapper;
import com.appointment.schedule.service.IScheduleService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScheduleServiceImpl extends ServiceImpl<ScheduleMapper, DoctorSchedule> implements IScheduleService {

    @Override
    public DoctorSchedule findById(Integer id) {
        return baseMapper.selectById(id);
    }

    @Override
    @Cacheable(value = "schedules", key = "'all'")
    public List<DoctorSchedule> findAll() {
        return baseMapper.selectList(null);
    }

    @Override
    @CacheEvict(value = "schedules", allEntries = true)
    public DoctorSchedule deductQuota(Integer id) {
        int updated = baseMapper.update(null, new LambdaUpdateWrapper<DoctorSchedule>()
                .setSql("available_quota = available_quota - 1")
                .eq(DoctorSchedule::getId, id)
                .gt(DoctorSchedule::getAvailableQuota, 0));
        if (updated == 0) {
            return null;
        }
        return baseMapper.selectById(id);
    }

    @Override
    @CacheEvict(value = "schedules", allEntries = true)
    public DoctorSchedule releaseQuota(Integer id) {
        baseMapper.update(null, new LambdaUpdateWrapper<DoctorSchedule>()
                .setSql("available_quota = available_quota + 1")
                .eq(DoctorSchedule::getId, id)
                .apply("available_quota < total_quota"));
        return baseMapper.selectById(id);
    }

    @Override
    @CacheEvict(value = "schedules", allEntries = true)
    public boolean save(DoctorSchedule entity) {
        return super.save(entity);
    }
}
