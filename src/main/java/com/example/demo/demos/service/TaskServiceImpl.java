package com.example.demo.demos.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.demos.common.TaskStatus;
import com.example.demo.demos.entity.TaskCron;
import com.example.demo.demos.mapper.TaskMapper;
import lombok.extern.slf4j.Slf4j;
//import org.quartz.*;
//import org.quartz.impl.StdScheduler;
//import org.quartz.impl.StdSchedulerFactory;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

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
    @PostConstruct
    public void init() throws SchedulerException {
        scheduler = StdSchedulerFactory.getDefaultScheduler();
        List<TaskCron> taskCrons = new ArrayList<>();
        taskCrons = taskMapper.selectList(new LambdaQueryWrapper<>());
        taskCrons.forEach(taskCron -> {
            if (taskCron.getTaskStatus().equals(TaskStatus.RUNNING.getStatus())) {
                try {
                    Trigger trigger = TriggerBuilder.newTrigger()
                            .withIdentity(taskCron.getTaskName(),"group1")
                            .startNow()
                            .withSchedule(CronScheduleBuilder.cronSchedule(taskCron.getCron()))
                            .build();

                    JobDetail job = JobBuilder.newJob(HelloJob.class)
                           .withIdentity(taskCron.getTaskName() ,"group1")
                            .usingJobData("datasourceId", taskCron.getDatasourceId())
                            .build();
                    scheduler.scheduleJob(job, trigger);
                    scheduler.start();
                    log.info("job: {} has been initialized successfully", taskCron.getTaskName());
                       }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }
    @Override
    public String addTask(TaskCron taskCron) {
        taskCron.setTaskStatus(TaskStatus.INIT.getStatus());
        int i = taskMapper.insert(taskCron);

        String cron = taskCron.getCron();
        try {

            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(taskCron.getTaskName(),"group1")
                    .startNow()
                    .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                    .build();

            JobDetail job = JobBuilder.newJob(DbConnectionServiceImpl.class)
                    .withIdentity(taskCron.getTaskName(),"group1")
                    .usingJobData("datasourceId", taskCron.getDatasourceId())
                    .build();

            scheduler.scheduleJob(job, trigger);

            log.info("task: {} has been init", taskCron.getTaskName());
        } catch (SchedulerException e) {
            return e.toString();
        }
        return null;
    }

    @Override
    public String stopTask(Long id) {
        TaskCron taskCron = taskMapper.selectOne(new LambdaQueryWrapper<TaskCron>().eq(TaskCron::getId, id));
        String taskName = taskCron.getTaskName();
        try{
            scheduler.pauseTrigger(TriggerKey.triggerKey(taskName ,"group1"));
            taskCron.setTaskStatus(TaskStatus.STOP.getStatus());
            taskMapper.updateById(taskCron);
            log.info("task: {} has been stopped",taskName);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public String startTask(Long id) {
        try {
            TaskCron taskCron = taskMapper.selectOne(new LambdaQueryWrapper<TaskCron>()
                    .eq(TaskCron::getId, id));

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(taskCron.getTaskName(),"group1")
                    .startNow()
                    .withSchedule(CronScheduleBuilder.cronSchedule(taskCron.getCron()))
                    .build();

            JobDetail job = JobBuilder.newJob(HelloJob.class)
                    .withIdentity(taskCron.getTaskName() ,"group1")
                    .usingJobData("datasourceId", taskCron.getDatasourceId())
                    .build();

            if (taskCron.getTaskStatus().equals(TaskStatus.STOP.getStatus())){
                // 暂停分为初始化就暂停 和 后续暂停 初始化暂停时候 jobDetail不存在
                if (!scheduler.checkExists(JobKey.jobKey(taskCron.getTaskName() ,"group1"))){
                    scheduler.scheduleJob(job, trigger);
                    scheduler.start();
                }else {
                // 后续暂停 jobDetail是存在的 所以直接恢复即可
                    scheduler.resumeJob(JobKey.jobKey(taskCron.getTaskName() ,"group1"));
                }
                // 初始化的同样需要启动
            }else if (taskCron.getTaskStatus().equals(TaskStatus.INIT.getStatus())){
                scheduler.scheduleJob(job, trigger);
                scheduler.start();
            }
            taskCron = taskMapper.selectOne(new LambdaQueryWrapper<TaskCron>()
                    .eq(TaskCron::getId, id));
            taskCron.setTaskStatus(TaskStatus.RUNNING.getStatus());
            taskMapper.updateById(taskCron);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public String updateTask(TaskCron taskCron) {
        Long id = taskCron.getId();
        try{
            TaskCron originalTask = taskMapper.
                    selectOne(new LambdaQueryWrapper<TaskCron>().eq(TaskCron::getId, id));
            if (originalTask.getTaskStatus().equals(TaskStatus.RUNNING.getStatus())){
                return "任务正在运行，无法修改";
            }
//            if(triggerState.equals(Trigger.TriggerState.NORMAL)){
//                scheduler.pauseTrigger(TriggerKey.triggerKey("trigger1", "group1"));
//                return "先停止";
//            }
//            scheduler.pauseTrigger(TriggerKey.triggerKey("trigger1", "group1"));
            taskMapper.update(taskCron, new LambdaQueryWrapper<TaskCron>()
                    .eq(TaskCron::getId, id));
//            scheduler.resumeTrigger(TriggerKey.triggerKey("trigger1", "group1"));
        }catch (Exception e){

        }
        return null;
    }
}
