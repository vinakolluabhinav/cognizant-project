package com.depositcorex.Main.Controller;



import com.depositcorex.Main.SIExecutionJob.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dev-tools")
public class DevToolsController {

    @Autowired
    private StandingInstructionJob siJob;

    /**
     * Manually triggers the Daily Batch Job.
     * POST http://localhost:8080/api/v1/dev-tools/trigger-si-job
     */
    @PostMapping("/trigger-si-job")
    public ResponseEntity<String> triggerJob() {
        System.out.println("MANUAL TRIGGER: Starting Standing Instruction Batch Job...");
        siJob.processDailySIs();
        return ResponseEntity.ok("Batch Job Triggered Successfully. Check Console for logs.");
    }
}