package com.appointment.schedule.controller;

import com.appointment.common.entity.DoctorSchedule;
import com.appointment.common.entity.Result;
import com.appointment.schedule.service.IScheduleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/schedule")
public class ScheduleController {

    @Resource
    private IScheduleService scheduleService;

    @GetMapping("/find/{id}")
    public Result<DoctorSchedule> findById(@PathVariable Integer id) {
        return Result.ok(scheduleService.findById(id));
    }

    @GetMapping("/findAll")
    public Result<List<DoctorSchedule>> findAll() {
        return Result.ok(scheduleService.findAll());
    }

    @PostMapping("/deduct/{id}")
    public Result<DoctorSchedule> deduct(@PathVariable Integer id) {
        DoctorSchedule updated = scheduleService.deductQuota(id);
        if (updated == null) {
            return Result.fail(400, "No available slots for this schedule");
        }
        return Result.ok(updated);
    }

    @PostMapping("/release/{id}")
    public Result<DoctorSchedule> release(@PathVariable Integer id) {
        DoctorSchedule updated = scheduleService.releaseQuota(id);
        if (updated == null) {
            return Result.fail(404, "Schedule not found");
        }
        return Result.ok(updated);
    }
}
