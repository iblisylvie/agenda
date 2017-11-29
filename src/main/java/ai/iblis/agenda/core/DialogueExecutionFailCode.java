// Copyright 2016 Leyantech Ltd. All Rights Reserved.

package ai.iblis.agenda.core;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * @author Qian Li, <qli@leyantech.com>
 * @date 2017-03-30
 */
public class DialogueExecutionFailCode {
  public static final String DUPLICATED_REPLY = "Duplicated reply detected.";
  public static final String USER_RESPONSE_TIMEOUT = "User response timeout detected.";
  public static final String DIALOGUE_INTERNAL_EXCEPTION = "Dialogue internal exception detected";
  public static final String AGENT_HIGH_EXEC_FREQUENCY = "Agent has been executed too many frequency.";
  public static final String BAD_REQUEST = "Bad request.";
  public static final String ILLEGAL_API_RESPONSE = "Illegal api response.";
  public static final String CONCEPT_LOAD_FAILED = "Failed to load concept.";
  public static final String NO_VALID_ANSWER_IS_COMPUTED = "No valid answer is computed.";
  public static final String FAILED_TO_COMPUTE_H2H_ANSWER = "Failed to compute h2h answer.";
  public static final String API_CALL_FAILED = "API Call failed.";
  public static final String EXPECTED = "EXPECTED";

  /**
   * @author Qian Li, <qli@leyantech.com>
   * @date 2016-12-01
   */
  public static class AbstractHistory<T> {
    // Deque as stack.
    protected Deque<T> mHistory = new ArrayDeque<T>();

    public void push(T item) {
      mHistory.push(item);
    }

    public T peek() {
      return mHistory.peek();
    }

    public int size() {
      return mHistory.size();
    }

    public boolean isEmpty() {
      return mHistory.isEmpty();
    }

    protected T get(int historyIndex) {
      if (historyIndex > mHistory.size() - 1)
        return null;
      if (historyIndex < 0)
        return null;

      Iterator<T> it = mHistory.descendingIterator();
      if (historyIndex > mHistory.size() / 2) {
        historyIndex = mHistory.size() - 1 - historyIndex;
        it = mHistory.iterator();
      }
      int index = 0;
      T item = null;
      while (it.hasNext()) {
        item = it.next();
        if (index == historyIndex)
          return item;
        ++index;
      }
      return null;
    }

    // 返回一个hisotry从现在往过去方向前进的iterator, 从给定的historyIdx开始。
    protected Iterator<T> getIterator(int historyIdx) {
      Iterator<T> it = mHistory.iterator();
      if (historyIdx > mHistory.size() - 1)
        return null;
      if (historyIdx < 0)
        return null;

      int startHi = mHistory.size() - 1;
      while (historyIdx != startHi) {
        it.next();
        --startHi;
      }
      return it;
    }
  }
}
