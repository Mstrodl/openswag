package tech.coolmathgames.swag.component;

import li.cil.oc.api.Network;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.AbstractValue;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashSet;
import java.util.Set;

public class ComputeCardComponent extends BaseComponent {
  public final Set<DgramSocket> connections = new HashSet<DgramSocket>();

  public ComputeCardComponent(EnvironmentHost host) {
    super("compute_card", host);
    node = Network.newNode(this, Visibility.Network).withComponent(getComponentName()).withConnector(32).create();
  }

  @Callback(doc = "function():number; Gets the current real-world time in seconds since the Unix epoch")
  public Object[] time(Context context, Arguments args) {
    return new Object[] {System.currentTimeMillis() / 1000.0};
  }

  @Callback(doc = "function():userdata -- Creates a Dgram socket")
  public Object[] openDgram(Context context, Arguments arguments) throws IOException {
    if(connections.size() > 5) {
      throw new IOException("Too many Dgram sockets open!");
    }
    DgramSocket socket = new DgramSocket(this);
    connections.add(socket);
    return new Object[] { socket };
  }

  public static class DgramSocket extends AbstractValue {
    private ComputeCardComponent component;
    private DatagramChannel socket;

    // Userdata
    @SuppressWarnings("unused")
    public DgramSocket() {
      super();
      this.socket = null;
      this.component = null;
    }

    public DgramSocket(ComputeCardComponent component) throws IOException {
      super();
      this.socket = DatagramChannel.open(StandardProtocolFamily.INET);
      this.socket.bind(null);
      this.socket.configureBlocking(false);
      this.component = component;
    }

    @Callback(doc = "function():string, number, string -- Returns ip, port, and data of received packet")
    public Object[] read(Context context, Arguments arguments) throws IOException {
      if(this.socket == null) {
        throw new IOException("socket was disconnected");
      }
      ByteBuffer buffer = ByteBuffer.allocate(1024*1024);
      // No idea why this isn't already InetSocketAddress...
      InetSocketAddress socketAddress = (InetSocketAddress) socket.receive(buffer);
      if(socketAddress == null) {
        return null;
      }
      String received = new String(buffer.array(), 0, buffer.position());
      return new Object[] {socketAddress.getAddress(), socketAddress.getPort(), received};
    }

    @Callback(doc = "function(address:string, port:number, data:string) -- Sends packet to specified address")
    public Object[] write(Context context, Arguments arguments) throws IOException {
      if(this.socket == null) {
        throw new IOException("socket was disconnected");
      }
      byte[] buf = arguments.checkString(2).getBytes();
      this.socket.send(ByteBuffer.wrap(buf), new InetSocketAddress(arguments.checkString(0), arguments.checkInteger(1)));
      return null;
    }

    @Callback(doc = "function() -- Closes the socket")
    public Object[] close(Context context, Arguments arguments) {
      this.close();
      return null;
    }

    @Override
    public void dispose(Context context) {
      super.dispose(context);
      this.close();
    }

    private void close() {
      if(this.socket != null) {
        try {
          this.socket.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      if(this.component != null) {
        component.connections.remove(this);
      }
      this.component = null;
      this.socket = null;
    }
  }
}
