package dev.fanstage.server;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DemoMediaApiTest {
    @TempDir Path dir;
    @Test void rangeAndAllowlist() throws Exception {
        Files.write(dir.resolve("demo-01.mp4"), new byte[4096]);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new DemoMediaApi(dir.toString())).build();
        mvc.perform(head("/demo-media/demo-01.mp4")).andExpect(status().isOk()).andExpect(header().longValue("Content-Length", 4096));
        mvc.perform(get("/demo-media/demo-01.mp4").header("Range", "bytes=0-1023"))
            .andExpect(status().isPartialContent()).andExpect(header().string("Content-Range", "bytes 0-1023/4096"));
        mvc.perform(get("/demo-media/demo-08.mp4")).andExpect(status().isNotFound());
        mvc.perform(get("/demo-media/demo-02.mp4")).andExpect(status().isNotFound());
    }
    @Test void unsetDirectoryDisablesMedia() throws Exception {
        MockMvcBuilders.standaloneSetup(new DemoMediaApi("")).build()
            .perform(get("/demo-media/demo-01.mp4")).andExpect(status().isNotFound());
    }
}
