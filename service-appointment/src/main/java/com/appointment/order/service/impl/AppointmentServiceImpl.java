package com.appointment.order.service.impl;

import com.appointment.common.entity.*;
import com.appointment.common.exception.BizException;
import com.appointment.order.client.PatientClient;
import com.appointment.order.client.ScheduleClient;
import com.appointment.order.mapper.AppointmentMapper;
import com.appointment.order.service.IAppointmentService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements IAppointmentService {

    @Resource
    private PatientClient patientClient;

    @Resource
    private ScheduleClient scheduleClient;

    @Override
    public Appointment book(Integer patientId, Integer scheduleId) {
        // 1. Validate patient exists
        Result<Patient> patientRes = patientClient.findById(patientId);
        if (patientRes == null || patientRes.getData() == null) {
            throw new BizException(404, "Patient not found");
        }

        // 2. Prevent duplicate active booking for the same schedule
        Long existing = baseMapper.selectCount(new LambdaQueryWrapper<Appointment>()
                .eq(Appointment::getPatientId, patientId)
                .eq(Appointment::getScheduleId, scheduleId)
                .eq(Appointment::getStatus, "BOOKED"));
        if (existing > 0) {
            throw new BizException(400, "You already have an active booking for this schedule");
        }

        // 3. Deduct slot in service-schedule
        Result<DoctorSchedule> deductRes = scheduleClient.deduct(scheduleId);
        if (deductRes == null || deductRes.getCode() != 200 || deductRes.getData() == null) {
            throw new BizException(400, deductRes != null ? deductRes.getMessage() : "No available slots");
        }

        // 4. Save appointment
        Appointment apt = new Appointment();
        apt.setAppointmentNo("APT" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase());
        apt.setPatientId(patientId);
        apt.setScheduleId(scheduleId);
        apt.setStatus("BOOKED");
        apt.setCreateTime(LocalDateTime.now());
        baseMapper.insert(apt);

        // 5. Aggregate Patient + Schedule for frontend display
        apt.setPatient(patientRes.getData());
        apt.setSchedule(deductRes.getData());
        return apt;
    }

    @Override
    public Appointment cancel(Integer appointmentId) {
        Appointment apt = baseMapper.selectById(appointmentId);
        if (apt == null) {
            throw new BizException(404, "Appointment not found");
        }
        if (!"BOOKED".equals(apt.getStatus())) {
            throw new BizException(400, "Only BOOKED appointments can be cancelled");
        }
        apt.setStatus("CANCELLED");
        baseMapper.updateById(apt);
        scheduleClient.release(apt.getScheduleId());

        Result<Patient> pRes = patientClient.findById(apt.getPatientId());
        Result<DoctorSchedule> sRes = scheduleClient.findById(apt.getScheduleId());
        if (pRes != null) apt.setPatient(pRes.getData());
        if (sRes != null) apt.setSchedule(sRes.getData());
        return apt;
    }

    @Override
    public List<Appointment> findByPatientId(Integer patientId) {
        List<Appointment> list = baseMapper.selectList(
                new LambdaQueryWrapper<Appointment>().eq(Appointment::getPatientId, patientId)
                        .orderByDesc(Appointment::getId));
        return enrich(list);
    }

    @Override
    public List<Appointment> findAllAggregated() {
        List<Appointment> list = baseMapper.selectList(
                new LambdaQueryWrapper<Appointment>().orderByDesc(Appointment::getId));
        return enrich(list);
    }

    private List<Appointment> enrich(List<Appointment> list) {
        for (Appointment apt : list) {
            Result<Patient> pRes = patientClient.findById(apt.getPatientId());
            Result<DoctorSchedule> sRes = scheduleClient.findById(apt.getScheduleId());
            if (pRes != null) apt.setPatient(pRes.getData());
            if (sRes != null) apt.setSchedule(sRes.getData());
        }
        return list;
    }
}
