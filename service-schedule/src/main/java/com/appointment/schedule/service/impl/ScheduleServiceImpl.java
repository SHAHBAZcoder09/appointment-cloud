package com.appointment.schedule.service.impl;

import com.appointment.common.entity.DoctorSchedule;
import com.appointment.schedule.mapper.ScheduleMapper;
import com.appointment.schedule.service.IScheduleService;
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
        DoctorSchedule s = baseMapper.selectById(id);
        if (s == null || s.getAvailableQuota() <= 0) {
            return null;
        }
        s.setAvailableQuota(s.getAvailableQuota() - 1);
        baseMapper.updateById(s);
        return s;
    }

    @Override
    @CacheEvict(value = "schedules", allEntries = true)
    public DoctorSchedule releaseQuota(Integer id) {
        DoctorSchedule s = baseMapper.selectById(id);
        if (s == null) {
            return null;
        }
        if (s.getAvailableQuota() < s.getTotalQuota()) {
            s.setAvailableQuota(s.getAvailableQuota() + 1);
            baseMapper.updateById(s);
        }
        return s;
    }

    @Override
    @CacheEvict(value = "schedules", allEntries = true)
    public boolean save(DoctorSchedule entity) {
        return super.save(entity);
    }
}
