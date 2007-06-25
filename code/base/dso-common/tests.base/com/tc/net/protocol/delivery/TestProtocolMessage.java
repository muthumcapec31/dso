/*
 * All content copyright (c) 2003-2006 Terracotta, Inc., except as may otherwise be noted in a separate copyright
 * notice. All rights reserved.
 */
package com.tc.net.protocol.delivery;

import com.tc.bytes.TCByteBuffer;
import com.tc.exception.ImplementMe;
import com.tc.net.protocol.TCNetworkHeader;
import com.tc.net.protocol.TCNetworkMessage;
import com.tc.net.protocol.delivery.OOOProtocolMessage;

/**
 * 
 */
public class TestProtocolMessage implements OOOProtocolMessage {
  public TCNetworkMessage msg;
  public long             sent;
  public long             ack;
  public boolean          isHandshake      = false;
  public boolean          isHandshakeReply = false;
  public boolean          isSend           = false;
  public boolean          isAck            = false;
  private boolean         isGoodbye        = false;
  public short            sessionId        = 0;

  public TestProtocolMessage(TCNetworkMessage msg, long sent, long ack) {
    this.msg = msg;
    this.sent = sent;
    this.ack = ack;
  }

  public TestProtocolMessage() {
    //
  }

  public long getAckSequence() {
    return ack;
  }

  public long getSent() {
    return sent;
  }

  public boolean isHandshake() {
    return isHandshake;
  }

  public boolean isHandshakeReply() {
    return isHandshakeReply;
  }

  public boolean isSend() {
    return isSend;
  }

  public boolean isAck() {
    return isAck;
  }

  public short getSessionId() {
    return (sessionId);
  }

  public void setSessionId(short id) {
    sessionId = id;
  }

  /*********************************************************************************************************************
   * TCNetworkMessage stuff
   */

  public TCNetworkHeader getHeader() {
    throw new ImplementMe();
  }

  public TCNetworkMessage getMessagePayload() {
    throw new ImplementMe();
  }

  public TCByteBuffer[] getPayload() {
    throw new ImplementMe();
  }

  public TCByteBuffer[] getEntireMessageData() {
    throw new ImplementMe();
  }

  public boolean isSealed() {
    throw new ImplementMe();
  }

  public void seal() {
    throw new ImplementMe();
  }

  public int getDataLength() {
    throw new ImplementMe();
  }

  public int getHeaderLength() {
    throw new ImplementMe();
  }

  public int getTotalLength() {
    throw new ImplementMe();
  }

  public void wasSent() {
    throw new ImplementMe();
  }

  public void setSentCallback(Runnable callback) {
    throw new ImplementMe();
  }

  public Runnable getSentCallback() {
    throw new ImplementMe();
  }

  public void recycle() {
    throw new ImplementMe();
  }

  public boolean isGoodbye() {
    return isGoodbye;
  }

  public void reallyDoRecycleOnWrite() {
    //
  }
}