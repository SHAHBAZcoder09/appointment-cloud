package com.appointment.schedule.mapper;

import com.appointment.common.entity.DoctorSchedule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduleMapper extends BaseMapper<DoctorSchedule> {
}
