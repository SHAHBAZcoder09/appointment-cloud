package com.appointment.order.client;

import com.appointment.common.entity.Patient;
import com.appointment.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "service-patient")
public interface PatientClient {

    @GetMapping("/patient/find/{id}")
    Result<Patient> findById(@PathVariable("id") Integer id);

    @GetMapping("/patient/port")
    Result<Integer> getPort();
}
