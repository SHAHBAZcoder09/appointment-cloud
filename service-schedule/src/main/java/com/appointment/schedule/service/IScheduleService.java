package com.appointment.schedule.service;

import com.appointment.common.entity.DoctorSchedule;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface IScheduleService extends IService<DoctorSchedule> {
    DoctorSchedule findById(Integer id);
    List<DoctorSchedule> findAll();
    DoctorSchedule deductQuota(Integer id);
    DoctorSchedule releaseQuota(Integer id);
}
