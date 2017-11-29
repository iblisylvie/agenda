// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.utils;

import com.googlecode.protobuf.format.JsonFormat;
import org.apache.logging.log4j.core.util.FileUtils;
import org.yaml.snakeyaml.Yaml;
import utils.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Qian Li, <qli@leyantech.com>
 * @date 2016-10-18
 */
public class DttUtils {

  private static final Logger LOG = new Logger(DttUtils.class);

  private static final String RES_PREFIX = "uri:";

  public static Map<String, DTT> getImportedDTT(List<String> importedFiles) {
    if (importedFiles == null || importedFiles.isEmpty()) {
      return new HashMap<>();
    }
    return importedFiles.stream().collect(Collectors.toMap(file -> file, file -> {
      try (InputStream is = getStream(file)) {
        DTT.Builder dttBuilder = DTT.newBuilder();
        loadFromYaml(is, dttBuilder);
        return dttBuilder.build();
      } catch (IOException e) {
        LOG.fatal(e, "Failed to load dtt from resource file {}", file);
      }
      return DTT.getDefaultInstance();
    }));
  }

  private static InputStream getStream(String file) throws FileNotFoundException {
    InputStream is = null;
    if (file.startsWith(RES_PREFIX)) {
      String resUri = file.substring(RES_PREFIX.length(), file.length());
      is = DttUtils.class.getResourceAsStream(resUri);
    } else {
      is = new FileInputStream(new File(file));
    }
    if (is == null) {
      LOG.fatal("Failed to load dtt from {}", file);
    }
    return is;
  }

  // 从一个root dtt出发，所有recursively被import的DTT.
  public static Map<String, DTT> getAllImportedDTT(DTT rootDTT) {
    Map<String, DTT> allImports = new HashMap<>();
    getAllImportedDTT(rootDTT, allImports);
    return allImports;
  }

  private static void getAllImportedDTT(DTT rootDTT, Map<String, DTT> allImports) {
    getImportedDTT(rootDTT.getImportList()).forEach((dttPath, dtt) -> {
      allImports.put(dttPath, dtt);
      getAllImportedDTT(dtt, allImports);
    });
  }

  public static void loadFromYaml(InputStream is, Builder builder) throws IOException {
    Yaml yaml = new Yaml();
    Object object = yaml.load(is);
    ObjectMapper objectMapper = new ObjectMapper();
    String jsonString = objectMapper.writeValueAsString(object);
    JsonFormat jsonFormat = new JsonFormat();
    jsonFormat.merge(new ByteArrayInputStream(jsonString.getBytes()), builder);
  }
}
