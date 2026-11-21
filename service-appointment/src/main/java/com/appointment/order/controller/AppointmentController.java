package com.appointment.order.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.appointment.common.entity.Appointment;
import com.appointment.common.entity.Result;
import com.appointment.common.exception.BizException;
import com.appointment.order.client.PatientClient;
import com.appointment.order.service.IAppointmentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("/appointment")
public class AppointmentController {

    @Resource
    private IAppointmentService appointmentService;

    @Resource
    private PatientClient patientClient;

    @RequestMapping(value = "/book/{patientId}/{scheduleId}", method = {RequestMethod.GET, RequestMethod.POST})
    @SentinelResource(value = "bookAppointment", blockHandler = "bookBlockHandler", fallback = "bookFallback")
    public Result<Appointment> book(@PathVariable Integer patientId, @PathVariable Integer scheduleId) {
        return Result.ok(appointmentService.book(patientId, scheduleId));
    }

    public Result<Appointment> bookBlockHandler(Integer patientId, Integer scheduleId, BlockException ex) {
        return Result.fail(429, "Too many booking requests! Blocked by Sentinel rate limiter.");
    }

    public Result<Appointment> bookFallback(Integer patientId, Integer scheduleId, Throwable ex) {
        if (ex instanceof BizException biz) {
            return Result.fail(biz.getCode(), biz.getMessage());
        }
        return Result.fail(503, "Booking service temporarily unavailable (Sentinel fallback): " + ex.getMessage());
    }

    @RequestMapping(value = "/cancel/{id}", method = {RequestMethod.GET, RequestMethod.POST})
    public Result<Appointment> cancel(@PathVariable Integer id) {
        try {
            return Result.ok(appointmentService.cancel(id));
        } catch (BizException ex) {
            return Result.fail(ex.getCode(), ex.getMessage());
        }
    }

    @GetMapping("/list/{patientId}")
    public Result<List<Appointment>> listByPatient(@PathVariable Integer patientId) {
        return Result.ok(appointmentService.findByPatientId(patientId));
    }

    @GetMapping("/findAll")
    public Result<List<Appointment>> findAll() {
        return Result.ok(appointmentService.findAllAggregated());
    }

    /** Load-balancing demo: hits service-patient instances */
    @GetMapping("/lb/port")
    public Result<Integer> lbPort() {
        return patientClient.getPort();
    }

    /** Circuit-breaker demo */
    @GetMapping("/degrade/{fail}")
    @SentinelResource(value = "degradeDemo", fallback = "degradeFallback")
    public Result<String> degrade(@PathVariable Boolean fail) {
        if (Boolean.TRUE.equals(fail)) {
            throw new RuntimeException("Simulated downstream error");
        }
        return Result.ok("Service is healthy");
    }

    public Result<String> degradeFallback(Boolean fail, Throwable ex) {
        return Result.fail(503, "Circuit breaker fallback triggered!");
    }
}
