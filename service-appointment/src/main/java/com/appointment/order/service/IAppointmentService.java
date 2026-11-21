package com.appointment.order.service;

import com.appointment.common.entity.Appointment;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface IAppointmentService extends IService<Appointment> {
    Appointment book(Integer patientId, Integer scheduleId);
    Appointment cancel(Integer appointmentId);
    List<Appointment> findByPatientId(Integer patientId);
    List<Appointment> findAllAggregated();
}
