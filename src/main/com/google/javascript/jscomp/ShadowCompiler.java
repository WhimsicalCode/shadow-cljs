package com.google.javascript.jscomp;

import com.google.debugging.sourcemap.SourceMapConsumerV3;
import com.google.debugging.sourcemap.proto.Mapping.OriginalMapping;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ShadowCompiler extends Compiler {

    private final Map<String, SourceMapConsumerV3> consumerCache = new HashMap<>();

    // original file name from an inline input source map -> the source it was mapped through
    private final Map<String, String> inlineMapSources = new HashMap<>();

    public ShadowCompiler() {
        super();
    }

    public ShadowCompiler(PrintStream outStream) {
        super(outStream);
    }

    public ShadowCompiler(ErrorManager errorManager) {
        super(errorManager);
    }

    /**
     * fixing https://github.com/google/closure-compiler/issues/3825 by removing relative path logic
     * the inputs provided by shadow-cljs always use the full name and as such don't need that logic
     *
     * this makes source maps work again on windows
     */
    @Override
    public OriginalMapping getSourceMapping(String sourceName, int lineNumber, int columnNumber) {
        try {
            if (sourceName == null) {
                return null;
            }

            SourceMapInput sourceMap = inputSourceMaps.get(sourceName);
            if (sourceMap == null) {
                return null;
            }

            SourceMapConsumerV3 consumer = consumerCache.get(sourceMap.getOriginalPath());

            if (consumer == null) {
                consumer = sourceMap.getSourceMap(this.getErrorManager());
                if (consumer == null) {
                    return null;
                }

                consumerCache.put(sourceMap.getOriginalPath(), consumer);
            }

            OriginalMapping result = consumer.getMappingForLine(lineNumber, columnNumber + 1);
            if (result == null) {
                return null;
            }

            return result.toBuilder()
                    .setOriginalFile(originalFile(sourceName, sourceMap, result))
                    .setColumnPosition(result.getColumnPosition() - 1)
                    .build();
        } catch (Exception e) {
            // sometimes fails on windows trying to resolve [synthetic:1] sources
            return null;
        }
    }

    /**
     * the input source maps shadow-cljs registers map each source back to itself (CLJS output
     * to its .cljs file), so the mapping's file is always the source's name. a JS source can
     * also carry a map of its own in a sourceMappingURL comment, which closure reads as
     * "<name>.inline.map". that map names the files the source was built from (a bundle's map
     * names every file it bundled), so keep those, resolved against the source's directory.
     */
    private String originalFile(String sourceName, SourceMapInput sourceMap, OriginalMapping mapping) {
        String file = mapping.getOriginalFile();
        if (file.isEmpty() || !sourceMap.getOriginalPath().endsWith(".inline.map")) {
            return sourceName;
        }

        String resolved;
        if (file.contains("://") || file.startsWith("/")) {
            resolved = file;
        } else {
            Path dir = Path.of(sourceName).getParent();
            resolved = (dir == null ? Path.of(file) : dir.resolve(file)).normalize().toString().replace('\\', '/');
        }

        inlineMapSources.put(resolved, sourceName);
        return resolved;
    }

    /**
     * the files that inline input source maps mapped to, each with the source it was mapped
     * through. only complete once source maps have been generated.
     */
    public Map<String, String> getInlineMapSources() {
        return Collections.unmodifiableMap(inlineMapSources);
    }
}
