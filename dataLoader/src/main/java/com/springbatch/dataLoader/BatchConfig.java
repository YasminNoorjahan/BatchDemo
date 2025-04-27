package com.springbatch.dataLoader;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.ResourceAwareItemReaderItemStream;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.batch.item.file.transform.PassThroughLineAggregator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.repository.JobRepository;

import java.io.IOException;

@Configuration
public class BatchConfig {

    private final PlatformTransactionManager platformTransactionManager;

    private final JobRepository jobRepository;

    public BatchConfig(PlatformTransactionManager platformTransactionManager, JobRepository jobRepository) {
        this.platformTransactionManager = platformTransactionManager;
        this.jobRepository = jobRepository;
    }

    @Bean
    public Job dataLoaderJob() {
        return new JobBuilder("dataLoaderJob", jobRepository)
                .start(preprocessingStep())
                .build();
    }

    @Bean
    public Step preprocessingStep(){

        return new StepBuilder("preprocessingStep", jobRepository)
                .<String,String> chunk(5,platformTransactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }



    @Bean
    public MultiResourceItemReader<String> reader(){
        MultiResourceItemReader<String> reader = new MultiResourceItemReader<>();
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try{
            reader.setResources(resolver.getResources("file:C://inbound1/*.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        reader.setDelegate(singleFileReader());
        return reader;
    }


    @Bean
    public FlatFileItemReader<String> singleFileReader() {
        FlatFileItemReader<String> reader = new FlatFileItemReader<String>(){
            @Override
            public String read() throws Exception{
                String line = super.read();
                System.out.println("[Reader] Read Line :" + line);
                return line;
            }
        };
        reader.setLineMapper(new PassThroughLineMapper());
        return reader;
    }


    @Bean
    public  ItemProcessor<String,String> processor(){
        return line -> {
            System.out.println("[Processor] processed line:"+line);
            return line;
        };
    }

    @Bean
    public ItemWriter<String> writer(){
        return items ->{
            System.out.println("[writer] writing chunks"+items);
        };
    }

//    @Bean
//    public FlatFileItemWriter<String> writer(){
//        FlatFileItemWriter<String> writer= new FlatFileItemWriter<>();
//        writer.setResource(new FileSystemResource("c://outbound/output/txt"));
//        writer.setLineAggregator(new PassThroughLineAggregator<>());
//        return writer;
//    }


}
