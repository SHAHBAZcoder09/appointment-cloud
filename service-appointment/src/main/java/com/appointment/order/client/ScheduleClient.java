package com.appointment.order.client;

import com.appointment.common.entity.DoctorSchedule;
import com.appointment.common.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "service-schedule")
public interface ScheduleClient {

    @GetMapping("/schedule/find/{id}")
    Result<DoctorSchedule> findById(@PathVariable("id") Integer id);

    @GetMapping("/schedule/findAll")
    Result<List<DoctorSchedule>> findAll();

    @PostMapping("/schedule/deduct/{id}")
    Result<DoctorSchedule> deduct(@PathVariable("id") Integer id);

    @PostMapping("/schedule/release/{id}")
    Result<DoctorSchedule> release(@PathVariable("id") Integer id);
}
