//--------------------------------------------------------------------------
//	Copyright (c) 1998-2004, Drew Davidson and Luke Blanshard
//  All rights reserved.
//
//	Redistribution and use in source and binary forms, with or without
//  modification, are permitted provided that the following conditions are
//  met:
//
//	Redistributions of source code must retain the above copyright notice,
//  this list of conditions and the following disclaimer.
//	Redistributions in binary form must reproduce the above copyright
//  notice, this list of conditions and the following disclaimer in the
//  documentation and/or other materials provided with the distribution.
//	Neither the name of the Drew Davidson nor the names of its contributors
//  may be used to endorse or promote products derived from this software
//  without specific prior written permission.
//
//	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
//  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
//  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
//  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
//  COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
//  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
//  OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
//  AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
//  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
//  THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
//  DAMAGE.
//--------------------------------------------------------------------------
package org.apache.ibatis.ognl;


/**
 * Superclass for OGNL exceptions, incorporating an optional encapsulated exception.
 *
 * @author Luke Blanshard (blanshlu@netscape.net)
 * @author Drew Davidson (drew@ognl.org)
 */
public class OgnlException extends Exception {
  /**
   * The root evaluation of the expression when the exception was thrown
   */
  private Evaluation evaluation;

  /**
   * Why this exception was thrown.
   *
   * @serial
   */
  private Throwable reason;

  /**
   * Constructs an OgnlException with no message or encapsulated exception.
   */
  public OgnlException() {
    this(null, null);
  }

  /**
   * Constructs an OgnlException with the given message but no encapsulated exception.
   *
   * @param msg the exception's detail message
   */
  public OgnlException(String msg) {
    this(msg, null);
  }

  /**
   * Constructs an OgnlException with the given message and encapsulated exception.
   *
   * @param msg    the exception's detail message
   * @param reason the encapsulated exception
   */
  public OgnlException(String msg, Throwable reason) {
    super(msg);
    this.reason = reason;
  }

  /**
   * Returns the encapsulated exception, or null if there is none.
   *
   * @return the encapsulated exception
   */
  public Throwable getReason() {
    return reason;
  }

  /**
   * Returns the Evaluation that was the root evaluation when the exception was
   * thrown.
   */
  public Evaluation getEvaluation() {
    return evaluation;
  }

  /**
   * Sets the Evaluation that was current when this exception was thrown.
   */
  public void setEvaluation(Evaluation value) {
    evaluation = value;
  }

  /**
   * Returns a string representation of this exception.
   *
   * @return a string representation of this exception
   */
  public String toString() {
    if (reason == null)
      return super.toString();
    return super.toString() + " [" + reason + "]";
  }


  /**
   * Prints the stack trace for this (and possibly the encapsulated) exception on
   * System.err.
   */
  public void printStackTrace() {
    printStackTrace(System.err);
  }

  /**
   * Prints the stack trace for this (and possibly the encapsulated) exception on the
   * given print stream.
   */
  public void printStackTrace(java.io.PrintStream s) {
    synchronized (s) {
      super.printStackTrace(s);
      if (reason != null) {
        s.println("/-- Encapsulated exception ------------\\");
        reason.printStackTrace(s);
        s.println("\\--------------------------------------/");
      }
    }
  }

  /**
   * Prints the stack trace for this (and possibly the encapsulated) exception on the
   * given print writer.
   */
  public void printStackTrace(java.io.PrintWriter s) {
    synchronized (s) {
      super.printStackTrace(s);
      if (reason != null) {
        s.println("/-- Encapsulated exception ------------\\");
        reason.printStackTrace(s);
        s.println("\\--------------------------------------/");
      }
    }
  }
}
