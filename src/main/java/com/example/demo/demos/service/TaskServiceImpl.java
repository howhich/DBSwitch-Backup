package com.example.demo.demos.service;


import cn.hutool.core.stream.StreamUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.demos.Exception.DbswitchException;
import com.example.demo.demos.dbswitch.common.entity.CloseableDataSource;
import com.example.demo.demos.dbswitch.common.entity.DbConnectionCreateRequest;
import com.example.demo.demos.dbswitch.common.type.ProductTypeEnum;
import com.example.demo.demos.dbswitch.common.util.DSTestUtils;
import com.example.demo.demos.dbswitch.common.util.DSUtils;
import com.example.demo.demos.dbswitch.common.util.JdbcUrlUtils;
import com.example.demo.demos.dbswitch.data.config.DbswichPropertiesConfiguration;
import com.example.demo.demos.dbswitch.data.entity.SourceDataSourceProperties;
import com.example.demo.demos.dbswitch.data.util.JsonUtils;
import com.example.demo.demos.dbswitch.schema.TableDescription;
import com.example.demo.demos.entity.DatabaseConnectionEntity;
import com.example.demo.demos.entity.TaskCron;
import com.example.demo.demos.handler.BackupHandler;
import com.example.demo.demos.mapper.DbConnectionMapper;
import com.example.demo.demos.mapper.TaskMapper;
import com.example.demo.demos.response.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
//import org.quartz.*;
//import org.quartz.impl.StdScheduler;
//import org.quartz.impl.StdSchedulerFactory;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.config.CronTask;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

//import static com.example.demo.demos.dbswitch.common.util.DSUtils.createURLClassLoader;


@Service
@Slf4j
public class TaskServiceImpl extends ServiceImpl<TaskMapper, TaskCron> implements TaskService  {
    @Autowired
    private Scheduler scheduler;

    private List<JobDetail> jobs = new ArrayList<>();

    private List<Trigger> triggers = new ArrayList<>();

    @Autowired
    private TaskMapper taskMapper;
//    @Autowired
//    private DynamicScheduleTask dynamicScheduleTask;

    @Override
    public List<TaskCron> getTasks() {
        List<TaskCron> taskCrons = taskMapper.selectList(new LambdaQueryWrapper<>());
        return taskCrons;
    }

    @Override
    public String addTask(Long id) {
        TaskCron taskCron = taskMapper.selectOne(new LambdaQueryWrapper<TaskCron>().eq(TaskCron::getId, id));
        String cron = taskCron.getCron();
        try {
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity("trigger1","group1")
                    .startNow()
//                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
//                            .withIntervalInSeconds(1)
//                            .repeatForever())
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .build();
//            Trigger trigger1 = TriggerBuilder.newTrigger().withIdentity("trigger2","group2")
//                    .startNow()
//                    .withSchedule(SimpleScheduleBuilder.simpleSchedule()
//                            .withIntervalInSeconds(1)
//                            .repeatForever())
//                    .withSchedule(CronScheduleBuilder.cronSchedule("0/1 * * * * ?"))
//                    .build();
            JobDetail job = JobBuilder.newJob(HelloJob.class)
                    .withIdentity("job1","group1")
                    .usingJobData("name","quartz")
                    .build();
//            Set<Trigger> triggers = new HashSet<>();
//            triggers.add(trigger);
//            triggers.add(trigger1);
//            Map<JobDetail, Set<Trigger>> map = new HashMap<>();
//            map.put(job,triggers);
//            scheduler.scheduleJobs(Collections.unmodifiableMap(map), false);
            scheduler.scheduleJob(job, trigger);
            //启动任务调度
            scheduler.start();
            log.info("task: {} has been started", taskCron.getTaskName());
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public String stopTask(Long id) {
        TaskCron taskCron = taskMapper.selectOne(new LambdaQueryWrapper<TaskCron>().eq(TaskCron::getId, id));
        String taskName = taskCron.getTaskName();
        try{
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.pauseTrigger(TriggerKey.triggerKey("trigger1","group1"));
            log.info("task: {} has been stopped",taskName);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public String startTask(Long id) {
        try {

//            Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey("trigger1", "group1"));
            scheduler.resumeTrigger(TriggerKey.triggerKey("trigger1", "group1"));
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public String updateTask(TaskCron taskCron) {
        Long id = taskCron.getId();
        try{
            Trigger.TriggerState triggerState = scheduler.getTriggerState(TriggerKey.triggerKey("trigger1", "group1"));

            if(triggerState.equals(Trigger.TriggerState.NORMAL)){
                scheduler.pauseTrigger(TriggerKey.triggerKey("trigger1", "group1"));
                return "先停止";
            }


            scheduler.pauseTrigger(TriggerKey.triggerKey("trigger1", "group1"));
            taskMapper.update(taskCron, new LambdaQueryWrapper<TaskCron>()
                    .eq(TaskCron::getId, id));
            scheduler.resumeTrigger(TriggerKey.triggerKey("trigger1", "group1"));
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
