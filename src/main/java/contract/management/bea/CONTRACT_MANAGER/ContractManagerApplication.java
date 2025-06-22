package contract.management.bea.CONTRACT_MANAGER;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ContractManagerApplication {

	public static void main(String[] args) {
		    ZipSecureFile.setMinInflateRatio(0.0001);
		SpringApplication.run(ContractManagerApplication.class, args);
	}

}
