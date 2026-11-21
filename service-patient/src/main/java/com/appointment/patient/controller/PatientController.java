package com.appointment.patient.controller;

import com.appointment.common.entity.Patient;
import com.appointment.common.entity.Result;
import com.appointment.patient.service.IPatientService;
import com.appointment.patient.util.JwtUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/patient")
public class PatientController {

    @Resource
    private IPatientService patientService;

    @PostMapping("/login")
    public Result<Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        Patient p = patientService.getOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getUsername, username)
                .eq(Patient::getPassword, password));
        if (p == null) {
            return Result.fail(401, "Invalid username or password");
        }
        String token = JwtUtil.createToken(p.getId(), p.getUsername());
        Map<String, Object> res = new HashMap<>();
        res.put("token", token);
        res.put("patient", p);
        return Result.ok(res);
    }

    @GetMapping("/find/{id}")
    public Result<Patient> findById(@PathVariable Integer id) {
        return Result.ok(patientService.findById(id));
    }

    @GetMapping("/findAll")
    public Result<List<Patient>> findAll() {
        return Result.ok(patientService.findAll());
    }

    @GetMapping("/port")
    public Result<Integer> port(@Value("${server.port}") int port) {
        return Result.ok(port);
    }
}
