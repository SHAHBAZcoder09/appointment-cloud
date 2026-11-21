package com.appointment.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("t_appointment")
public class Appointment implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String appointmentNo;
    private Integer patientId;
    private Integer scheduleId;
    private String status; // BOOKED, CANCELLED, EXPIRED
    private LocalDateTime createTime;

    @TableField(exist = false)
    private Patient patient;

    @TableField(exist = false)
    private DoctorSchedule schedule;
}
