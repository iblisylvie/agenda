// Copyright 2016 Leyantech Ltd. All Rights Reserved.
package utils;

import org.apache.logging.log4j.core.util.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;

/**
 * Utils to process protocol buffer.
 *
 * @author Qian Li, {@literal <qli@leyantech.com>}
 * @date 2016-07-11.
 */
public class PbUtils {
  private static final Logger LOG = new Logger(PbUtils.class);

  public static void loadFromText(InputStream is, GeneratedMessageV3.Builder builder)
      throws IOException {
    TextFormat.merge(new InputStreamReader(is, FileUtils.DEFAULT_CHARSET), builder);
  }

  public static void loadFromBinary(InputStream is, GeneratedMessageV3.Builder builder)
      throws IOException {
    builder.mergeFrom(removeStreamSizeLimit(is));
  }

  private static CodedInputStream removeStreamSizeLimit(InputStream inputStream) {
    // Pre-create the CodedInputStream so that we can remove the size limit restriction
    // when parsing.
    CodedInputStream codedInputStream = CodedInputStream.newInstance(inputStream);
    codedInputStream.setSizeLimit(Integer.MAX_VALUE);
    return codedInputStream;
  }

  // encode by base64.
  public static String dumpToBase64String(GeneratedMessageV3 msg) {
    return Base64.getEncoder().encodeToString(msg.toByteArray());
  }

  /**
   * a wrapper of dumpToBase64String for lazy evaluating in log message rendering.
   *
   * <p>
   * you can safely replace statements like
   * <code>LOG.debug("request is {}", PbUtils.dumpToBase64String(request))</code>
   * with
   * <code>LOG.debug("request is {}", PbUtils.asBase64String(request))</code>
   * which should be more efficient when <pre>DEBUG</pre> logging level is disabled,
   * as the redundant proto message marshaling is avoided.
   *
   * BUG AGAIN, it is strongly unrecommended to dump a proto message literally in any log.
   * </p>
   *
   * @param msg the proto message to dump
   * @return an anonymous object with toString method overridden to toShortDebugString.
   */
  public static Object asBase64String(GeneratedMessageV3 msg) {
    return new Object() {
      @Override
      public String toString() {
        return dumpToBase64String(msg);
      }
    };
  }

  public static void loadFromBase64String(String msg, GeneratedMessageV3.Builder builder)
      throws IOException {
    byte[] bytes = Base64.getDecoder().decode(msg);
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    builder.mergeFrom(bais);
  }

  // 打印protobuf in text format, 换行符替换成" "
  public static String toShortDebugString(Message message) {
    if (message == null) return null;
    return TextFormat.printToUnicodeString(message).replace("\n", " ");
  }

  /**
   * a wrapper of toShortDebugString for lazy evaluating in string template rendering.
   *
   * <p>
   * you can safely replace statements like
   * <code>LOG.debug("request is {}", PbUtils.toShortDebugString(request))</code>
   * with
   * <code>LOG.debug("request is {}", PbUtils.asShortDebugString(request))</code>
   * which should be more efficient when <pre>DEBUG</pre> logging level is disabled,
   * as the redundant proto message marshaling is avoided.
   *
   * BUG AGAIN, it is strongly unrecommended to dump a proto message literally in any log.
   * </p>
   *
   * @param msg the proto message to dump
   * @return an anonymous object with toString method overridden to toShortDebugString.
   */
  public static Object asShortDebugString(Message msg) {
    return new Object() {
      @Override
      public String toString() {
        return toShortDebugString(msg);
      }
    };
  }

  public static void loadFromString(String str, Message.Builder message) {
    try {
      TextFormat.getParser().merge(str, message);
    } catch (TextFormat.ParseException e) {
      LOG.fatal("Parse protocol failed, {}", e);
    }
  }
}