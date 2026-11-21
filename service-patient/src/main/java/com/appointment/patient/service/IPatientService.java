package com.appointment.patient.service;

import com.appointment.common.entity.Patient;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface IPatientService extends IService<Patient> {
    Patient findById(Integer id);
    List<Patient> findAll();
}
