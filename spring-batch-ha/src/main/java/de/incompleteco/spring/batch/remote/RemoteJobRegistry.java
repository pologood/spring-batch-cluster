package de.incompleteco.spring.batch.remote;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.configuration.JobFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.job.SimpleJob;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.beans.factory.annotation.Autowired;

import de.incompleteco.spring.batch.domain.JobEntity;
import de.incompleteco.spring.batch.domain.JobEntityRepository;

/**
 * implementation to support remote jobs
 * @author incomplete-co.de
 *
 */
public class RemoteJobRegistry implements JobRegistry {

	@Autowired
	private JobEntityRepository jobEntityRepository;
	
	private JobRegistry localJobRegistry;
	
	public RemoteJobRegistry(JobRegistry localJobRegistry) {
		this.localJobRegistry = localJobRegistry;
	}
	
	@Override
	public Collection<String> getJobNames() {
		Collection<String> names = new ArrayList<String>();
		List<JobEntity> entities = jobEntityRepository.findAll();
		for (JobEntity entity : entities) {
			names.add(entity.getName());
		}//end for
		return names;
	}


	@Override
	public Job getJob(String name) throws NoSuchJobException {
		//check if exists locally
		if (localJobRegistry.getJob(name) != null) {
			return localJobRegistry.getJob(name);
		}//end if
		//check if the name exists
		if (jobEntityRepository.findByName(name) == null) {
			throw new NoSuchJobException("job doesn't exist");
		}//end if
		//build a 'fake' job
		Job job = new SimpleJob(name);
		//return
		return job;
	}


	@Override
	public void register(JobFactory jobFactory) throws DuplicateJobException {
		//build an entity
		JobEntity entity = new JobEntity();
		entity.setName(jobFactory.getJobName());
		//save
		jobEntityRepository.save(entity);
		//register it 'locally'
		localJobRegistry.register(jobFactory);
	}


	@Override
	public void unregister(String jobName) {
		jobEntityRepository.delete(jobName);
		//remove locally
		localJobRegistry.unregister(jobName);
	}
	

}