package de.ksul.archiv;

import de.ksul.archiv.configuration.ArchivConfiguration;
import de.ksul.archiv.configuration.ArchivTestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ArchivTestConfiguration.class, ArchivConfiguration.class})
public class ArchivApplicationTests {


	@Autowired
	private MockMvc mockMvc;



	@Test
	public void contextLoads() {
	}


	@Test
	public void testIsConnected() throws Exception {
		this.mockMvc.perform(MockMvcRequestBuilders.get("/Archiv/isConnected")
				.accept(MediaType.APPLICATION_JSON_UTF8))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(MockMvcResultMatchers.content().contentType("application/json"));

	}

}
