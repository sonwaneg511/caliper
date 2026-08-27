package com.caliper.jobs.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.caliper.jobs.entity.ScheduledJobDetail;
import com.caliper.jobs.repository.ScheduledJobDetailRepository;
import com.caliper.jobs.service.JobService;

import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@Tag(name = "Job Controller", description = "APIs for Testing operations")
public class JobsController {
	
	@Value("${log.path}")
	public  String logPath;
	
    @Autowired
    private JobService jobService;

    @Autowired
    private ScheduledJobDetailRepository repo;

    @GetMapping("/jobs")
    public String viewJobs(Model model) {
        model.addAttribute("jobs", repo.findAll());
        return "jobs/view-jobs";
    }

    @GetMapping("/jobs/new")
    public String createJobForm(Model model) {
        model.addAttribute("job", new ScheduledJobDetail());
        return "jobs/add-job";
    }

    @PostMapping("/jobs")
    public String saveJob(@ModelAttribute ScheduledJobDetail job, @RequestParam("timeInput") String timeInput) {
    	if (!job.isOneTime() && (job.getDelayMinutes() == null || job.getDelayMinutes() <= 0) && timeInput != null && !timeInput.isEmpty()) {
    	    job.setCronExpression(convertTimeToCron(timeInput));
    	}
        jobService.saveConfig(job);
        return "redirect:/jobs";
    }

    @PostMapping("/jobs/run/{id}")
    public String runJobNow(@PathVariable("id") Long id) {
        jobService.runNow(id);
        return "redirect:/jobs";
    }


    @PostMapping("/jobs/stop/{id}")
    public String stopJob(@PathVariable("id") Long id) {
        System.out.println("Stop called for job ID: " + id);
        jobService.stopNow(id);
        return "redirect:/jobs";
    }

    @GetMapping("/jobs/status/{id}")
    @ResponseBody
    public boolean isJobRunning(@PathVariable("id") Long id) {
        return jobService.isRunning(id);
    }

    private String convertTimeToCron(String hhmm) {
        String[] parts = hhmm.split(":");
        return String.format("0 %s %s * * *", parts[1], parts[0]);
    }

    @GetMapping("/jobs/edit/{id}")
    public String showEditPage(@PathVariable("id") Long id, Model model) {
    	ScheduledJobDetail job = repo.findById(id).orElseThrow();

        String timeInput = "";
        if (!job.isOneTime() && job.getCronExpression() != null) {
            String[] parts = job.getCronExpression().split(" ");
            if (parts.length >= 3) {
                timeInput = parts[2] + ":" + parts[1]; // HH:mm
            }
        }
        
        model.addAttribute("job", job);
        model.addAttribute("timeInput", timeInput);  // ✅ Add this
        return "jobs/edit-job";
    }

    @PostMapping("/jobs/update")
    public String updateJob(@ModelAttribute ScheduledJobDetail job, @RequestParam(required = false) String timeInput) {
      
    	 if (!job.isOneTime() && (job.getDelayMinutes() == null || job.getDelayMinutes() <= 0) && timeInput != null && !timeInput.isEmpty()) {
             job.setCronExpression(convertTimeToCron(timeInput));
         }
        
        jobService.saveConfig(job);
        return "redirect:/jobs";
    }


    @PostMapping("/jobs/delete/{id}")
    public String deleteJob(@PathVariable("id") Long id) {
        repo.deleteById(id);
        return "redirect:/jobs";
    }

    @GetMapping("/logs/{id}")
    public String listLogsForJob(@PathVariable("id") Long jobId, Model model) {	
    	
    	  System.out.println("Log Path = " + logPath);

    	    File logFolder = new File(logPath);

    	    System.out.println("Exists = " + logFolder.exists());

        File[] matchingLogs = logFolder.listFiles((dir, name) -> name.startsWith(jobId + "_") && name.endsWith(".log"));

        List<String> logFiles = new ArrayList<>();
        if (matchingLogs != null) {
            for (File file : matchingLogs) {
                logFiles.add(file.getName());
            }
        }

        model.addAttribute("jobId", jobId);
        model.addAttribute("logFiles", logFiles);
        return "jobs/log-list";
    }
    
    @GetMapping("/logs/view/{fileName}")
    public String viewLogContent(@PathVariable String fileName, Model model) {
        File file = new File(logPath, fileName);
        List<String> lines = new ArrayList<>();
        if (file.exists()) {
            try {
                lines = Files.readAllLines(file.toPath());
            } catch (IOException e) {
                lines.add("Error reading file: " + e.getMessage());
            }
        } else {
            lines.add("Log file not found.");
        }

        model.addAttribute("fileName", fileName);
        model.addAttribute("logLines", lines);
        return "jobs/view-logs";
    }
}
