package com.springbatch.dataLoader;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.batch.core.Job;

@SpringBootApplication
public class DataLoaderApplication  implements CommandLineRunner {


	private final JobLauncher jobLauncher;

	private final Job job;

	public DataLoaderApplication(JobLauncher jobLauncher, Job job){
		this.jobLauncher = jobLauncher;
		this.job = job;
	}
	public static void main(String[] args) {
		SpringApplication.run(DataLoaderApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception{
		JobParameters jobParameters = new JobParametersBuilder()
				.addLong("time",System.currentTimeMillis())
				.toJobParameters();
		jobLauncher.run(job,jobParameters);
	}
}
