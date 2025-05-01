package com.springbatch.dataLoader;

import com.springbatch.dataLoader.dto.ProcessedFile;
import com.springbatch.dataLoader.dto.LineEntity;
import com.springbatch.dataLoader.reader.FileAwareReader;
import com.springbatch.dataLoader.repository.ProcessedFileRepository;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.repository.JobRepository;

import java.io.File;
import java.io.IOException;

@Configuration
public class BatchConfig {
    private final PlatformTransactionManager platformTransactionManager;
    private final JobRepository jobRepository;
    private final EntityManagerFactory entityManagerFactory;

    private final ProcessedFileRepository processedFileRepository;

    public BatchConfig(PlatformTransactionManager platformTransactionManager, JobRepository jobRepository, EntityManagerFactory entityManagerFactory, ProcessedFileRepository processedFileRepository) {
        this.platformTransactionManager = platformTransactionManager;
        this.jobRepository = jobRepository;
        this.entityManagerFactory = entityManagerFactory;
        this.processedFileRepository = processedFileRepository;
    }

    @Bean
    public Job dataLoaderJob() {
        return new JobBuilder("dataLoaderJob", jobRepository)
                .start(preprocessingStep())
                .next(afterFilProcessedStep())
                .build();
    }

    @Bean
    public Step preprocessingStep(){
        return new StepBuilder("preprocessingStep", jobRepository)
                .<String, LineEntity> chunk(5,platformTransactionManager)
                .reader(reader())
                .processor(processor())
                .writer(customJpaWriter())
                .listener(injectStepExecution(fileAwareReader()))
                .build();
    }

    @Bean
    @StepScope
    public MultiResourceItemReader<String> reader(){
        MultiResourceItemReader<String> reader = new MultiResourceItemReader<>();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try{
            reader.setResources(resolver.getResources("file:C://inbound1/*.txt"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        reader.setDelegate(fileAwareReader());
        return reader;
    }

    @Bean
    public StepExecutionListener injectStepExecution(FileAwareReader reader){
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                reader.setJobExecution(stepExecution.getJobExecution());
            }
        };
    }

    @Bean
    public FileAwareReader fileAwareReader(){
        FileAwareReader reader = new FileAwareReader();
        reader.setLineMapper(new PassThroughLineMapper());
        return reader;
    }

    @Bean
    public  ItemProcessor<String, LineEntity> processor(){
        return line -> {
            System.out.println("[Processor] processed line:"+line);
//            Thread.sleep(5000);
            return new LineEntity(line);
        };
    }

    @Bean
    public ItemWriter<String> writer(){
        return items ->{
            System.out.println("[writer] writing chunks"+items);

        };
    }

    @Bean
    public JpaItemWriter<LineEntity> customJpaWriter(){
        JpaItemWriter<LineEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return  writer;
    }

//    @Bean
//    public FlatFileItemWriter<String> writer(){
//        FlatFileItemWriter<String> writer= new FlatFileItemWriter<>();
//        writer.setResource(new FileSystemResource("c://outbound/output/txt"));
//        writer.setLineAggregator(new PassThroughLineAggregator<>());
//        return writer;
//    }


    @Bean
    public Tasklet afterFileProcessedTasklet(){
        return ((contribution, chunkContext) -> {
            ExecutionContext jobContext = chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext();
            String files = jobContext.getString("files");
            String[] fileArr = files.split(",");

            for(String fileName: fileArr){
                processedFileRepository.save(new ProcessedFile(fileName,"processed"));
                File file = new File("c://inbound1//"+fileName);
                if (file.exists()){
                    file.delete();
                }
            }
            return  RepeatStatus.FINISHED;
        });
    }

    @Bean
    public Step afterFilProcessedStep(){
        return new StepBuilder("afterFilProcessedStep", jobRepository)
                .tasklet(afterFileProcessedTasklet(),platformTransactionManager)
                .build();
    }
}
