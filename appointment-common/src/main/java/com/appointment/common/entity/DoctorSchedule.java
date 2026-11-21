package com.appointment.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@TableName("t_schedule")
public class DoctorSchedule implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String doctorName;
    private String department;
    private String scheduleDate;
    private String timeSlot;
    private Integer totalQuota;
    private Integer availableQuota;
    private BigDecimal fee;
}
