package com.bluebear.cinemax.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.List;

@Configuration
public class RagConfig {

    private static final Logger log = LoggerFactory.getLogger(RagConfig.class);

    @Value("vectorstore.json")
    private String vectorStoreName;

    @Value("classpath:/docs/huong-dan-dat-ve.txt")
    private Resource resource;

    @Bean
    SimpleVectorStore simpleVectorStore(@Qualifier("openAiEmbeddingModel") EmbeddingModel embeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        File vectorStoreFile = getVectorStoreFile();

        log.info("Vector Store Path: {}", vectorStoreFile.getAbsolutePath());

        if (vectorStoreFile.exists()) {
            log.info("✅ Vector Store File Exists. Loading...");
            simpleVectorStore.load(vectorStoreFile);
        } else {
            log.info("📄 Vector Store File Not Found. Reading and saving...");
            try {
                TextReader textReader = new TextReader(resource);
                textReader.getCustomMetadata().put("filename", "huong-dan-dat-ve.txt");

                List<Document> documents = textReader.get();
                TextSplitter textSplitter = new TokenTextSplitter();
                List<Document> splitDocuments = textSplitter.apply(documents);

                simpleVectorStore.add(splitDocuments);
                simpleVectorStore.save(vectorStoreFile);

                log.info("✅ Vector Store Saved Successfully.");
            } catch (Exception e) {
                log.error("❌ Failed to load/save vector store", e);
            }
        }

        return simpleVectorStore;
    }

    private File getVectorStoreFile() {
        // Lấy thư mục hiện tại khi chạy file JAR
        String currentDir = System.getProperty("user.dir");
        File dir = new File(currentDir, "vectorstore");

        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                log.info("📂 Created vectorstore directory at {}", dir.getAbsolutePath());
            } else {
                log.warn("⚠️ Could not create vectorstore directory at {}", dir.getAbsolutePath());
            }
        }

        return new File(dir, vectorStoreName);
    }
}
