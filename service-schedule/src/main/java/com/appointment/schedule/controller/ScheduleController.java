package com.appointment.schedule.controller;

import com.appointment.common.entity.DoctorSchedule;
import com.appointment.common.entity.Result;
import com.appointment.schedule.service.IScheduleService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RefreshScope
@RequestMapping("/schedule")
public class ScheduleController {

    @Resource
    private IScheduleService scheduleService;

    @Value("${booking.open:true}")
    private boolean bookingOpen;

    @Value("${booking.notice:Clinic booking is open.}")
    private String bookingNotice;

    @GetMapping("/config")
    public Result<Object> config() {
        Map<String, Object> map = new HashMap<>();
        map.put("bookingOpen", bookingOpen);
        map.put("bookingNotice", bookingNotice);
        return Result.ok(map);
    }

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
        if (!bookingOpen) {
            return Result.fail(403, "Booking is currently paused by clinic admin (Nacos Config)");
        }
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
