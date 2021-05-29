package demo.logic.utilities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import demo.config.Configurations;
import demo.logic.exceptions.InvalidFileException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import java.io.File;
import java.io.FileNotFoundException;

@Component
@PropertySource("classpath:application.properties")
public class XMLReader {
	private String configFile;
	
	@Autowired
	public XMLReader(@Value("${config.file}") String configFile) {
		super();
		this.configFile = configFile;
		loadConfigFile();
	}
	
	public Configurations loadConfigFile() {
		try {
			Configurations configurations;
			File xmlFile = ResourceUtils.getFile("classpath:" + this.configFile);
			if (xmlFile == null || !xmlFile.exists()) {
				throw new InvalidFileException("Config file was not found or empty");
			}
			JAXBContext jaxbContext = JAXBContext.newInstance(Configurations.class);
	        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	        JAXBElement<Configurations> elemConfig = jaxbUnmarshaller.unmarshal(new StreamSource(xmlFile), Configurations.class);
	        configurations = elemConfig.getValue();
	        return configurations;
		} catch (JAXBException | FileNotFoundException e) {
			throw new InvalidFileException(e.getMessage());
		}
	}

}
