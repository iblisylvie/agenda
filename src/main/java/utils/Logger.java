package utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.async.AsyncLogger;
import org.apache.logging.log4j.message.Message;

/**
 * @author Zhenyi Zhou, <zyzhou@leyantech.com>
 * @date 2016/06/01
 */
public class Logger {

  private org.apache.logging.log4j.Logger logger;

  public Logger(Class<?> clazz) {
    logger = LogManager.getLogger(clazz);
  }

  public Logger(String name) {
    logger = LogManager.getLogger(name);
  }

  /**
   * Logs message at trace level, return message.
   */
  public String trace(final String message) {
    logger.trace(message);
    return message;
  }

  /**
   * Logs message with parameters at trace level, return formatted message.
   */
  public String trace(final String message, final Object... params) {
    final Message msg = logger.getMessageFactory().newMessage(message, params);
    logger.trace(msg);
    return msg.getFormattedMessage();
  }

  /**
   * Logs a message with the specific Marker at the trace level, return formatted message.
   */
  public String trace(final Throwable throwable, final String message, final Object... params) {
    final Message msg = logger.getMessageFactory().newMessage(message, params);
    logger.trace(msg, throwable);
    return msg.getFormattedMessage();
  }

  /**
   * Logs message at debug level, return message.
   */
  public String debug(final String message) {
    logger.debug(message);
    return message;
  }

  /**
   * Logs message with parameters at debug level, return formatted message.
   */
  public String debug(final String message, final Object... params) {
    final Message msg = logger.getMessageFactory().newMessage(message, params);
    logger.debug(msg);
    return msg.getFormattedMessage();
  }

  /**
   * Logs a message with the specific Marker at the debug level, return formatted message.
   */
  public String debug(final Throwable throwable, final String message, final Object... params) {
    final Message msg = logger.getMessageFactory().newMessage(message, params);
    logger.debug(msg, throwable);
    return msg.getFormattedMessage();
  }

  /**
   * Logs message at info level, return message.
   */
  public String info(final String message) {
    logger.info(message);
    return message;
  }

  /**
   * Logs message with parameters at info level, return formatted message.
   */
  public String info(final String message, final Object... params) {
    final Message msg = logger.getMessageFactory().newMessage(message, params);
    logger.info(msg);
    return msg.getFormattedMessage();
  }

  /**
   * Logs a message with the specific Marker at the info level, return formatted message.
   */
  public String info(final Throwable throwable, final String message, final Object... params) {
    final Message msg = logger.getMessageFactory().newMessage(message, params);
    logger.info(msg, throwable);
    return msg.getFormattedMessage();
  }

  /**
   * Logs message at warn level, return message.
   */
  public String warn(final String message) {
    logger.warn(message);
    return message;
  }

  /**
   * Logs message with parameters at warn level, return formatted message.
   */
  public String warn(final String message, final Object... params) {
    final Message msg = logger.getMessageFactory().newMessage(message, params);
    logger.warn(msg);
    return msg.getFormattedMessage();
  }

  /**
   * Logs a message with the specific Marker at the warn level, return formatted message.
   */
  public String warn(final Throwable throwable, final String message, final Object... params) {
    final Message msg = logger.getMessageFactory().newMessage(message, params);
    logger.warn(msg, throwable);
    return msg.getFormattedMessage();
  }

  /**
   * Logs message at error level, return message.
   */
  public String error(final String message) {
    logger.error(message);
    return message;
  }

  /**
   * Logs message with parameters at error level, return formatted message.
   */
  public String error(final String message, final Object... params) {
    final Message msg = logger.getMessageFactory().newMessage(message, params);
    logger.error(msg);
    return msg.getFormattedMessage();
  }

  /**
   * Logs a message with the specific Marker at the error level, return formatted message.
   */
  public String error(final Throwable throwable, final String message, final Object... params) {
    final Message msg = logger.getMessageFactory().newMessage(message, params);
    logger.error(msg, throwable);
    return msg.getFormattedMessage();
  }

  /**
   * Logs message at fatal level, return message.
   */
  public String fatal(final String message) {
    try {
      logger.fatal(message);
      return message;
    } finally {
      Runtime.getRuntime().exit(-1);
    }
  }

  /**
   * Logs message with parameters at fatal level, return formatted message.
   */
  public String fatal(final String message, final Object... params) {
    try {
      final Message msg = logger.getMessageFactory().newMessage(message, params);
      logger.fatal(msg);
      return msg.getFormattedMessage();
    } finally {
      Runtime.getRuntime().exit(-1);
    }
  }

  /**
   * Logs a message with the specific Marker at the fatal level, return formatted message.
   */
  public String fatal(final Throwable throwable, final String message, final Object... params) {
    try {
      final Message msg = logger.getMessageFactory().newMessage(message, params);
      logger.fatal(msg, throwable);
      return msg.getFormattedMessage();
    } finally {
      Runtime.getRuntime().exit(-1);
    }
  }

  public boolean isAsyncLogger() {
    return this.logger instanceof AsyncLogger;
  }
}