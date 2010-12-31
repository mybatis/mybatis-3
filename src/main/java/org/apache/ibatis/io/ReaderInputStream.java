package org.apache.ibatis.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

/**
 * Adapts a <code>Reader</code> as an <code>InputStream</code>.
 * Adapted from <CODE>StringInputStream</CODE>.
 *
 */
public class ReaderInputStream extends InputStream {

  /** Source Reader */
  private Reader in;

  private String encoding = System.getProperty("file.encoding");

  private byte[] slack;

  private int begin;

  /**
   * Construct a <CODE>ReaderInputStream</CODE>
   * for the specified <CODE>Reader</CODE>.
   *
   * @param reader   <CODE>Reader</CODE>.  Must not be <code>null</code>.
   */
  public ReaderInputStream(Reader reader) {
    in = reader;
  }

  /**
   * Construct a <CODE>ReaderInputStream</CODE>
   * for the specified <CODE>Reader</CODE>,
   * with the specified encoding.
   *
   * @param reader     non-null <CODE>Reader</CODE>.
   * @param encoding   non-null <CODE>String</CODE> encoding.
   */
  public ReaderInputStream(Reader reader, String encoding) {
    this(reader);
    if (encoding == null) {
      throw new IllegalArgumentException("encoding must not be null");
    } else {
      this.encoding = encoding;
    }
  }

  /**
   * Reads from the <CODE>Reader</CODE>, returning the same value.
   *
   * @return the value of the next character in the <CODE>Reader</CODE>.
   *
   * @exception IOException if the original <code>Reader</code> fails to be read
   */
  public synchronized int read() throws IOException {
    if (in == null) {
      throw new IOException("Stream Closed");
    }

    byte result;
    if (slack != null && begin < slack.length) {
      result = slack[begin];
      if (++begin == slack.length) {
        slack = null;
      }
    } else {
      byte[] buf = new byte[1];
      if (read(buf, 0, 1) <= 0) {
        result = -1;
      }
      result = buf[0];
    }

    if (result < -1) {
      result += 256;
    }

    return result;
  }

  /**
   * Reads from the <code>Reader</code> into a byte array
   *
   * @param b  the byte array to read into
   * @param off the offset in the byte array
   * @param len the length in the byte array to fill
   * @return the actual number read into the byte array, -1 at
   *         the end of the stream
   * @exception IOException if an error occurs
   */
  public synchronized int read(byte[] b, int off, int len) throws IOException {
    if (in == null) {
      throw new IOException("Stream Closed");
    }

    while (slack == null) {
      char[] buf = new char[len]; // might read too much
      int n = in.read(buf);
      if (n == -1) {
        return -1;
      }
      if (n > 0) {
        slack = new String(buf, 0, n).getBytes(encoding);
        begin = 0;
      }
    }

    if (len > slack.length - begin) {
      len = slack.length - begin;
    }

    System.arraycopy(slack, begin, b, off, len);

    if ((begin += len) >= slack.length) {
      slack = null;
    }

    return len;
  }

  /**
   * Marks the read limit of the StringReader.
   *
   * @param limit the maximum limit of bytes that can be read before the
   *              mark position becomes invalid
   */
  public synchronized void mark(final int limit) {
    try {
      in.mark(limit);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe.getMessage());
    }
  }

  /**
   * @return   the current number of bytes ready for reading
   * @exception IOException if an error occurs
   */
  public synchronized int available() throws IOException {
    if (in == null) {
      throw new IOException("Stream Closed");
    }
    if (slack != null) {
      return slack.length - begin;
    }
    if (in.ready()) {
      return 1;
    } else {
      return 0;
    }
  }

  /**
   * @return false - mark is not supported
   */
  public boolean markSupported() {
    return false; // would be imprecise
  }

  /**
   * Resets the StringReader.
   *
   * @exception IOException if the StringReader fails to be reset
   */
  public synchronized void reset() throws IOException {
    if (in == null) {
      throw new IOException("Stream Closed");
    }
    slack = null;
    in.reset();
  }

  /**
   * Closes the Stringreader.
   *
   * @exception IOException if the original StringReader fails to be closed
   */
  public synchronized void close() throws IOException {
    if (in != null) {
      in.close();
      slack = null;
      in = null;
    }
  }
}
