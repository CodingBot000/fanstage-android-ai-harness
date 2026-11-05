package dev.fanstage.server;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.IntStream;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
class DemoMediaApi {
    private static final Map<String, String> FILES = IntStream.rangeClosed(1, 7).boxed().collect(
        java.util.stream.Collectors.toUnmodifiableMap(i -> String.format("demo-%02d", i), i -> String.format("demo-%02d.mp4", i)));

    @GetMapping("/demo-media/{id}.mp4")
    ResponseEntity<Resource> media(@PathVariable String id) {
        String directory = System.getenv("FANSTAGE_MEDIA_DIR");
        String filename = FILES.get(id);
        if (directory == null || directory.isBlank() || filename == null) return ResponseEntity.notFound().build();
        Path root = Path.of(directory);
        Path file = root.resolve(filename);
        if (!Files.isDirectory(root) || !Files.isRegularFile(file) || !Files.isReadable(file) || Files.isSymbolicLink(file)) return ResponseEntity.notFound().build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("video/mp4")).body(new FileSystemResource(file));
    }
}
