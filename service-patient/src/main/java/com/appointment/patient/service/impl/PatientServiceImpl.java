package com.appointment.patient.service.impl;

import com.appointment.common.entity.Patient;
import com.appointment.patient.mapper.PatientMapper;
import com.appointment.patient.service.IPatientService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements IPatientService {

    @Override
    @Cacheable(value = "patient", key = "#id", unless = "#result == null")
    public Patient findById(Integer id) {
        return baseMapper.selectById(id);
    }

    @Override
    public List<Patient> findAll() {
        return baseMapper.selectList(null);
    }
}
