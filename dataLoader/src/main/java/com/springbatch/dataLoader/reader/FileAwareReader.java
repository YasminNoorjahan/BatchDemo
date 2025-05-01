package com.springbatch.dataLoader.reader;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.core.io.Resource;

public class FileAwareReader extends FlatFileItemReader<String> {
    StepExecution stepExecution;
    JobExecution jobExecution;

    public void setStepExecution(StepExecution stepExecution){
        this.stepExecution = stepExecution;
    }

    public void setJobExecution(JobExecution jobExecution){
        this.jobExecution = jobExecution;
    }

    @Override
    public String read() throws Exception{
        String line = super.read();
        System.out.println("[Reader] Read Line :" + line);

        return line;
    }

    @Override
    public void setResource(Resource resource) {
        super.setResource(resource);
        if(jobExecution != null && resource != null){

            if(jobExecution.getExecutionContext().get("files") != null) {
                String files = (String) jobExecution.getExecutionContext().get("files");
                jobExecution.getExecutionContext().put("files",files+","+resource.getFilename());

            }
            else {
                jobExecution.getExecutionContext().put("files",resource.getFilename());
            }
        }
    }
}
