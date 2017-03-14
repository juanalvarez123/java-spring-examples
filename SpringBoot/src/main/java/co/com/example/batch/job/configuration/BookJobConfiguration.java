package co.com.example.batch.job.configuration;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;

import co.com.example.batch.job.listener.JobCompletionNotificationListener;
import co.com.example.batch.job.processor.BookItemProcessor;
import co.com.example.model.Book;

@Configuration
@EnableBatchProcessing
public class BookJobConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	// @Autowired
	// public DataSource dataSource;

	// tag::readerwriterprocessor[]
	@Bean
	public FlatFileItemReader<Book> reader() {
		FlatFileItemReader<Book> reader = new FlatFileItemReader<Book>();
		reader.setResource(new ClassPathResource("sample-data.csv"));
		reader.setLineMapper(new DefaultLineMapper<Book>() {
			{
				setLineTokenizer(new DelimitedLineTokenizer() {
					{
						setNames(new String[] { "firstName", "lastName" });
					}
				});
				setFieldSetMapper(new BeanWrapperFieldSetMapper<Book>() {
					{
						setTargetType(Book.class);
					}
				});
			}
		});
		return reader;
	}

	@Bean
	public BookItemProcessor processor() {
		return new BookItemProcessor();
	}

	@Bean
	public JdbcBatchItemWriter<Book> writer() {
		JdbcBatchItemWriter<Book> writer = new JdbcBatchItemWriter<Book>();
		// writer.setItemSqlParameterSourceProvider(new
		// BeanPropertyItemSqlParameterSourceProvider<Person>());
		// writer.setSql("INSERT INTO people (first_name, last_name) VALUES
		// (:firstName, :lastName)");
		// writer.setDataSource(dataSource);
		return writer;
	}

	// Defines the job 
	@Bean
	public Job importBookJob(JobCompletionNotificationListener listener) {
		return jobBuilderFactory.get("importUserJob")
				.incrementer(new RunIdIncrementer())
				.listener(listener)
				.flow(step1())
				.end()
				.build();
	}

	//a single step
	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1")
				.<Book, Book>chunk(10)
				.reader(reader())
				.processor(processor())
				.writer(writer())
				.build();
	}
	// end::jobstep[]
}